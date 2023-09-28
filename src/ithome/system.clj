(ns ithome.system
  (:require [ring.adapter.jetty :as jetty]
            [ithome.db :as db]
            [ithome.router :as router]
            [reitit.ring :as ring]
            [nrepl.server]
            [ring.middleware.reload :refer [wrap-reload]]
            [reitit.core :as r])
  (:gen-class))

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

(defonce nrepl (nrepl.server/start-server :port 19999 :bind "0.0.0.0"))

(defn start-server
  [dev? port]
  (let [app_hanlder (if dev?
                      (do
                        (prn "enable auto-reloading in dev enviroment")
                        (wrap-reload #'app))
                      app)]
    (prn "Start nrepl on 19999")
    (prn "Start server on " port)
    (prn "Init db")
    (db/init-table)

;;   https://github.com/ring-clojure/ring/blob/master/ring-jetty-adapter/src/ring/adapter/jetty.clj
    (reset! server (jetty/run-jetty app_hanlder
                                    {:port port
                                     :join? false
                                     :configurator graceful-restart}))))

(defn stop-server
  []
  (when-not (nil? @server)
    (prn "Stop server")
    (.stop @server)
    (nrepl.server/stop-server nrepl)
    (reset! server nil)))

(defn -main
  [& _args]
  (start-server true 7777))
