(ns intermine.client
  (:refer-clojure :exclude [count])
  (:use intermine.client.futures
        intermine.client.xml
        intermine.client.model
        intermine.client.transport))

(defn count [service query]
  (eventually [model (get-model service)]
      (let [xml (query-xml model query)
            params {:query xml}]
        (get-in-json service "/query/results" :count :type "count" :params params))))

(defn records [service query]
  (eventually [model (get-model service)]
      (let [xml (query-xml model query)
            params {:query xml}]
        (get-in-json service "/query/results" :results :type "objects" :params params))))

