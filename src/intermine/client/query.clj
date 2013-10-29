(ns intermine.client.query)

(defn from [root] {:root root})

(defn select [q & columns]
  (assoc q :select columns))

(defn where [q & args]
  (let [wh-clauses (get q :where [])]
    (assoc q :where (conj wh-clauses args))))
