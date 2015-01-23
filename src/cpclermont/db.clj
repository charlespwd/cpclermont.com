(ns cpclermont.db
  (:require [markdown.core :as md]
            [clojure.string :as str]
            [clj-yaml.core :as yml]
            [clojure.java.io :as io]))

(defn article-file [id]
  (io/as-file (str "resources/articles/" id ".md")))

(defn exists? [id]
  (.exists (article-file id)))

(defn fetch [id]
  (let [file (article-file id)]
    (if (.exists file)
      (slurp file)
      nil)))

(defn split-yaml+md [yaml+md]
  (when yaml+md
    (let [yaml (second (re-find #"^---([\S\s]*)---" yaml+md)) ; similar to jekyllrb
          md   (str/replace yaml+md #"^---[\S\s]*---" "")] ; remove the yaml front-matter
      [yaml md])))

(defn parse [yaml+md]
  (when-let [[yaml md] (split-yaml+md yaml+md)]
    (merge
      (yml/parse-string yaml)
      {:content (md/md-to-html-string md
                                      :heading-anchors true
                                      :reference-links? true)})))

(defn article
  ([id]
   (parse (fetch id))))

#_(split-yaml+md (fetch "foo"))
#_(clojure.pprint/pprint (article "foo"))
#_(article "bar")
