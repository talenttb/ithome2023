;; (ns build
;;   (:require [clojure.tools.build.api :as b]
;;             [org.corfield.build :as bb]))
;; (def lib 'ithome/app)
;; (def version (b/git-process {:git-args "describe --tags --long --always --dirty"}))
;; (def basis (b/create-basis {:project "deps.edn"}))

;; (defn clean [opts]
;;   (bb/clean opts))

;; (def class-dir "target/classes")
;; (defn default-jar-name [{:keys [version] :or {version version}}]
;;   (format "target/%s-app.jar" version))

;; (defn uberjar [{:keys [jar-name version optimize debug verbose]
;;                 :or   {version version, optimize true, debug false, verbose false}}]
;;   (println "Cleaning up before build")
;;   (clean nil)

;;   (println "Bundling sources")
;;   (b/copy-dir {:src-dirs   ["src"]
;;                :target-dir class-dir})

;;   (println "Compiling server. Version:" version)
;;   (b/compile-clj {:basis      basis
;;                   :src-dirs   ["src"]
;;                   :class-dir  class-dir})

;;   (println "Building uberjar")
;;   (b/uber {:class-dir class-dir
;;            :uber-file (str (or jar-name (default-jar-name {:version version})))
;;            :basis     basis
;;            :main      'user}))


(ns build
  (:require [clojure.tools.build.api :as b]))

(def class-dir "target/classes")
(def basis (b/create-basis {:project "deps.edn"}))
(def uber-file "target/app.jar")

(defn clean [_]
  (b/delete {:path "target"}))

(defn uber [_]
  (clean nil)
  (b/copy-dir {:src-dirs ["src" "resources"]
               :target-dir class-dir})
  (prn "Basis: " basis)
  (b/compile-clj {:basis basis
                  :src-dirs ["src"]
                  :class-dir class-dir})
  (b/uber {:class-dir class-dir
           :uber-file uber-file
           :basis basis
           :main 'user}))

