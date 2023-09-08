(ns user
  (:require [java-time.api :as t]))

(defn date-str
  []
  (t/format
    (t/with-zone (t/formatter "yyyy-MM-dd") (t/zone-id))
    (t/instant)))
