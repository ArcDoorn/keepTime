;; -*- mode:clojure; -*-
(ns keepTime.fields
  (:require [goog.editor.Field]
            [clojure.string :as cstr]
            [fetch.remotes :as remotes])
  (:require-macros [fetch.macros :as fetchm])
  (:use [jayq.core :only [$]]
  	[keepTime.tools :only [date-to-url remove-html]]))

(defn get-data
  "Return a map {key val, ...} of all classes of the form data-key-val."
  [component]
  (let [classes-raw (.attr ($ component) "class"),
        classes (cstr/split classes-raw #"\s+")]
    (apply hash-map 
           (apply concat
                  (map #(list (keyword (first %)) (second %))
                       (map #(rest (re-find #"^data-([^\-]*)-(.*)" %))
                            (filter #(re-find #"^data-" %) classes)))))))

(defn data-to-class-str
  "generates the class string corresponding to the given data object"
  [data]
  (apply str
         (map #(str ".data-"
                    (name (first %))
                    "-"
                    (second %))
              data)))

(defn get-relative-components
  "Gets the corresponding component with same data and given class
for a given component."
  [component class]
  ($ (str "." (name class)
       (data-to-class-str (get-data component)))))

(defn submit-field-content
  "transfer the data of a given field and hide the buttons etc."
  [{:keys [submit-data field ok cancel work day] :as params}]
  ;; call remote
  (fetchm/letrem [result
                  (save-field submit-data)]
                 (.log js/console
                       (str "Remote call result:" result))
                 ;; reload full page
                 ;; a better solution would be to update
                 ;; all related fields
                 (set! window.location
                       (str "/day/" day "/" work)))
  ;; hide buttons
  (.hide ok)
  (.hide cancel)
  ;; make field non editable
  (.makeUneditable field)
  ;; unbind the button
  (.unbind ok "click")
  (.unbind cancel "click"))

(defmulti ok-function
  "Handling the click of an ok-button: validate and submit data"
  (fn [x]
    (keyword (:tab (get-data (:component x))))))

(defmethod ok-function :timespan
  [{:keys [component field ok cancel work day] :as params}]
  (let [date-value (str day "T"
                        (remove-html
                         (.getCleanContents field))
                        ":00"),
        submit-data (assoc (get-data component)
                      :value date-value)]
    (cond
     (not (re-matches
      #"\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}"
      date-value))
     (js/alert "Sorry, but the time should be something like 01:23."),
     :default (submit-field-content
               (assoc params
                 :submit-data submit-data)))))  

(defmethod ok-function :default
  [{:keys [component field ok cancel work day] :as params}]
  (let [field-value (.getCleanContents field),
        submit-data (assoc (get-data component)
                      :value field-value)]
    (cond
     (re-matches #"\s*"
                 (remove-html field-value))
     (js/alert "Sorry, but the field may not be empty."),
     :default (submit-field-content
               (assoc params
                 :submit-data submit-data)))))

(defn create-click-fun
  "Create a click function for the given field and component"
  [field component day work]
  (let [ok (get-relative-components component :submit)
        cancel (get-relative-components component :cancel)]
  (fn []
    ;; bind functions to buttons
    (.bind ok "click"
           (partial ok-function
                    {:field field,
                     :component component,
                     :ok ok,
                     :cancel cancel,
                     :day day,
                     :work work}))

    (let [start-content (.getCleanContents field)]
      (.bind cancel "click"
             (fn [] 
               ;; call remote
               (.setHtml field false start-content)
               ;; hide buttons
               (.hide ok)
               (.hide cancel)
               ;; make field non editable
               (.makeUneditable field)
               ;; unbind the button
               (.unbind ok "click")
               (.unbind cancel "click"))))
      
    ;; show edit button
    (.show ok)
    (.show cancel)
    ;; make field editable
    (.makeEditable field))))

(defn render-edit-fields
  "Render Closure fields to all components of the editable class.
Handle function are linked to the corresponding buttons."
  [day work]
  (let [component-list ($ :.editable)]
    (doseq [component component-list
          :let [field (goog.editor/Field. component)]]
      (do (.bind ($ component) "click"
               (create-click-fun field ($ component) day work))))))

(defn create-new-fun
  "Returns a function for calling the remote creation function."
  [component day work]
  (fn []
    ;; get data
    (fetchm/letrem [result
                    (new-entity (get-data component))]
                   (.log js/console
                         (str "Remote call result:" result))
                   ;; reload full page
                   ;; a better solution would be to update
                   ;; all related fields
                   (set! window.location
                        (str "/day/" day (when work (str "/" work)))))))

(defn bind-new-buttons
  "Link remote functions to all new buttons."
  [day work]
  (let [component-list ($ :.button.new)]
    (doseq [component component-list]
      (do (.bind ($ component) "click"
               (create-new-fun ($ component) day work))))))

(defn create-delete-fun
  "Returns a fucntion, that calls the remote delete function."
  [component day work]
  (fn []
    ;; get data
    (fetchm/letrem [result
                    (delete-entity (get-data component))]
                   (.log js/console
                         (str "Remote call result:" result))
                   ;; reload full page
                   ;; a better solution would be to update
                   ;; all related fields
                   (set! window.location
                         (str "/day/" day (when work (str "/" work)))))))

(defn bind-delete-buttons
  "Link remote functions to all new buttons."
  [day work]
  (let [component-list ($ :.button.delete)]
    (doseq [component component-list]
      (do (.bind ($ component) "click"
               (create-delete-fun ($ component) day work))))))

