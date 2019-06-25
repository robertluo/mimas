(ns robertluo.mimas.impl.javac
  (:require
   [clojure.java.io :as io]
   [clojure.spec.alpha :as s]
   [badigeon.javac :as javac])
  (:import
   [javax.tools ToolProvider]
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
  [class-path target-path filenames opts]
  (->> (map str filenames)
       (into `[~@(when class-path ["-cp" class-path]) ~@opts "-d" ~target-path])))

(defn assoc-if
  "assoc k, v to map m if (pred v) is true, else return m"
  ([m k v]
   (assoc-if m k v seq))
  ([m k v pred]
   (if (pred v)
     (assoc m k v)
     m)))

(defn run-compile
  "call java compiler with command-line"
  [command-line]
  (let [compiler (ToolProvider/getSystemJavaCompiler)
        empty-out #(ByteArrayOutputStream.)
        out (empty-out)
        err (empty-out)]
    (.run compiler nil out err (into-array String command-line))
    (-> nil
        (assoc-if :javac/out (str out))
        (assoc-if :javac/err (str err)))))

(s/def ::javac/out string?)
(s/def ::javac/err string?)
(s/def ::result (s/keys :opt [::javac/out ::javac/err]))

(s/fdef run-compile
  :args (s/cat :command-line ::command-line)
  :ret ::result)

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (-> (command "lib" "/tmp" (source-filenames ["/tmp"]) nil)
      (run-compile))
  )
