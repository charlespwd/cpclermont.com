(ns cpclermont.views.views
  (:require [selmer.parser :as p]
            [cpclermont.views.content :refer [contents]]
            [environ.core :refer [env]]))

(defn defaults []
  {:livereload (if-not (env :production)
                 (contents :livereload))})

(p/set-resource-path!
  (clojure.java.io/resource "templates"))

(when (env :dev) (p/cache-off!))

(defn layout [m]
  (p/render-file "layout.html" m))

(defn content-map
  "Helper method for developer convinience. Instead of writing :key
   (contents :key), this method does it automatically."
  [ks custom]
  (merge (defaults) (zipmap ks (map contents ks)) custom))

(defn home []
  (p/render-file
    "pages/index.html"
    (content-map [:hero-headline
                  :hero-cta
                  :hero-title
                  :footer-description
                  :footer-title]
                 {:title (contents :index-title)
                  :desc  (contents :index-desc)
                  :img   (contents :index-img)
                  :url   (contents :base-url)
                  :body-classes "homepage"})))
