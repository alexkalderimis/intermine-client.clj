(ns intermine.client.model
  (:require [clojure.data.json :as json]
            [http.async.client :as http]))

(defn- get-str-f [uri]
  (future (with-open [client (http/create-client)]
    (->> uri
         (http/GET client)
         http/await
         http/string))))

(defn- get-json [uri]
  (future (with-open [client (http/create-client)]
    (-> (http/GET client uri :headers {:Accept "application/json"})
        http/await
        http/string
        (json/read-str :key-fn keyword)))))

(defn get-class-def [m n] (get-in m [:classes (keyword n)]))

(defn- coalesce-fields [model]
  (let [classes (:classes model)
        class-names (keys classes)
        combine-fields (fn [cds name] (assoc-in cds [name :fields]
          (apply merge (map (cds name) [:attributes :references :collections]))))
        ]
    (assoc model :classes (reduce combine-fields classes class-names))))

(defn get-model [service]
  (future (-> service
    :base
    (str "/model")
    get-json
    deref
    :model
    coalesce-fields)))

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
          next-type (or (subclasses path-key)
                        (some next-def [:type :referencedType :name]))]
      {:valid true :type next-type :defs defs})))

(defn- resolve-path [model subclasses state part]
  (let [cd (get-class-def model (:type state))
        next-def (get-in cd [:fields part])]
    (if next-def
      (valid-path state next-def subclasses)
      (invalid-path state part))))

(defn- make-root-path [model class-name]
  (if-let [class-def (get-class-def model class-name)]
    (valid-path {:defs []} class-def)
    {:valid false :failed-on class-name}))

(defn make-path
  ( [model path-string] (make-path model path-string {}) )
  ( [model path-string subclasses]
    (let [[root & parts] (map keyword (.split path-string "\\."))
          initial-state (make-root-path model root)]
      (reduce (partial resolve-path model subclasses) initial-state parts))))

