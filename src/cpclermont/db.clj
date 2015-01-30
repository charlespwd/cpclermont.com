(ns cpclermont.db
  (:require [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def directory (io/file "resources/articles/"))

(defn- fetch-multiple
  ([]
   ;; the first one is the directory itself
   (drop 1 (file-seq directory)))
  ([n]
   (take n (fetch-multiple))))

(defn- split-yaml+md [yaml+md]
  (when yaml+md
    (let [yaml (second (re-find #"^---([\S\s]*)---" yaml+md)) ; similar to jekyllrb
          md   (str/replace yaml+md #"^---[\S\s]*---" "")] ; remove the yaml front-matter
      [yaml md])))

(defn- parse [yaml+md]
  (when-let [[yaml md] (split-yaml+md yaml+md)]
    (merge
      (yaml/parse-string yaml)
      {:content (md/md-to-html-string md
                                      :heading-anchors true
                                      :reference-links? true)})))

(def posts (reverse (map (comp parse slurp) (fetch-multiple))))

(defn post
  ([id]
   (first (filter (comp (partial = id) :id) posts))))

#_(def article-name "todo-or-how-to-start-blogging")
#_(split-yaml+md (fetch article-name))
#_(clojure.pprint/pprint (post article-name))
#_(post "bar")
