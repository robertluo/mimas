(ns robertluo.mimas
  "A minimum build library"
  (:require
   [robertluo.mimas.impl :as impl]
   [robertluo.mimas.test :as test]))

(def builtin-tasks
  {:project  #'impl/project
   :test     #'test/task
   :javac    #'impl/javac
   :coverage #'test/coverage})

(defn build
  "Run build tasknames (keywords) on task-map, default to builtin-tasks"
  ([tasknames]
   (build builtin-tasks tasknames))
  ([task-map tasknames]
   (impl/build task-map tasknames)))

(defn f->main
  "Return a main function for build function f"
  [f]
  (fn [& args]
    (try
      (->> (map keyword args)
           f)
      (System/exit 0)
      (catch Exception e
        (binding [*out* *err*]
          (println (.getMessage e)))
        (System/exit 1)))))

(def -main (f->main build))

(comment
  (build [:project :test])
  (build [:project :javac])
  )
