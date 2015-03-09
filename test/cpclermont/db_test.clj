(ns cpclermont.db-test
  (:require [cpclermont.db :as db]
            [clojure.java.io :as io]
            [clojure.test :refer :all]))

(def ^:dynamic *directory* (io/file "test/fixtures/articles"))

(deftest db
  (let [posts (db/posts *directory*)]
    (testing "Parsing and fetching posts with #post"
      (let [firstpost (db/post "first" *directory*)
            inexistent (db/post "this-file-name-does-not-exist" *directory*)]
        (is (contains? firstpost :id))
        (is (contains? firstpost :content))
        (is (contains? firstpost :title))
        (is (not (nil? (re-find #"^<h1>(.*)</h1><p>(.*)</p>" (:content firstpost)))))
        (is (nil? inexistent))))
    (testing "Fetching multiple articles with #posts"
      (is (= "infinite" (:id (first posts))) "should be ordered by date")
      (is (= "Hello World" (:title (first posts))) "the posts should list titles")
      (is (apply >= (map (comp #(.getTime %) :date) posts)) "should be ordered by in descending order date"))))
