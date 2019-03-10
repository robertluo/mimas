# A minimum build library for tools.deps

tools.deps is not a build tool itself, but the management of dependencies overlaps to other build tools such as leiningen and boot. Having a build script available is easy for most clojure projects.

There are some wonderful build libraries like [badigeon](https://github.com/EwenG/badigeon/tree/master/src/badigeon), [kaocha](https://github.com/lambdaisland/kaocha), even tools original built for leiningen or boot are also easy to use in this new context (after all, there are just clojure libraries!), like [cloverage](https://github.com/cloverage/cloverage), [eftest](https://github.com/weavejester/eftest). A mini library to organize these libraries together will be handy.

## Usage

Insert these into your `deps.edn`:

```clojure
{:aliases
 {:dev
  {:extra-paths ["dev" "test"]
   :extra-deps
   {:git/url "https://github.com/robertluo/mimas"
    :sha "$RELEASE-SHA-COPY-FROM-UI"}}}}
```

Create a file `build.clj` under `dev` directory as your own build script:

```clojure
(ns build
  (:require [robertluo.mimas :as mimas]))

(def -main (mimas/f->main mimas/build))
```
