(ns mimas
  (:refer-clojure :exclude [test])
  (:require
   [clojure.tools.namespace.repl :as repl]
   [eftest.runner :refer [run-tests find-tests]]
   [cloverage.coverage :as cov]
   [badigeon.clean :as clean]
   [badigeon.bundle :as bundle]
   [badigeon.jar :as jar]
   [clojure.tools.deps.alpha.reader :as deps]
   [clojure.string :as str]))

(def project
  "项目建造信息"
  (-> (slurp "project.edn") (clojure.edn/read-string)))

(def refresh repl/refresh)

(defn test []
  (let [summary (run-tests (find-tests "test") {:multithread? false})]
    (when-not (zero? (+ (:fail summary) (:error summary)))
      (throw (ex-info "Tests failed" {:reason summary})))))

(defn coverage []
  (binding [cov/*exit-after-test* false]
    (cov/-main
     "-p" "src"
     "-e" ""
     "-s" "test")))

(def tasks
  {:test     #'test
   :coverage #'coverage})

;; entry point
(defn -main [& tasknames]
  (try
    (doseq [task-name tasknames]
      (if-let [task (get tasks (keyword task-name))]
        (task)
        (throw (ex-info "Invalid task name" {:task/name task-name}))))
    (System/exit 0)
    (catch clojure.lang.ExceptionInfo e
      (System/exit (:exit-code (ex-data e) 1)))))
