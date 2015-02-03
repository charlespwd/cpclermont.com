(ns cpclermont.views.views
  (:require [selmer.parser :as selmer]
            [cpclermont.views.content :refer [contents]]
            [environ.core :refer [env]]))

(selmer/set-resource-path!
  (clojure.java.io/resource "templates"))

(when (env :dev) (selmer/cache-off!))

(def defaults [:footer-description
               :footer-title
               :jquery
               :livereload])

(defn content-map
  "Helper method for developer convenience. Instead of writing :key
   (contents :key), this method does it automatically."
  ([ks m]
   (let [d+ks (concat defaults ks)]
     (merge (zipmap d+ks (map contents d+ks)) (content-map m))))
  ([m]
   (zipmap (keys m) (map #(if (keyword? %) (contents %) %) (vals m)))))

(defn posts
  ([articles]
   (selmer/render-file
     "pages/blog-home.html"
     (content-map [:mailchimp-cta
                   :mailchimp-strong
                   :mailchimp-subject]
                  {:title        :blog-title
                   :desc         :blog-desc
                   :img          :blog-img
                   :url          :base-url
                   :posts        articles
                   :body-classes "no-sidebar"}))))

(defn post
  ([article]
   (selmer/render-file
     "pages/article.html"
     (content-map [:mailchimp-cta
                   :mailchimp-strong
                   :mailchimp-subject]
                  (merge article {:body-classses "no-sidebar"})))))

(defn home []
  (selmer/render-file
    "pages/index.html"
    (content-map [:hero-headline
                  :hero-cta
                  :hero-title]
                 {:title :index-title
                  :desc  :index-desc
                  :img   :index-img
                  :url   :base-url
                  :body-classes "homepage"})))
