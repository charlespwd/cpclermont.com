(ns cpclermont.db
  (:require [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def ^:dynamic *directory* (io/file "resources/articles/"))
(def ^:private post-publish-time "T12:00:00.0-05:00")
(def ^:private filename-fmt-re #"^(\d{4}-\d{2}-\d{2})-([^.]*)\.(.*)$") ; 1-date;2-id;3-ext
(def ^:private filename-fmt-date-group 1)
(def ^:private filename-fmt-id-group 2)

(defn- fetch-multiple
  ([]
   ;; the first one is the *directory* itself
   (drop 1 (file-seq *directory*)))
  ([n]
   (take n (fetch-multiple))))

(defn- split-yaml+md [yaml+md]
  (if yaml+md
    (let [yaml (second (re-find #"^---([\S\s]*)---" yaml+md)) ; similar to jekyllrb
          md   (str/replace yaml+md #"^---[\S\s]*---" "")] ; remove the yaml front-matter
      [yaml md])))

(defn- merge-with-date-and-id [{:keys [filename] :as post}]
  (let [matches (re-find filename-fmt-re filename)
        date    (nth matches filename-fmt-date-group nil)
        id      (nth matches filename-fmt-id-group nil)]
    (if id
      (merge post
             {:id id
              :date (clojure.instant/read-instant-date (str date post-publish-time))}))))

(defn parse [yaml+md]
  (if-let [[yaml md] (split-yaml+md yaml+md)]
    (merge
      (yaml/parse-string yaml)
      {:content (md/md-to-html-string md
                                      :heading-anchors true
                                      :reference-links? true)})))

(defn- merge-with-content [{:keys [file] :as post}]
  (if file
    (let [content-map (parse (slurp file))]
      (merge post content-map))))

(defrecord Post [file filename])

(defn- file->post [file]
  (->Post file (.getName file)))

(defn posts
  "Return a seq of posts."
  ([]
   (->> (fetch-multiple)
        (map file->post)
        (map merge-with-date-and-id)
        (map merge-with-content)
        (sort-by :date)
        (reverse))) ; recent first

  ([directory]
   (binding [*directory* directory]
     (posts))))

(defn post
  "Return a single post."
  ([id]
   (post id *directory*))

  ([id directory]
   (->> (posts directory)
        (filter (comp (partial = id) :id))
        (first))))
