(ns robertluo.mimas.impl.jar
  "create a jar file"
  (:refer-clojure :exclude [run!])
  (:import
   [java.nio.file FileSystem FileSystems Files Path Paths StandardCopyOption]
   [java.net URI])
  (:require [clojure.spec.alpha :as s]))

(defn new-jar-fs
  [filename]
  (let [uri (URI/create (str "jar:file:" filename))]
    (FileSystems/newFileSystem uri {"create" "true"})))

(defn mk-opts
  [opts]
  (->> opts
       (map {:replace    StandardCopyOption/REPLACE_EXISTING
             :copy-attrs StandardCopyOption/COPY_ATTRIBUTES
             :atomic     StandardCopyOption/ATOMIC_MOVE})
       (into-array StandardCopyOption)))

(defn empty-strs
  []
  (make-array String 0))

(defn run!
  "create a jar file of filename with files described as in file-desc"
  [filename file-desc options]
  (with-open [fs (new-jar-fs filename)]
    (doseq [[out-name in-name] file-desc
            :let               [out-path (Paths/get out-name empty-strs)
                                in-path (.getPath fs in-name empty-strs)]]
      (when (and out-path in-path)
        (Files/copy out-path in-path (mk-opts options))))))

(s/def ::file-desc
  (s/map-of string? string?))

(s/def ::copy-options
  (s/coll-of #{:replace :copy-attrs :atomic}))

(s/fdef run!
  :args (s/cat :filename string? :file-desc ::file-desc :options ::copy-options))

(comment
  (require '[clojure.spec.test.alpha :as stest])
  (stest/instrument)
  (mk-opts [:replace])
  (def file-desc {"/tmp/foo" "/foo"
                  "/tmp/bar" "/bar"})
  (run! "/tmp/hello.jar" file-desc [:replace])
  )
