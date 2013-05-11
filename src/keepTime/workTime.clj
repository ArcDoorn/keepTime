(ns keepTime.workTime
  (require [clj-time.core :as ctc ]))

(def week-tags
  "Week day names used here"
  [:mon :tue :wed :thu :fri :sat :sun])

(defn eastern
  [year]
;;  1. die Säkularzahl:                                  K(X) = X div 100
;;  2. die säkulare Mondschaltung:                       M(K) = 15 + (3K + 3) div 4 − (8K + 13) div 25
;;  3. die säkulare Sonnenschaltung:                     S(K) = 2 − (3K + 3) div 4
;;  4. den Mondparameter:                                A(X) = X mod 19
;;  5. den Keim für den ersten Vollmond im Frühling:   D(A,M) = (19A + M) mod 30
;;  6. die kalendarische Korrekturgröße:               R(D,A) = (D + A div 11) div 29[12]
;;  7. die Ostergrenze:                               OG(D,R) = 21 + D − R
;;  8. den ersten Sonntag im März:                    SZ(X,S) = 7 − (X + X div 4 + S) mod 7
;;  9. die Entfernung des Ostersonntags von der
;;     Ostergrenze (Osterentfernung in Tagen):      OE(OG,SZ) = 7 − (OG − SZ) mod 7
;; 10. das Datum des Ostersonntags als Märzdatum
;;     (32. März = 1. April usw.):                         OS = OG + OE
;;
  (let [K (quot year 100),
        M (- (+ 15 (quot (+ (* 3 K) 3) 4))
             (quot (+ (* 8 K) 13) 25)),
        S (- 2 (quot (+ (* 3 K) 3) 4)),
        A (mod year 19),
        D (mod (+ (* 19 A) M) 30),
        R (quot (+ D (quot A 11)) 29),
        OG (- (+ 21 D) R),
        SZ (- 7 (mod (+ year (quot year 4) S) 7)),
        OE (- 7 (mod (- OG SZ) 7)),
        OS (+ OG OE)]
    (ctc/plus (ctc/date-time year 03 01) ;; add the eastern offset to the first march.
              (ctc/days (- OS 1)))))

(defn week-day-before-date
  "Returns the date with a given weekday before the given date."
  [day-of-week date]
  (let [current-day-of-week (ctc/day-of-week date),
        desired-day-of-week (+ (.indexOf week-tags day-of-week) 1),
        offset (+ (mod (+ 7 (- current-day-of-week
                            desired-day-of-week 1))
                    7) 1)]
    (ctc/minus date (ctc/days offset))))

(defn holiday-list
  "The list of holidays for a given year"
  [year]
  (let [eastern (eastern year)]
    {:new-year (ctc/date-time year 1 1),
     :epiphany (ctc/date-time year 1 6),
     :maundy-thursday (ctc/minus eastern (ctc/days 3)),
     :good-friday (ctc/minus eastern (ctc/days 2)),
     :easter-sunday eastern,
     :easter-monday (ctc/plus eastern (ctc/days 1)),
     :international-workers-day (ctc/date-time year 5 1),
     :ascension (ctc/plus eastern (ctc/days 39)),
     :pentecost-sunday (ctc/plus eastern (ctc/days 49)),
     :pentecost-monday (ctc/plus eastern (ctc/days 50)),
     :corpus-christi (ctc/plus eastern (ctc/days 60)),
     :augsburger-peace-feast (ctc/date-time year 8 8),
     :maria-ascension (ctc/date-time year 8 15),
     :german-unity-day (ctc/date-time year 10 3),
     :reformation-day (ctc/date-time year 10 31),
     :all-saints-day (ctc/date-time year 11 1),
     :day-of-repentance-and-prayer (week-day-before-date
                                    :wed
                                    (ctc/date-time year 11 23)),
     :holy-eve (ctc/date-time year 12 24),
     :first-day-of-christmas (ctc/date-time year 12 25),
     :second-day-of-christmas (ctc/date-time year 12 26)
     :silvester (ctc/date-time year 12 31)}))

(defn date-tags
  "Tag a day with day of week and with holiday tags."
  [date]
  (conj
   (map first (filter
               #(= date (second %))
               (holiday-list (ctc/year date))))
   (week-tags (- (ctc/day-of-week date) 1))))

(defn calculate-work-hours
  [date]
  (let [hours-per-tag
        {:new-year 0,
         :epiphany 1,
         :maundy-thursday 1,
         :good-friday 0,
         :easter-sunday 0,
         :easter-monday 0,
         :international-workers-day 0,
         :ascension 0,
         :pentecost-sunday 0,
         :pentecost-monday 0,
         :corpus-christi 1,
         :augsburger-peace-feast 1,
         :maria-ascension 1,
         :german-unity-day 0,
         :reformation-day 1,
         :all-saints-day 1,
         :day-of-repentance-and-prayer 1,
         :holy-eve 1/2,
         :first-day-of-christmas 0,
         :second-day-of-christmas 0,
         :silvester 1/2,
         :mon 1,
         :tue 1,
         :wed 1,
         :thu 1,
         :fri 1,
         :sat 0,
         :sun 0},
        date-tags (date-tags date),
        base-work-hours 8]
    (* base-work-hours
       (apply min
              (map hours-per-tag date-tags)))))
