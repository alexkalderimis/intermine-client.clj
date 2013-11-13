(ns intermine.client.transport
  (:refer-clojure :exclude (await))
  (:import [java.io IOException])
  (:use [http.async.client :only (stream-seq create-client await string GET POST)])
  (:require [clojure.data.json :as json]))

(defn get-str-f [uri]
  (future (with-open [client (create-client)]
    (->> uri
         (GET client)
         await
         string))))

(defn- get-json [uri]
  (future (with-open [client (create-client)]
    (-> (GET client uri :headers {:Accept "application/json"})
        await
        string
        (json/read-str :key-fn keyword)))))

(defn- check-for-error [result]
  (if (not (:error result))
    result
    (throw (IOException. (apply format "[ERROR: %s] %s"
                            (map result [:statusCode :error]))))))

;; Memoizing this fn provides caching to the whole system. But dangerous
;; if big queries are made - make caching a parameter on a session connection
;; object.
(defn get-in-json [service path key & {:keys [params] :as options}]
  (with-open [client (create-client)]
    (let [uri (str (:base service) path)
          query (merge (select-keys service [:token]) params)
          mimetype (str "application/json"
                        (if-let [t (:type options)] (str ";type=" t) ""))
          headers {:Accept mimetype}
          meth (cond (= :GET (:method options)) #'GET
                     :default #'POST)]
      (-> client
          (meth uri :query query :headers headers)
          await
          string
          (json/read-str :key-fn keyword)
          check-for-error
          key))))

;; (defn [s]
;;  (cons 

;;(defn lazy-json [uri]
;;  (with-open [client (create-client)]
;;    (let [resp (stream-seq client :get uri)
;;          cnks (string resp)]
;;      (lazy-seq (next-obj cnks)))))

