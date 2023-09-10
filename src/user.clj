(ns user
  (:require [ring.adapter.jetty :as jetty]
            [java-time.api :as t]
            [clojure.edn :as edn]
            [clojure.tools.logging :as log]
            [ring.middleware.reload :refer [wrap-reload]])
  (:gen-class))

(defn date-str
  []
  (t/format
   (t/with-zone (t/formatter "yyyy-MM-dd") (t/zone-id))
   (t/instant)))

(defn graceful-restart [jetty]
  (.setStopTimeout jetty 10000)
  (.setStopAtShutdown jetty true))

(defn app [_req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World 9898"})

(defonce server (atom nil))

(defn start-server
  [dev? port]
  (let [app_hanlder (if dev?
                      (do
                        (log/info "enable auto-reloading in dev enviroment")
                        (wrap-reload #'app))
                      app)]
    (log/info "Start server on " port)
;;   https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/src/ring/adapter/jetty.clj
    (reset! server (jetty/run-jetty app_hanlder
                                    {:port port
                                     :join? false
                                     :configurator graceful-restart}))))

(defn stop-server
  []
  (when-not (nil? @server)
    (do
      (.stop @server)
      (reset! server nil))))
