(ns intermine.client.futures)

(defn- rebind [bindings]
  (->> bindings
       (partition 2)
       (map #(list (gensym (first %)) (last %)))
       (reduce (fn [c [k v]] (conj c k v)) [])))

(defmacro eventually [bindings & body]
  (let [rebound (rebind bindings)
        syms (map first (partition 2 bindings))
        gensyms (map first (partition 2 rebound))
        dereffed (map #(list 'deref %) gensyms)
        blocking-bindings (into [] (interleave syms dereffed))]
    `(let ~rebound
       (future ;; Don't return nested futures - that is bad
         (let [ret# (let ~blocking-bindings ~@body)]
           (if (future? ret#) (deref ret#) ret#))))))

