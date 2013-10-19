# intermine-client

An asynchronous, promise based InterMine client libary for the JVM written
in clojure.

## Use it!

Declare a dependency in `project.clj`

``` clojure
(defproject your-project "1.0.0-SNAPSHOT"
  :description "Your project description"
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [intermine-client "0.0.1"]])
```

Require:

``` clojure
(ns sample (:require intermine.client))
```

Run a query:

``` clojure
;; All results come as instances of clojure.core/future, so 
;; we dereference the call to intermine.client/count
(let [service {:base "http://www.flymine.org/query/service"}
      query   {:select ["Gene.proteins"] :where  [[:lookup "bsk"]]}
      n-proteins @(intermine.client/count service query)]
    (println "Basket has" n-proteins "proteins"))
```

## License

Copyright © 2013 Alex Kalderimis

Distributed under the Eclipse Public License, the same as Clojure.
