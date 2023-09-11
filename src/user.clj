(ns user
  (:require [ring.adapter.jetty :as jetty]
            [java-time.api :as t]
            [clojure.edn :as edn]
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

;; (def router
;;   (r/router
;;    [["/api/ping" ::ping]
;;     ["/api/orders/:id" ::order]]))

(defn handler [_]
  {:status 200, :body "pong"})

(def app
  (ring/ring-handler
   (ring/router
    [["/api"
      ["/ping" {:name ::api.ping :get (fn [_] {:status 200, :body "api/pong"})}]
      ["/user/:id" ::api.user]]
     ["/users"
      {:get (fn [{::r/keys [router]}]
              {:status 200
               :body (for [i (range 10)]
                       {:uri (-> router
                                 (r/match-by-name ::user {:id i})
                                   ;; with extra query-params
                                 (r/match->path {:iso "möly"}))})})}]
     ["/users/:id"
      {:name ::user
       :get (constantly {:status 200, :body "user..."})}]
     ["/ping" handler]])
   (constantly {:status 404, :body ""})))

(comment
  (app {:uri "/invalid"})
  (app {:request-method :get :uri "/ping"})
  (app {:request-method :get :uri "/api/ping"})
  (app {:request-method :get, :uri "/users"})
  )


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
