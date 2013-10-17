(defproject intermine-client "0.1.0-SNAPSHOT"
  :description "Client library for InterMine API"
  :url "http://github.com/alexkalderimis/intermine-client.clj"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :source-paths ["src"]
  :min-lein-version "2.0.0"
  :dependencies [
    [org.clojure/clojure "1.4.0"]
    [org.slf4j/slf4j-simple "1.7.5"]
    [org.clojure/data.xml "0.0.7"]
    [org.clojure/data.json "0.2.3"]
    [http.async.client "0.5.2"]])
