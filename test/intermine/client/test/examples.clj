(ns intermine.client.test.examples
  (:require intermine.client))

(use 'clojure.test)

(deftest queries
  (let [service {:base "http://www.flymine.org/query/service"}
        query   { :select ["Gene.transcripts"]
                   :where  [[:lookup "forkhead"]]}]
    (testing "Counting"
        (is (= 4 @(intermine.client/count service query))))
    (testing "Records"
      (let [ [forkhead] @(intermine.client/records service query)
             primId (-> forkhead :transcripts first :primaryIdentifier) ]
        (is (= "FBtr0085321" primId))))))

  
