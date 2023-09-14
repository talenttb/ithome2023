(ns ithome.db
  (:require
   [honey.sql :as sql]
   [next.jdbc :as jdbc]
   [nano-id.core :refer [nano-id]]
   [clojure.data.json :as json]
   [next.jdbc.result-set :as rs])
  (:import [com.github.f4b6a3.ksuid KsuidCreator]))

(defn- ksuid
  []
  (str (KsuidCreator/getMonotonicKsuid)))

(def ^:private config {:dbtype "sqlite"
                       :dbname "./mydev/db"})

(def ^:private ds (jdbc/get-datasource config))

(defn query [sql]
  (jdbc/execute! ds sql
                 {:return-keys true
                  :builder-fn rs/as-unqualified-maps}))

;; in sqlite, both of 'query1' are equal almost.
#_(defn query1 [sql]
    (jdbc/execute-one! ds sql
                       {:return-keys true
                        :builder-fn rs/as-unqualified-maps}))

(defn query1 [sql]
  (first (query sql)))

(defn put [data]
  (let [k (nano-id)
        data' (-> data
                  (assoc :id k))
        stmt (sql/format {:insert-into [:ithome [:id :k :v]]
                          :values [[(ksuid) k (json/write-str data')]]})]
    (jdbc/execute! ds stmt)
    data'))

(defn- create-table
  [tbl]
  (jdbc/execute! ds (sql/format
                     {:create-table [tbl :if-not-exists]
                      :with-columns [[:id :text :primary :key]
                                     [:k :text]
                                     [:v :text]
                                     [:t :timestamp :default :current_timestamp]]})))

(defn init-table []
  (create-table "ithome"))

(comment
  (let [tbl "mytable"]
    (sql/format
     {:create-table [tbl :if-not-exists]
      :with-columns [[:id :text :primary :key]
                     [:k :text]
                     [:v :text]
                     [:t :timestamp :default :current_timestamp]]}))

  (create-table "ithome")

  (put {:hi "ithome" :day "11" :kind "test"})

  (query ["select * from ithome"])
  (query1 ["select * from ithome"])

  (query (sql/format {:select [:*]
                      :from [:ithome]})))
