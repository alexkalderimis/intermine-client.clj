(ns intermine.client.test.examples
  (:use intermine.client.futures)
  (:require [intermine.client :as c]
            [intermine.client.model :as m]))

(use 'clojure.test)

(declare service query attr-query)

;; Testing that we are caching the model appropriately
(defn report-model-requests [f]
  (let [requests-before @m/model-requests]
    (f)
    (is 1 (- @m/model-requests requests-before))))

(use-fixtures :once report-model-requests)

(deftest queries
  (let [service {:base "http://www.flymine.org/query/service"}
        query   { :select ["Gene.transcripts"]
                 :where  [[:lookup "forkhead"]]}
        attr-query { :select ["Gene.transcripts.primaryIdentifier"]
                    :where  [[:lookup "forkhead"]]}
        transcript {:class "MRNA" :value "FBtr0085321"}]
    @(eventually [n-transcripts (c/count service query) ;; Run in parallel.
                  [forkhead] (c/records service query)
                  [[cell]] (intermine.client/table service attr-query)
                  [[primId] [sndPrimId]] (c/rows service attr-query)
                  [[sndPrimId']] (c/rows service attr-query :offset 1)]
                 (is (= 4 n-transcripts))
                 (is (= (:value transcript) (-> forkhead
                                                :transcripts first :primaryIdentifier)))
                 (is (= sndPrimId sndPrimId'))
                 (is (= (:value transcript)  primId))
                 (is (not (= (:value transcript) sndPrimId')))
                 (is (= transcript (select-keys cell (keys transcript))))
                 )))
