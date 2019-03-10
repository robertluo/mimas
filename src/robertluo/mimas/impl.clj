(ns robertluo.mimas.impl
  "A minimum build library implementation.

  Cores concept here is task and building context.
  building context is map contains all information a task need to know,
  a task will be called with a context."
  (:refer-clojure :exclude [test])
  (:require
   [eftest.runner :refer [run-tests find-tests]]
   [cloverage.coverage :as cov]
   [badigeon.clean :as clean]
   [badigeon.bundle :as bundle]
   [badigeon.jar :as jar]
   [clojure.tools.deps.alpha.reader :as deps]
   [clojure.string :as str]))

(defn context-return
  "Turn function f into context returning function"
  [f]
  (fn [context]
    (let [rst (f context)]
      (if (nil? rst)
        context
        (merge context rst)))))

(defn project
  "read project's meta information into context"
  ([context]
   (project "project.edn" context))
  ([project-file context]
   (let [meta (try
                (some-> (slurp project-file) (clojure.edn/read-string))
                (catch java.io.IOException _ nil))]
     (when meta
       {:project/meta meta}))))

(defn test
  "Run tests of project using eftest"
  [{:test/keys [dir multithread?] :or {dir "test" multithread? false} :as context}]
  (let [summary (run-tests (find-tests dir) {:multithread? multithread?})]
    (when-not (zero? (+ (:fail summary) (:error summary)))
      (throw (ex-info "Tests failed" {:reason summary})))
    {:test/summary summary}))

(defn coverage
  "Run test coverage using cloverage"
  [{:keys [paths] :as context}]
  (binding [cov/*exit-after-test* false]
    (apply cov/-main
           "-e" ""
           "-s" "test"
           (interleave (repeat "-p") paths))))

(defn build
  ([tasks tasknames]
   (->> tasknames
        (map tasks)
        (map context-return)
        (reduce #(%2 %) {}))))
