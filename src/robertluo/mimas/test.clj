(ns robertluo.mimas.test
  "Test task"
  (:require
   [eftest.runner :refer [run-tests find-tests]]
   [cloverage.coverage :as cov]))

(defn coverage
  "Run test coverage using cloverage"
  [{:keys [paths]}]
  (binding [cov/*exit-after-test* false]
    (apply cov/-main
           "-e" ""
           "-s" "test"
           (interleave (repeat "-p") paths))))

(defn task
  "Run tests of project using eftest"
  [{:test/keys [dir multithread?] :or {dir "test" multithread? false}}]
  (let [summary (run-tests (find-tests dir) {:multithread? multithread?})]
    (when-not (zero? (+ (:fail summary) (:error summary)))
      (throw (ex-info "Tests failed" {:reason summary})))
    {:test/summary summary}))
