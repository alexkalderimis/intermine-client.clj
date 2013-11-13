(ns intermine.client
  (:refer-clojure :exclude [count])
  (:use intermine.client.futures
        intermine.client.xml
        intermine.client.model
        intermine.client.transport))

(defprotocol Closeable
  (close [this]))


(defn generate-def [func-name &
                    {:keys [json path res-key process]
                     :or {path "results"
                          res-key :results
                          process `(constantly identity)}}]
    `(defn ~func-name
      ~(format "Run a query against a service and return the %s" (or json path))
      [service# query# & {:as opts#}]
        (let [model# (get-model service#)
              xml# (query-xml model# query#)
              path# (str "/query/" ~path)
              page# {:start (:offset opts#) :size (:limit opts#) } 
              params# (merge page#
                             (dissoc opts# :offset :limit)
                             {:query xml#})]
              ((~process model# query#) (get-in-json service#
                           path#
                           ~res-key
                           :type ~json
                           :params params#)))))

(defmacro def-results-fns [& fn-params]
  (conj (for [param-set fn-params] (apply generate-def param-set)) `do))

(def-results-fns
  [rows]
  [values :process (constantly first)]
  [maps :process (fn [m q] (->> (canonical m q)
                                :select
                                (map keyword)
                                (partial zipmap)
                                (partial map)))]
  [count :res-key :count :json "count"]
  [records :res-key :results :json "objects"]
  [table :res-key :results :path "results/tablerows"])

(defn row-maps [service query & args]
  (eventually [rs (apply rows (concat [service query] args))]
              (let [f (partial zipmap (map keyword (:select query)))]
                (map f rs))))

(def date-parser (java.text.SimpleDateFormat. "yyyy-MM-dd'T'HH:mm:ssZ"))

(defn process-list [data]
  (-> data
      (update-in [:dateCreated] #(.parse date-parser %))
      (update-in [:status] keyword)
      (update-in [:tags] set)))

(defn lists [service]
  (map process-list (get-in-json service "/lists" :lists :method :GET)))

