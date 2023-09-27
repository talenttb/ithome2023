(ns build
  (:require [clojure.tools.build.api :as b]))

(def lib 'app)
(def main 'ithome.system)
(def class-dir "target/classes")

(defn- uber-opts [opts]
  (assoc opts
         :lib lib 
         :main main
         :uber-file "target/app.jar"
         :basis (b/create-basis {:project "deps.edn"})
         :class-dir class-dir
         :src-dirs ["src"]
         :ns-compile [main]))

(defn uber [opts]
  (println "Cleaning...")
  (b/delete {:path "target"})
  (let [opts (uber-opts opts)]
    (println "Copying files...")
    (b/copy-dir {:src-dirs   ["resources" "src"]
                 :target-dir class-dir})
    (println "Compiling files...")
    (b/compile-clj opts)
    (println "Creating uberjar...")
    (b/uber opts)))
