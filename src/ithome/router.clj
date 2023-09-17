(ns ithome.router
  (:require
   [ithome.hello :as hello]
   [ithome.login :as login]
   [clojure.tools.logging :as log]
   [ring.middleware.keyword-params :refer [wrap-keyword-params]]
   [ring.middleware.session.cookie :refer [cookie-store]]
   [ring.middleware.params :refer [wrap-params]]
   [ring.middleware.session :as session]
   [reitit.core :as r]))

(def api-router
  ["/api"
   ["/ping" {:name ::api.ping :get (fn [_] {:status 200, :body "api/pong"})}]
   ["/user/:id" ::api.user]])

(defn handler [_]
  {:status 200, :body "pong"})

(def session-options
  {:cookie-attrs {:path "/" :secure true :http-only true}
   :cookie-name "ithome2023"
   :store (cookie-store {:key "yoyoithome202309"})})

(defn wrap-session [handler]
  (fn [req]
    (let [req' (session/session-request req session-options)
          resp (handler req')]
      (session/session-response
       resp
       req'
       session-options))))

(def router
  (r/router 
   ["" {:middleware [wrap-params wrap-keyword-params wrap-session]}
    
    api-router
    ["/hello" (hello/router)]
    ["/login" (login/router)]
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
    ["/ping" handler]]))

(comment
  (-> router
    (r/match-by-name :login.root)
    (r/match->path {:iso "möly"}))
  (r/route-names router)
  (r/routes router))
