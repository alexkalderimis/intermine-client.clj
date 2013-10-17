(ns intermine.client.transport
  (:import [java.io IOException])
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

(defn- check-for-error [result]
  (if (not (:error result))
    result
    (throw (IOException. (apply format "[ERROR: %s] %s"
                            (map result [:statusCode :error]))))))

(defn get-in-json [service path key & {:keys [params] :as options}]
  (future (with-open [client (http/create-client)]
    (let [uri (str (:base service) path)
          query (merge {:token (:token service)} params)
          mimetype (str "application/json"
                        (if-let [t (:type options)] (str ";type=" t) ""))
          headers {:Accept mimetype}]
      (-> client
          (http/POST uri :query query :headers headers)
          http/await
          http/string
          (json/read-str :key-fn keyword)
          check-for-error
          key)))))

