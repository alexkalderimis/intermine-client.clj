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

(deftest also-select
  (testing "Adding columns to the select list"
    (is (= {:select ["Gene.name" "symbol"]}
           (q/also-select {:select ["Gene.name"]} "symbol")))
    (is (= {:root "Gene" :select ["symbol" "name"]}
           (q/also-select {:root "Gene" :select ["symbol"]} "name")))
    (is (= {:root "Gene" :select ["symbol" "name"]}
           (q/also-select {:root "Gene"} "symbol" "name")))))

(deftest order-by
  (testing "Adding a sort order"
    (is (= {:sort-order ["length" :asc]}
           (q/order-by {} "length" :asc)))
    (is (= {:sort-order ["length" :asc]}
           (q/order-by {} "length")))
    (is (= {:sort-order ["length" :asc "symbol" :desc]}
           (q/order-by {} "length" :asc "symbol" :desc)))))

(deftest with-optional
  (testing "Adding an outer join for optional attributes"
    (is (= {:outer-joins ["proteins" "organism"]}
           (q/with-optional {:outer-joins ["proteins"]} "organism")))
    (is (= {:outer-joins ["proteins" "organism" "location"]}
           (q/with-optional {:outer-joins ["proteins"]}
                            "organism" "location")))
    (is (= {:outer-joins ["proteins"]}
           (q/with-optional {} "proteins")))))

(deftest compositions
  (testing "Building a query using function"
    (let [pid  "primaryIdentifier"
          org  "organism"
          exp {:root "Gene"
               :sort-order ["length" :asc]
               :outer-joins ["organism"]
               :select [["primaryIdentifier" "organism"]]
               :where [[:lookup "eve"]]}]
      (= exp (-> (q/from "Gene")
                 (q/select pid org)
                 (q/with-optional org)
                 (q/where :lookup "eve")
                 (q/order-by "length"))))))
