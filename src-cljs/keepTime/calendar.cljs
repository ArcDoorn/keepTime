;; -*- mode:clojure; -*-

(ns keepTime.calendar
  (:require [goog.ui.DatePicker])
  (:use [keepTime.tools :only [date-to-url]]))


(defn date-changed
  "Handler for the date change in the picker.
   It redirects to the new date."
  [event]
  (when (.-date event)
    (set! window.location (date-to-url (.toIsoString (.-date event))))))


(defn render-calendar 
  "Renders a calender component to a given div element"
  [div day]
  (let [calendar (goog.ui/DatePicker.)]
    (.setDate calendar (js/Date. day))
    (.render calendar div)
    (.listen goog.events calendar
             (.-CHANGE (.-Events goog.ui.DatePicker))
               date-changed)))

