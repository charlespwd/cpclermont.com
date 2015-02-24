(ns cpclermont.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.route :as route]
            [bestcase.core :as bc]
            [bestcase.store.redis :as bcr]
            [bestcase.util.ring :refer [default-identity-fn identity-middleware-wrapper dashboard-routes]]
            [cpclermont.db :as db]
            [cpclermont.views.views :as v]
            [clojure.java.io :as io]
            [postal.core :as mailer]
            [clojure.tools.logging :as log]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.defaults :refer :all]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :refer [redirect]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [selmer.middleware]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]))

(defn- authenticated? [user pass]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (basic/wrap-basic-authentication authenticated?)))

(defn handle-contact [{{:keys [name email message]} :params}]
  (let [mess (mailer/send-message {:host (env :mailgun-smtp-server)
                                   :port (Integer. (env :mailgun-smtp-port))
                                   :user (env :mailgun-smtp-login)
                                   :pass (env :mailgun-smtp-password)}
                                  {:from    "contact@cpclermont.com"
                                   :reply-to email
                                   :to      "charles@cpclermont.com"
                                   :subject (str "[CPCLERMONT.COM/CONTACT] Hi from " name)
                                   :body    message})
        status (:error mess)]
    (log/info mess)
    (cond (= :SUCCESS status)
          {:status 201}
          :else {:status 400})))

(defroutes app
  (ANY "/repl" {:as req} (drawbridge req))
  (GET "/" [] (v/home))
  (GET "/blog" [] (v/posts db/posts))
  (GET "/blog/" [] (redirect "/blog"))
  (GET "/blog/:id" [id] (if-let [post (db/post id)]
                          (v/post post)
                          (redirect "/404")))
  (GET "/contact" [] (v/home))
  (POST "/contact" {:as req} (handle-contact req))
  (POST "/bingo/:id/:goal" [id goal] (do (bc/score (keyword id) (keyword goal))
                                         {:status 201}))
  (GET "/bestcase" [] (str (bc/results :mailchimp-subject-test :control)))
  (route/resources "/")
  (route/not-found (slurp (io/resource "404.html"))))

(defn wrap-error-page [handler]
  (fn [req]
    (try (handler req)
         (catch Exception e
           {:status 500
            :headers {"Content-Type" "text/html"}
            :body (slurp (io/resource "500.html"))}))))

(defn wrap-app [app]
  (do
    ;; We'd like to have the ab test results persist somewhere.
    (bc/set-config! {:store (bcr/create-redis-store (env :redis-conn))})

    (let [store (cookie/cookie-store {:key (env :session-secret)})
          session-config {:flash true, :store store, :cookies-attrs {:max-age (* 60 60 24 30)}}
          middleware-config (-> site-defaults
                                (assoc :session session-config)
                                (assoc-in [:security :xss-protection] nil) ;; TODO
                                (assoc-in [:security :anti-forgery] false))]
      (-> app

          ((if (env :dev)
             selmer.middleware/wrap-error-page
             identity))

          ((if (env :production)
             wrap-error-page
             trace/wrap-stacktrace))

          ;; ab testing identity middleware, we need to persist the identity
          ((identity-middleware-wrapper
             default-identity-fn
             {:easy-testing true
              :simple-no-bots true}))

          ;; needs to be last (!!!)
          (wrap-defaults middleware-config)))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
#_(def server (-main))
#_(.stop server)
