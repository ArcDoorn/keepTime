(ns keepTime.remotes
  (:require [noir.fetch.remotes :as remotes])
  (:require [noir.server :as server])
  (:require [keepTime.model])
  (:use keepTime.tools)
  (:use korma.core))

(defmulti update-field
  "Update a given field in a given DB tabel."
  #(vector (keyword (:tab %)) (keyword (:field %))))

(defmethod update-field
  :default [{:keys [tab value field id] :as data-map}]
  (update tab
          (set-fields {field value})
          (where {:id [= id]}))
  (apply str (map #(str (name (first %))
                        " -- " (second %) "\n") data-map)))

(defmethod update-field
  [:timespan :startTime]
  [{:keys [tab value field id] :as data-map}]
  (let [current-timespan (first (select tab
                                        (where {:id [= id]}))),
        fields (cond
                (not (time-in-order?
                      value
                      (:endTime current-timespan)))
                {field value, :endTime value},
                :default {field value})]
    (update tab
            (set-fields fields)
            (where {:id [= id]}))
    (apply str (map #(str (name (first %))
                          " -- " (second %) "\n") data-map))))

(defmethod update-field
  [:timespan :endTime]
  [{:keys [tab value field id] :as data-map}]
  (let [current-timespan (first (select tab
                                        (where {:id [= id]}))),
        fields (cond
                (not (time-in-order?
                      (:startTime current-timespan)
                      value))
                {field value, :startTime value},
                :default {field value})]
    (update tab
            (set-fields fields)
            (where {:id [= id]}))
    (apply str (map #(str (name (first %))
                          " -- " (second %) "\n") data-map))))


(remotes/defremote save-field [data-map]
  (update-field data-map))

(defmulti delete-entry #(keyword (:tab %)))

(defmethod delete-entry :timespan [data-map]
  (delete (:tab data-map)
          (where {:id [= (:id data-map)]})))

(defmethod delete-entry :work [data-map]
  (delete :timespan
          (where {:work_id [= (:id data-map)]}))
  (delete (:tab data-map)
          (where {:id [= (:id data-map)]})))

(remotes/defremote delete-entity [data-map]
  (delete-entry data-map)
  (apply str (map #(str (name (first %))
                  " -- " (second %) "\n") data-map)))

(defmulti insert-entry #(keyword (:tab %)))

(defmethod insert-entry :work [data-map]
  (insert :work (values {:name "New work package",
                         :text "Enter a discription ...",
                         :day (:day data-map)}))
  {:inserted "work"})
(defmethod insert-entry :timespan [data-map]
  (insert :timespan (values {:work_id (:work data-map),
                             :startTime (str
                                         (:day data-map)
                                         "T00:00:00"), 
                             :endTime (str
                                       (:day data-map)
                                       "T00:00:00")}))
    {:inserted "timestamp"})

(remotes/defremote new-entity [data-map]
  (insert-entry data-map))