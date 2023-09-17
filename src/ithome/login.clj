(ns ithome.login
  (:require
   [buddy.sign.jwt :as jwt]
   [buddy.hashers :as hashers]
   [java-time.api :as t]
   [clojure.core.match :as m]
   [ithome.db :as db]
   [buddy.core.nonce :as nonce]
   [hiccup2.core :as h]
   [honey.sql :as sql]
   [ring.util.response :as rr]
   [reitit.core :as r]))

(comment
  (def req req)
  (:request-method req)
  (:params req)

  (def claims {:user 1 :exp (t/plus (t/instant) (t/hours 2))})

  (def token (jwt/sign claims "key"))

  (defn derive-options []
    (-> #{:bcrypt+blake2b-512 :bcrypt+sha384
          :pbkdf2+blake2b-512 :pbkdf2+sha512
          :pbkdf2+sha3_256 :pbkdf2+sha1}
        shuffle
        first))

  (hashers/derive "secretpassword" {:alg (derive-options)})
  (hashers/verify "secretpassword" "bcrypt+blake2b-512$eecc8e644f1680ddac4996a6eae757bd$12$ed54a6c83108f45f1fc807fe8de173a9a3e87a49dd586b34")

  (let [pwd "hi"
        derived-pwd (hashers/derive pwd {:alg (derive-options)})
        result (hashers/verify pwd derived-pwd)]
    (db/put {:name "test" :kind "user" :derived-pwd derived-pwd})
    (prn result))
  
  (jwt/unsign token "key")
  (jwt/unsign token "key" {:now (t/plus (t/instant) (t/hours 10))}))

(defn login_handler [req]
  (m/match
   req
    {:request-method :post :params {:_action "login"}}
    (let [params (:params req)
          user (db/query1
                (sql/format {:select [:*]
                             :from [:ithome]
                             :where [:and
                                     [:= [:json_extract :v [:inline (str "$.kind")]] "user"]
                                     [:= [:json_extract :v [:inline (str "$.name")]] (:name params)]]}))]
      (if user
        (-> (rr/redirect (-> (:reitit.core/router req)
                             (r/match-by-name :hello.me)
                             r/match->path))
            (assoc :session (jwt/sign {:user (:id user)
                                       :exp (t/plus (t/instant) (t/hours 2))} "key")))

        ;; TODO: redirect to the same page with warning messages.
        {:status 401 :body "You!! Unauthorized"}))

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
