(ns user
  (:require [ring.adapter.jetty :as jetty]
            [java-time.api :as t]
            [ithome.db :as db]
            [ithome.router :as router]
            [clojure.tools.logging :as log]
            [reitit.ring :as ring]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.core :as r])
  (:gen-class))

(defn date-str
  []
  (t/format
   (t/with-zone (t/formatter "yyyy-MM-dd") (t/zone-id))
   (t/instant)))

(defn graceful-restart [jetty]
  (.setStopTimeout jetty 10000)
  (.setStopAtShutdown jetty true))

(def app
  (ring/ring-handler
   (ring/router 
    (r/routes router/router))
   (constantly {:status 404, :body ""})))

(comment
  (app {:uri "/invalid"})
  (app {:request-method :get :uri "/ping"})
  (app {:request-method :get :uri "/api/ping"})
  (app {:request-method :get, :uri "/users"})
  (-> router/router
    (r/match-by-name :login.root)
    (r/match->path {:iso "möly"}))
  (r/route-names router/router)
  (r/routes router/router))

(defonce server (atom nil))

(comment
  (start-server true 7777))

(defn start-server
  [dev? port]
  (let [app_hanlder (if dev?
                      (do
                        (log/info "enable auto-reloading in dev enviroment")
                        (wrap-reload #'app))
                      app)]
    (log/info "Start server on " port)
    (log/info "Init db")
    (db/init-table)

;;   https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/src/ring/adapter/jetty.clj
    (reset! server (jetty/run-jetty app_hanlder
                                    {:port port
                                     :join? false
                                     :configurator graceful-restart}))))

(defn stop-server
  []
  (when-not (nil? @server)
    (.stop @server)
    (reset! server nil)))
