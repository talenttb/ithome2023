(ns ithome.hello
  (:require
   [java-time.api :as t]
   [hiccup2.core :as h]))

(defn time-str
  "Returns a string representation of a datetime in the local time zone."
  [instant]
  (t/format
    (t/with-zone (t/formatter "hh:mm a") (t/zone-id))
    instant))

(defn run [opts]
  (println "Hello world, the time is" (time-str (t/instant))))

(defn you_handler [_]
  {:status 200
   :headers {"Content-Type" "text/html;charset=UTF-8"}
   :body
   (str (h/raw "<!DOCTYPE html>")
        (h/html [:html {:lang "en"}
                 [:head
                  [:meta {:charset "UTF-8"}]
                  [:meta {:name "viewport" :content "width=device-width, initial-scale=1.0"}]
                  [:title "HTML 5 Boilerplate"]
                  [:link {:rel "stylesheet"
                          :href "https://cdn.jsdelivr.net/npm/@unocss/reset/tailwind.min.css"}]

                  [:script
                   (h/raw
                    "
                    window.__unocss = {
                        rules: [
                            ['custom-rule', { color: 'red'}],
                        ]
                    }
                  ")]
                  [:script
                   {:src "https://cdn.jsdelivr.net/npm/@unocss/runtime/attributify.global.js"}]]

                 [:body {:p-2 "" :md:p-0 "" :h-full ""}
                  [:header {:max-w-screen-lg ""
                            :mx-auto ""
                            :md:flex ""
                            :justify-between ""
                            :py-4 ""}
                   [:h1 {:font-bold "" :text-4xl "" :md:inline-block ""}
                    "Basic template"]
                   [:ul {:pt-2 ""}
                    [:li {:md:inline-block "" :ml-4 ""}
                     [:a {:href "yyy"}
                      "basic"]]
                    [:li {:md:inline-block "" :ml-4 ""}
                     [:a {:href "xxx"}
                      "inline"]]]]
                  [:h2 {:max-w-screen-lg ""
                        :mx-auto ""
                        :font-bold ""
                        :text-2xl ""
                        :py-4 ""}
                   "This is a page"]
                  [:div {:custom-rule "" :m-1 ""} "test"]

                  [:p {:max-w-screen-lg "" :mx-auto ""}
                   "Give it a shot"]]]))})

(comment
  (str (h/html [:span {:class "foo"} "bar"]))
  (h/html
   {:lang "en"}
   [:head]
   [:body [:div [:h1 {:class "info"} "Hiccup"]]])
  )

(defn router
  []
  [""
   ["/you" {:name :hello.you :handler you_handler}]])
