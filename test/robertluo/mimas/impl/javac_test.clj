(ns robertluo.mimas.impl.javac-test
  (:require [robertluo.mimas.impl.javac :as sut]
            [clojure.test :refer :all]))

(deftest test-command-line
  (testing ""
    (is (= ["-cp" "lib.jar" "-d" "target/classes"]
           (sut/command-line "lib.jar" "target/classes" nil)))))
