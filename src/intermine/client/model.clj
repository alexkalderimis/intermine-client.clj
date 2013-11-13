(ns intermine.client.model
  (:use intermine.client.futures
        intermine.client.transport))

(declare coalesce-fields)

(def models (atom {}))
(def model-requests (atom 0))

(defn drop-cache [] (reset! models {}))

(defn get-model [service]
  "Return the model that describes the schema of this service"
  (if-let [m (find @models service)]
    (val m) ;; in cache - return it
    (do
      (swap! model-requests inc) ;; Record that we made a request
      (let [model (-> service (get-in-json "/model" :model) coalesce-fields)]
        (swap! models assoc service model) ;; store in the cache 
        model))))

(defn get-class-def [m n] (get-in m [:classes (keyword n)]))

(defn- coalesce-fields [model]
  (let [classes (:classes model)
        class-names (keys classes)
        combine-fields (fn [cds name] (assoc-in cds [name :fields]
          (apply merge (map (cds name) [:attributes :references :collections]))))
        ]
    (assoc model :classes (reduce combine-fields classes class-names))))

(defn- invalid-path [state part]
  (if (:valid state)
    {:valid false
     :parsed (clojure.string/join "." (map :name (:defs state)))
     :failed-on part}
    state))

(defn- valid-path
  ([state next-def] (valid-path state next-def {}))
  ([state next-def subclasses] 
    (let [defs      (conj (:defs state) next-def)
          path-key  (clojure.string/join "." (map :name defs))
          leaf      (not (nil? (:type next-def)))
          next-type (or (subclasses path-key)
                        (some next-def [:type :referencedType :name]))]
      {:valid true :type next-type :defs defs :leaf leaf})))

(defn- resolve-path [model subclasses state part]
  (let [cd (get-class-def model (:type state))
        next-def (get-in cd [:fields part])]
    (if next-def
      (valid-path state next-def subclasses)
      (invalid-path state part))))

(defn- make-root-path [model class-name]
  (if-let [class-def (get-class-def model class-name)]
    (valid-path {:defs [] :leaf false} class-def)
    {:valid false :failed-on class-name}))

(defn make-path
  ( [model path-string] (make-path model path-string {}) )
  ( [model path-string subclasses]
    (let [[root & parts] (map keyword (.split path-string "\\."))
          initial-state (make-root-path model root)]
      (reduce (partial resolve-path model subclasses) initial-state parts))))

