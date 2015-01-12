(defproject cpclermont "1.0.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://cpclermont.herokuapp.com"
  :license {:name "MIT"
            :url "http://example.com/FIXME"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [compojure "1.1.8"]
                 [selmer "0.7.8"]
                 [ring/ring-jetty-adapter "1.2.2"]
                 [ring/ring-devel "1.2.2"]
                 [ring-basic-authentication "1.0.5"]
                 [environ "0.5.0"]
                 [com.cemerick/drawbridge "0.0.6"]]
  :min-lein-version "2.0.0"
  :plugins [[environ/environ.lein "0.2.1"]]
  :hooks [environ.leiningen.hooks]
  :java-agents [[com.newrelic.agent.java/newrelic-agent "3.12.1"]]
  :profiles {:dev         {:jvm-opts ["-Dnewrelic.environment=development"]}
             :staging     {:jvm-opts ["-Dnewrelic.environment=staging"]}
             :production  {:jvm-opts ["-Dnewrelic.environment=production"]}})
