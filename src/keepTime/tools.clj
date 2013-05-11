(ns keepTime.tools
  (:use [clj-time.core :only [after? interval in-secs
                              time-zone-for-offset]])
  (:use [clj-time.format :only [formatter parse unparse]])
  (:use [clojure.string :only [join]]))

(def db-formatter (formatter (time-zone-for-offset 0)
                             "yyyy-MM-dd'T'HH:mm:ss"
                             "yyyy-MM-dd HH:mm:ss"
                             "yyyy-MM-dd"))

(def time-formatter (formatter "HH:mm"))

(defn db-date
  "parse a date given from the DB"
  [db-date-string]
  (parse db-formatter db-date-string))

(defn format-time
  "Formats a date-time fom the DB to a timestring HH:mm as it is
used for output."
  [date-time]
  (unparse time-formatter (parse db-formatter date-time)))

(defn time-in-order?
  "Returns true if the second argument is after the first.
The arguments are expected to be db time strings."
  [start end]
  (after? (parse db-formatter end)
          (parse db-formatter start)))

(defn in-the-afternoon?
  "Given date-time after noon?"
  [date-time]
  (after? (parse time-formatter
                 (unparse time-formatter
                          (parse db-formatter date-time)))
          (parse time-formatter "12:00")))

(defn in-the-morning?
  "Given date-time before noon?"
  [date-time]
  (not (in-the-afternoon? date-time)))


(defn format-duration
  "Formats a duration provided as number of seconds
into a human readable string."
  [seconds]
  (let [units {:h 3600 :min 60 :s 1}]
    (->> (reduce (fn [rests key]
                   (let [current (last rests)
                         new-rest (pop rests)
                         unit-value (key units)]
                     (conj new-rest
                           (int (/ current unit-value))
                           (mod current unit-value))))
                 [seconds] (keys units))
         (pop) ; remove the extra rest
        (interleave (keys units)) ; group output and unit
        (partition 2)
        (filter (fn [[unit value]] (not= 0 value))) ; remove zeros
        (map (fn [[unit value]] ; format as strings
               (str value (name unit))))
        (#(if (seq %) % ["0 min"]))
        (join " "))))

(defn duration-of-time-span
  "Calculates the duration of a given time span in seconds."
  [time-span]
  (in-secs (interval (parse db-formatter (:startTime time-span))
                     (parse db-formatter (:endTime time-span)))))

(defn formated-duration-of-time-span
  "Formats the duration of a given time span."
  [time-span]
  (format-duration (duration-of-time-span time-span)))


(defn formated-duration-of-day
  "Formats the logged time of a given day."
  [day]
  (format-duration (apply +
                          (map #(duration-of-time-span %)
                               (apply concat
                                      (map :timespan
                                           (:work day)))))))

(defn formated-duration-of-work
  "Formats the logged time of a given work package."
  [work]
  (format-duration (apply +
                      (map #(duration-of-time-span %)
                           (:timespan work)))))


(defn data-to-class-string
  "Creates a list of class tags in the form data-key-value for the map."
  [data-map]
  (join " " (map #(str "data-" (name (first %)) "-" (second %)) data-map)))
