(ns intermine.client.test.examples
  (:import java.util.Calendar)
  (:require [intermine.client :as c]
            [intermine.client.model :as m]))

(use 'clojure.test)

(declare service query attr-query)

(defn date->year [date]
  (-> (doto (Calendar/getInstance) (.setTime date)) (.get Calendar/YEAR)))

(def is-valid-status #{:CURRENT :TO_UPGRADE})
(def get-year (comp date->year :dateCreated))

(defn find-where [where xs]
  (let [f #(= where (select-keys % (keys where)))]
    (->> xs (filter f) first)))

;; Testing that we are caching the model appropriately
(defn report-model-requests [f]
  (let [requests-before @m/model-requests]
    (f)
    (is 1 (- @m/model-requests requests-before))))

(use-fixtures :once report-model-requests)

(def service {:base "http://www.flymine.org/query/service"})
(def query   {:select ["Gene.transcripts"]
              :where  [[:lookup "forkhead"]]})
(def attr-query { :select ["Gene.transcripts.primaryIdentifier"]
                  :where  [[:lookup "forkhead"]]} )
(def transcript-id "FBtr0085321")
(def transcript {:class "MRNA" :value transcript-id })
(def pl3 "PL classIIIc")

(deftest queries
  (testing "Counting"
    (is (= 4 (c/count service query))))
  (testing "Records"
    (let [[forkhead] (c/records service query)]
          (is (= transcript-id
                  (-> forkhead
                      :transcripts first :primaryIdentifier)))))
  (testing "Table"
    (let [[[cell]] (intermine.client/table service attr-query)]
      (is (= transcript (select-keys cell (keys transcript))))))
  (testing "Rows"
    (let [[[primId] [sndPrimId]] (c/rows service attr-query)
          [[sndPrimId']] (c/rows service attr-query :offset 1)]
      (is (= sndPrimId sndPrimId'))
      (is (= transcript-id  primId))
      (is (not (= transcript-id sndPrimId')))))
  (testing "values"
    (is (= transcript-id (first (c/values service attr-query :limit 1)))))
  (testing "maps"
    (let [[row] (c/maps service attr-query :limit 1)]
      (is (= transcript-id (row :Gene.transcripts.primaryIdentifier))))))

(deftest lists
  (let [lists (c/lists service)]
      (is (> (count lists) 20))
      (is (every? (comp is-valid-status :status) lists))
      (is (= 2008 (get-year (find-where {:name pl3} lists))))))


