(ns robertluo.mimas.impl
  "A minimum build library implementation.

  Cores concept here is task and building context.
  building context is map contains all information a task need to know,
  a task will be called with a context."
  (:require
   [robertluo.mimas.impl.javac :as javac]
   [clojure.tools.deps.alpha :as deps]
   [cloverage.coverage :as cov]))

(defn context-return
  "Turn function f into context returning function"
  [f]
  (fn [context]
    (let [rst (f context)]
      (if (map? rst)
        (merge context rst)
        context))))

(defn read-edn
  [filename]
  (try
    (some-> (slurp filename) (clojure.edn/read-string))
    (catch java.io.IOException _ nil)))

(defn project
  "read project's meta information into context"
  ([context]
   (project "project.edn" context))
  ([project-file _]
   (when-let [meta (read-edn project-file)]
     {:project/meta meta})))



(defn coverage
  "Run test coverage using cloverage"
  [{:keys [paths]}]
  (binding [cov/*exit-after-test* false]
    (apply cov/-main
           "-e" ""
           "-s" "test"
           (interleave (repeat "-p") paths))))

(defn class-path
  [context]
  (-> context
      (update :mvn/repos merge clojure.tools.deps.alpha.util.maven/standard-repos)
      (deps/resolve-deps {:verbose true})
      (deps/make-classpath nil nil)))

(defn javac
  "Compile java source files"
  [{:javac/keys [source-paths classes-path opts] :as context
    :or         {source-paths ["src/main/java" "src/java"]
                 classes-path "target/classes"}}]
  (let [cmd          (javac/command-line (class-path context) classes-path opts)
        source-files (javac/source-filenames source-paths)
        compile-rslt (javac/run! source-files cmd)]
    (if (:javac/result compile-rslt)
      (assoc compile-rslt :javac/target class-path)
      (throw (ex-info "Javac failed" compile-rslt)))))

(defn build
  ([tasks tasknames]
   (->> tasknames
        (map tasks)
        (map context-return)
        (reduce #(%2 %) (read-edn "deps.edn")))))

(comment
  (class-path (read-edn "deps.edn"))
  )
