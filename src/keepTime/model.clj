(ns keepTime.model
  (:use [korma.db])
  (:use [korma.core])
  (:require [clojure.java.jdbc :as sql]))

;; DB definitions ...............................

(def dbspec
  {:classname "org.sqlite.JDBC"
   :subprotocol "sqlite"
   :subname "timeData.sqlite"})
;   :subname "/home/doorn/programming/clojure/keepTime/timeData.sqlite"})

(defdb timeData dbspec)

(defentity timespan
  (entity-fields :id :starttime :endtime))

(defentity work
  (entity-fields :id :name :text :date)
  (has-many timespan))

;; Function for initializin the DB ...........................

(defn create-tables
  "Definition of the relevant tables."
  []
  (do 
    (sql/create-table
     "work"
     [:id "INTEGER" "PRIMARY KEY" "AUTOINCREMENT"]
     [:name "VARCHAR(1024)"]
     [:text "VARCHAR(1024)"]
     [:day "DATE" "NOT NULL"])
    (sql/create-table
     "timespan"
     [:id "INTEGER" "PRIMARY KEY" "AUTOINCREMENT"]
     [:startTime "DATETIME"]
     [:endTime "DATETIME"]
     [:work_id "INTEGER" "REFERENCES work(id)"])))

(defn invoke-with-connection
  "Helper function to do SQL commands inside a transaction."
  [spec f]
  (sql/with-connection spec
    (sql/transaction
     (f))))

(defn init-tables
  "Create the tables. Used for a fresh DB."
  []
  (invoke-with-connection dbspec create-tables))

;; Access functions ........................................

(defn log-time
  "Logs a time span given by start and end time to the given work package.
If the work package doesn't exist yet, it will be created in the database.
The work package, including a possiblegenerated id is returned."
  [workPackage startTime endTime]
  (let [workId (or (:id workPackage)
                   (#((first (keys %)) %)
                    ; need only the single value of the returned hash
                    (insert work
                      (values (select-keys workPackage
                                           [:id :day :text :name])))))]
    (insert timespan
      (values {:work_id workId
               :startTime startTime
               :endTime endTime}))
    (first (select work
             (with timespan)
             (where {:id workId})))))



(defn report-time [day]
  "Reports the loged work for the given day."
  {:day day,
   :work (select work
           (with timespan)
           (where {:day [= day]}))})