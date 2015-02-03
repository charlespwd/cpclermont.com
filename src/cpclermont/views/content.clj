(ns cpclermont.views.content
  (:require [environ.core :refer [env]]))

(defn contents [k]
  ({:base-url "http://cpclermont.com"
    :blog-title "Blog"
    :blog-desc "Charles-Philippe Clermont blogs about software, business, marketing and life in general."
    :footer-description "I love to talk about software and selectively do consulting work"
    :footer-title "Let's get in touch!"
    :hero-description "I love to talk about software and selectively do consulting work."
    :hero-headline "I make, ship, write, and help businesses convert users."
    :hero-cta "Start"
    :hero-title "Hi, I'm Charles"
    :index-desc "Charles-Philippe Clermont is a marketer-programmer unicorn."
    :index-img "http://cpclermont.com/public/img/cpclermont.jpg"
    :index-site-name "CP Clermont"
    :index-title "CP Clermont"
    :mailchimp-subject "Grow your business."
    :mailchimp-strong "Receive my best advice every Wednesday."
    :mailchimp-cta "Send me the good stuff!"
    :tag-line "Engineering marketing solutions"
    :jquery (if (env :production) "//code.jquery.com/jquery-1.11.1.min.js" "/js/jquery.min.js")
    :livereload (if-not (env :production) "<script src=\"//localhost:35729/livereload.js\"></script>")}
   k))
