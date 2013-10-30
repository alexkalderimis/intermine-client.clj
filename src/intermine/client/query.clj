(ns intermine.client.query)

(defn from [root] {:root root})

(defn select [q & columns]
  (assoc q :select columns))

(defn also-select [q & columns]
  (let [select-list (get q :select [])]
    (assoc q :select (vec (concat select-list columns)))))

(defn order-by
  ([q path] (order-by q path :asc))
  ([q path0 & args] (assoc q :sort-order (into [path0] args))))

(defn with-optional [q & paths]
  (let [joins (get q :outer-joins [])]
    (assoc q :outer-joins (into joins paths))))

(defn where [q & args]
  (let [wh-clauses (get q :where [])]
    (assoc q :where (conj wh-clauses args))))
