(ns cpclermont.views.views
  (:require [selmer.parser :as p]
            [cpclermont.views.content :refer [contents]]
            [environ.core :refer [env]]))

(p/set-resource-path!
  (clojure.java.io/resource "templates"))

(when (env :dev) (p/cache-off!))

(def defaults [:footer-description
               :footer-title
               :jquery
               :livereload])

(defn content-map
  "Helper method for developer convinience. Instead of writing :key
   (contents :key), this method does it automatically."
  ([ks m]
   (let [d+ks (concat defaults ks)]
     (merge (zipmap d+ks (map contents d+ks)) (content-map m))))
  ([m]
   (zipmap (keys m) (map #(if (keyword? %) (contents %) %) (vals m)))))

(defn blog
  ([]
   (p/render-file
     "pages/blog-home.html"
     (content-map []
                  {:title        :blog-title
                   :desc         :blog-desc
                   :img          :blog-img
                   :url          :base-url
                   :body-classes "right-sidebar"})))
  ([article]
   (p/render-file
     "pages/article.html"
     (content-map []
                  (merge article {:body-classses "no-sidebar"})))))

(defn home []
  (p/render-file
    "pages/index.html"
    (content-map [:hero-headline
                  :hero-cta
                  :hero-title]
                 {:title :index-title
                  :desc  :index-desc
                  :img   :index-img
                  :url   :base-url
                  :body-classes "homepage"})))
