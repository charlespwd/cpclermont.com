(ns cpclermont.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [cpclermont.db :as db]
            [clojure.java.io :as io]
            [postal.core :as mailer]
            [clojure.tools.logging :as log]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
            [ring.util.response :refer [redirect]]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.basic-authentication :as basic]
            [selmer.middleware]
            [cpclermont.views.views :as v]
            [cemerick.drawbridge :as drawbridge]
            [environ.core :refer [env]]))

(defn- authenticated? [user pass]
  (= [user pass] [(env :repl-user false) (env :repl-password false)]))

(def ^:private drawbridge
  (-> (drawbridge/ring-handler)
      (session/wrap-session)
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
  (ANY "/repl" {:as req}
    (drawbridge req))
  (GET "/" [] (v/home))
  (GET "/blog" [] (redirect "/blog/todo-or-how-to-start-a-blog") #_(v/blog))
  (GET "/blog/" [] (redirect "/blog"))
  (GET "/blog/:id" [id] (cond (db/exists? id) (v/blog (db/article id))
                              :else (redirect "/404")))
  (GET "/contact" [] (v/home))
  (POST "/contact" {:as req} (handle-contact req))
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
  (let [store (cookie/cookie-store {:key (env :session-secret)})]
    (-> app
        ((if (env :dev)
           selmer.middleware/wrap-error-page
           identity))
        ((if (env :production)
           wrap-error-page
           trace/wrap-stacktrace))
        (site {:session {:store store}}))))

(defn -main [& [port]]
  (let [port (Integer. (or port (env :port) 5000))]
    (jetty/run-jetty (wrap-app #'app) {:port port :join? false})))

;; For interactive development:
#_(def server (-main))
#_(.stop server)
