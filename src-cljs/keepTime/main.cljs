;; -*- mode:clojure; -*-
(ns keepTime.main
  (:require [goog.ui.DatePicker] [goog.editor.Field]
            [clojure.browser.repl :as repl])
  (:use [jayq.core :only [$]]
        [keepTime.tools :only [date-changed]]
        [keepTime.calendar :only [render-calendar]]
        [keepTime.fields :only [render-edit-fields
                                bind-new-buttons
                                bind-delete-buttons]]))
;; repl hook in 
(repl/connect "http://localhost:9000/repl")

;; load when the document is ready
(defn ^:export init-page
  "Init the page on start up."
  [day work]
  ;; render the date picker
  (render-calendar (first ($ :#calendar)) day)
  ;; render an edit field to the work title
  (render-edit-fields day work)
  ;; bind the new buttons
  (bind-new-buttons day work)
  ;; bind the delete buttons
  (bind-delete-buttons day work))
