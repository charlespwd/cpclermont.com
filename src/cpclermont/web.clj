(ns cpclermont.web
  (:require [compojure.core :refer [defroutes GET PUT POST DELETE ANY]]
            [compojure.handler :refer [site]]
            [compojure.route :as route]
            [clojure.java.io :as io]
            [clojure.tools.logging :as log]
            [ring.middleware.stacktrace :as trace]
            [ring.middleware.session :as session]
            [ring.middleware.session.cookie :as cookie]
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
  (log/info name "sent you an email from" email "saying" message))

(defroutes app
  (ANY "/repl" {:as req}
    (drawbridge req))
  (GET "/" [] (v/home))
  (GET "/contact" [] (v/home))
  (POST "/contact" {:as req} (do (handle-contact req) (v/home)))
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
