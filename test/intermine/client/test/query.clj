(ns intermine.client.test.query
  (:use clojure.test)
  (:require [intermine.client.query :as q]))

(deftest where
  (testing "Adding a simple attribute contraint"
    (let [q {:select ["Gene"]}
          expected {:select ["Gene"]
                    :where [["symbol" := "eve"]]}]
      (is (= expected (-> q (q/where "symbol" := "eve")))))))

(deftest from
  (testing "Start a query with from"
    (is (= {:root "Gene"} (q/from "Gene")))))

(deftest select
  (testing "Selecting columns"
    (is (= {:root "Gene" :select ["symbol" "length"]}
           (q/select {:root "Gene"} "symbol" "length")))))

(deftest compositions
  (testing "Building a query using function"
    (let [pid  "primaryIdentifier"
          exp {:root "Gene"
               :select [["primaryIdentifier"]]
               :where [[:lookup "eve"]]}]
      (= exp (-> (q/from "Gene") (q/select pid) (q/where :lookup "eve"))))))

