(ns robertluo.mimas.impl.javac
  (:refer-clojure :exclude [run!])
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [badigeon.javac :as javac])
  (:import
   [javax.tools ToolProvider DiagnosticCollector JavaCompiler StandardJavaFileManager
    Diagnostic$Kind Diagnostic]
   [java.io ByteArrayOutputStream]))

(defn source-file?
  [s]
  (clojure.string/ends-with? s ".java"))

(defn source-filenames
  "return source file names with paths"
  [paths]
  (->> (map io/file paths)
       (map file-seq)
       (apply concat)
       (filter #(.isFile ^java.io.File %))
       (map #(.getAbsolutePath ^java.io.File %))
       (filter source-file?)))

(s/def ::command-line (s/coll-of string?))

(s/fdef source-filenames
  :args (s/cat :paths (s/coll-of string?))
  :ret ::command-line)

(defn command-line
  "return jdk command line without command itself"
  [class-path target-path opts]
  `[~@(when class-path ["-cp" class-path]) ~@opts "-d" ~target-path])

(defn assoc-if
  "assoc k, v to map m if (pred v) is true, else return m"
  ([m k v]
   (assoc-if m k v seq))
  ([m k v pred]
   (if (pred v)
     (assoc m k v)
     m)))

(defn diagnostics
  "return diagnostics map of dc"
  [^DiagnosticCollector dc]
  (for [^Diagnostic diags (.getDiagnostics dc)]
    {:code          (.getCode diags)
     :column-number (.getColumnNumber diags)
     :line-number   (.getLineNumber diags)
     :source        (.getSource diags)
     :start-pos     (.getStartPosition diags)
     :position      (.getPosition diags)
     :message       (.getMessage diags (java.util.Locale/getDefault))
     :kind          (condp = (.getKind diags)
                      Diagnostic$Kind/ERROR             :error
                      Diagnostic$Kind/WARNING           :warning
                      Diagnostic$Kind/NOTE              :note
                      Diagnostic$Kind/OTHER             :other
                      Diagnostic$Kind/MANDATORY_WARNING :mandatory-warning
                      :other)}))

(s/def ::code string?)
(s/def ::report
  (s/keys :opt-un [::code]))

(defn run!
  "call java compiler with source file names and javac options"
  [source-file-names options]
  (let [^JavaCompiler compiler  (ToolProvider/getSystemJavaCompiler)
        ^DiagnosticCollector dc (DiagnosticCollector.)]
    (with-open [^StandardJavaFileManager file-mng (.getStandardFileManager compiler dc nil nil)]
      (let [^DiagnosticCollector dcc (DiagnosticCollector.)
            cu                       (.getJavaFileObjectsFromStrings file-mng source-file-names)
            task                     (.getTask compiler nil file-mng dcc options nil cu)
            rst                      (.call task)]
        #:javac{:result         rst
                :file-report    (diagnostics dc)
                :compile-report (diagnostics dcc)}))))

(s/def ::javac/result boolean?)
(s/def ::result (s/keys :opt [::javac/result]))

(s/fdef run-compile
  :args (s/cat :command-line ::command-line)
  :ret ::result)

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (run! (source-filenames ["sample/src/java"]) (command-line nil "/tmp" nil))
  )
