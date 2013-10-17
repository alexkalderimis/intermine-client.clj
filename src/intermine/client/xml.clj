(ns intermine.client.xml
  (use [clojure.string :only (join)]
       [clojure.data.xml :only (emit-str sexp-as-element)]
       intermine.client.model))

(defn emrooten [root path]
  (if (.startsWith path root) path (str root "." path)))

(defn expandRefs [model path-str]
  (let [path (make-path model path-str)]
    (if (:leaf path)
      path
      (let [cd (get-class-def model (:type path))]
        (map #(str path-str "." %) (map name (keys (:attributes cd))))))))

(defn opify [op]
  (-> op name .toUpperCase (.replaceAll "-" " ")))

(defn constraint [model root clause]
  (if (= 2 (count clause))
    (if (#{:lookup :in :not-in} (first clause))
      (constraint model root (concat [root] clause))
      (let [[path value] clause
            op (if (seq? value) :one-of :=)]
        (constraint model root [path op value])))
    (let [[path op value] clause]
      {:path (emrooten root (str path)) :op (opify op) :value value})))

(defn to-sexpr [model cols wheres]
    [:query {:model (:name model) :view (join " " cols)}
      (map (partial conj [:constraint]) wheres)])

(defn query-xml [model query]
  (let [cols (query :select) 
        root (or (:root query) (-> cols first (.split "\\.") first))
        col-paths (->> cols
                       (map (partial emrooten root))
                       (map (partial expandRefs model))
                       flatten)
        where (query :where)
        where-recs (map #(constraint model root %) where)
        xml-expr (to-sexpr model col-paths where-recs)]
    (emit-str (sexp-as-element xml-expr))))
