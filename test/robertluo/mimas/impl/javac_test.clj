(ns robertluo.mimas.impl.javac-test
  (:require [robertluo.mimas.impl.javac :as sut]
            [clojure.test :refer [deftest use-fixtures is testing]]
            [clojure.spec.test.alpha :as stest]))

(use-fixtures :once (fn [f] (stest/instrument) (f) (stest/unstrument)))

(deftest source-filesnames
  (is (= #{"Wrong.java" "Hello.java"}
         (->> (sut/source-filenames ["sample"])
              (map #(last (.split % "/")))
              (set)))))

(deftest test-command-line
  (testing "command-line can assemble options"
    (is (= ["-cp" "lib.jar" "-d" "target/classes"]
           (sut/command-line "lib.jar" "target/classes" nil)))))

(deftest test-run!
  (is (= #:javac{:result true}
         (sut/run! ["sample/src/java/Hello.java"] nil)))
  (is (= #:javac{:result false}
         (-> (sut/run! ["sample/src/java/Wrong.java"] nil)
             (select-keys [:javac/result])))))
