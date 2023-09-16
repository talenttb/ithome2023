(ns ithome.login
  (:require
   [clojure.core.match :as m]
   [ithome.db :as db]
   [hiccup2.core :as h]
   [honey.sql :as sql]
   [ring.util.response :as rr]
   [reitit.core :as r]))

(comment
  (:request-method req)
  (:params req))

(defn login_handler [req]
  ;; For demo how to debug ~~~
  (def req req)
  (m/match
   req
    {:request-method :post :params {:_action "login"}}
    (let [_ (def req req)
          params (:params req)
          user (db/query1
                (sql/format {:select [:*]
                             :from [:ithome]
                             :where [:and
                                     [:= [:json_extract :v [:inline (str "$.kind")]] "user"]
                                     [:= [:json_extract :v [:inline (str "$.name")]] "user"]]}))]
      (rr/redirect (-> (:reitit.core/router req)
                       (r/match-by-name :hello.you)
                       r/match->path)))

    :else
    {:status 200
     :headers {"Content-Type" "text/html;charset=UTF-8"}
     :body
     (str (h/raw "<!DOCTYPE html>")
          (h/html [:html {:lang "en"}
                   [:head
                    [:meta {:charset "UTF-8"}]
                    [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                    [:title "ITHOME 2023"]
                    [:link {:rel "stylesheet"
                            :href "https://cdn.jsdelivr.net/npm/@unocss/reset/tailwind.min.css"}]
                    [:script
                     {:src "https://cdn.jsdelivr.net/npm/@unocss/runtime/attributify.global.js"}]]

                   [:body {:p-2 "" :md:p-0 "" :h-full ""}
                    [:header {:max-w-screen-lg ""
                              :mx-auto ""
                              :md:flex ""
                              :justify-between ""
                              :py-4 ""}
                     [:h1 {:font-bold "" :text-4xl "" :md:inline-block ""}
                      "Welcome"]]
                    [:h2 {:max-w-screen-lg ""
                          :mx-auto ""
                          :font-bold ""
                          :text-2xl ""
                          :py-4 ""}
                     "Login: "]
                    [:div {:max-w-screen-lg ""
                           :mx-auto ""
                           :py-4 ""}
                     [:form {:action (-> (:reitit.core/router req)
                                         (r/match-by-name :login.root)
                                         (r/match->path {:_action "login"}))
                             :method "POST"}
                      [:label {:for "name" :font-bold ""}
                       "First name:"]
                      [:input#name {:b-1 "" :type "text" :name "name"}]
                      [:br]
                      [:br]
                      [:label {:for "pwd" :font-bold ""}
                       "Password:"]
                      [:input#pwd {:border-1 "" :type "text" :name "pwd"}]
                      [:br]
                      [:br]
                      [:input {:type "submit" :value "Submit"}]]]

                    [:p {:max-w-screen-lg "" :mx-auto ""}
                     "Give it a shot"]]]))}))

(defn router
  []
  [""
   ["" {:name :login.root :handler login_handler}]])
