(ns ithome.hello
  (:require [java-time.api :as t]
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
                  [:link {:rel "stylesheet" :href "style.css"}]]
                 [:body [:script {:src "index.js"}]
                  [:div [:h1 "Hello Hiccup"]]]]))})

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
   ["/you" {:handler you_handler}]])
