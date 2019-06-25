(ns foo.bar-test
  (:require
   [clojure.test :refer :all]
   [foo.bar :as sut]))

(deftest test-add
  (is (= 7 (sut/add 3 4))))
