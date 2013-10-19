(ns intermine.client
  (:refer-clojure :exclude [count])
  (:use intermine.client.futures
        intermine.client.xml
        intermine.client.model
        intermine.client.transport))

(defprotocol Closeable
  (close [this]))

(defn generate-def [func-name & {:keys [json path res-key] :or {path "results" res-key :results}}]
    `(defn ~func-name
      ~(format "Run a query against a service and return the %s" (or json path))
      [service# query# & {:as opts#}]
      (eventually [model# (get-model service#)]
          (let [xml# (query-xml model# query#)
                path# (str "/query/" ~path)
                page# {:start (:offset opts#) :size (:limit opts#) } 
                params# (merge page# (dissoc opts# :offset :limit) {:query xml#})]
            (get-in-json service# path# ~res-key :type ~json :params params#)))))

(defmacro def-results-fns [& fn-params]
  (conj (for [param-set fn-params] (apply generate-def param-set)) `do))

(def-results-fns
  [rows]
  [count :res-key :count :json "count"]
  [records :res-key :results :json "objects"]
  [table :res-key :results :path "results/tablerows"])
