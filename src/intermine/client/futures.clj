(ns intermine.client.futures)

(defn- anon-sym [_] (gensym))

(defmacro eventually [bindings & body]
  (let [lefts (->> bindings (partition 2) (map first))
        rights (->> bindings (partition 2) (map last))
        gensyms (map anon-sym rights)
        dereffed (map #(list 'deref %) gensyms)
        rebound (into [] (interleave gensyms rights))
        blocking-bindings (into [] (interleave lefts dereffed))]
    `(let ~rebound
       (future ;; Don't return nested futures - that is bad
         (let [ret# (let ~blocking-bindings ~@body)]
           (if (future? ret#) (deref ret#) ret#))))))

