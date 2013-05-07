;; -*- mode:clojure; -*-
(ns keepTime.tools
  (:use [jayq.core :only [$]])
  (:require [clojure.string :as cs]))

(defn date-to-url
  "Returns the url that points to the day
   represented by the give date (as ISO string, e.g. 20121221)."
  [date]
  (str
   "/day/"
   (subs date 0 4)
   "-"
   (subs date 4 6)
   "-"
   (subs date 6 8)))

(defn remove-html
  "Remove html-tags from a given string."
  [text]
  (cs/replace text #"<[^<>]*>" ""))
