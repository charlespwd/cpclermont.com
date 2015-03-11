(ns cpclermont.db
  (:require [markdown.core :as md]
            [clj-yaml.core :as yaml]
            [clojure.string :as str]
            [clojure.java.io :as io]))

(def ^:dynamic *posts-directory* (io/file "resources/articles/"))
(def ^:dynamic *static-directory* (io/file "resources/static/"))
(def ^:private post-publish-time "T12:00:00.0-05:00")

;;; Util

(defn- fetch-files
  [directory]
  ;; the first one is the *posts-directory* itself
  (drop 1 (file-seq directory)))

(defn- split-yaml+md [yaml+md]
  (if yaml+md
    (let [yaml (second (re-find #"^---([\S\s]*)---" yaml+md)) ; similar to jekyllrb
          md   (str/replace yaml+md #"^---[\S\s]*---" "")] ; remove the yaml front-matter
      [yaml md])))

(defn parse [yaml+md]
  (if-let [[yaml md] (split-yaml+md yaml+md)]
    (merge
      (yaml/parse-string yaml)
      {:content (md/md-to-html-string md :heading-anchors true
                                         :reference-links? true)})))

(defn- merge-with-content [{:keys [file] :as page}]
  (if file
    (let [content-map (parse (slurp file))]
      (merge page content-map))))

(defn- merge-with-date-and-id [{:keys [filename] :as post}]
  (let [matches (re-find #"^(\d{4}-\d{2}-\d{2})-([^.]*)\.(.*)$" filename)
        date    (nth matches 1 nil)
        id      (nth matches 2 nil)]
    (if id
      (merge post
             {:id id
              :date (clojure.instant/read-instant-date (str date post-publish-time))}))))

(defn- merge-with-id [{:keys [filename] :as page}]
  (let [matches (re-find #"([^.]*)\.(md|markdown)" filename)
        id      (nth matches 1 nil)]
    (if id (merge page {:id id}))))

(defrecord Page [file filename])

(defn- file->page [file]
  (->Page file (.getName file)))

;;; Pages

(defn pages
  "Return a seq of pages"
  ([] (pages *static-directory*))
  ([directory]
    (->> (fetch-files directory)
         (map file->page)
         (map merge-with-id)
         (map merge-with-content))))

(defn page
  "Return a single page"
  ([id] (page id *static-directory*))
  ([id directory]
   (->> (pages directory) ; HACK
        (filter (comp (partial = id) :id))
        (first))))

;;; Posts

(defn posts
  "Return a seq of posts."
  ([] (posts *posts-directory*))
  ([directory]
   (->> (fetch-files directory)
        (map file->page)
        (map merge-with-date-and-id)
        (map merge-with-content)
        (sort-by :date)
        (reverse)))) ; recent first

(defn post
  "Return a single post."
  ([id] (post id *posts-directory*))
  ([id directory]
   (->> (posts directory) ; HACK
        (filter (comp (partial = id) :id))
        (first))))
