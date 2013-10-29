(ns intermine.client.query)

(defn from [root] {:root root})

(defn select [q & columns]
  (assoc q :select columns))

(defn also-select [q & columns]
  (let [select-list (get q :select [])]
    (assoc q :select (vec (concat select-list columns)))))

(defn where [q & args]
  (let [wh-clauses (get q :where [])]
    (assoc q :where (conj wh-clauses args))))
