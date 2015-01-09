(ns cpclermont.views.views
  (:require [selmer.parser :as p]
            [cpclermont.views.content :refer [contents]]
            [environ.core :refer [env]]))

(p/set-resource-path! 
  (clojure.java.io/resource "templates"))

#_(when (env :dev)
    (p/cache-off!))

(defn layout [m]
  (p/render-file "layout.html" m))

(defn home []
  (p/render-file "pages/index.html" 
                 {:title     (contents :index-title)
                  :desc      (contents :index-desc)
                  :img       (contents :index-img)
                  :url       (contents :base-url)
                  :site-name (contents :index-site-name)}))
