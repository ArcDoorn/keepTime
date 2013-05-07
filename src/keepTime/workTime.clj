(ns keepTime.workTime
  (require [clj-time.core :as ctc ]))

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

(defn holiday-list
  "The list of holidays for a given year"
  [year]
  (let [eastern (eastern year)]
    {:new-year (ctc/date-time year 01 01),
     :epiphany (ctc/date-time year 01 06),
     :maundy-thursday (ctc/minus eastern (ctc/days 3)),
     :good-friday (ctc/minus eastern (ctc/days 2)),
     :easter-sunday eastern,
     :easter-monday (ctc/plus eastern (ctc/days 1)),
     :international-workers-day (ctc/date-time year 05 01),
     :ascension (ctc/plus eastern (ctc/days 39)),
     :pentecost-sunday (ctc/plus eastern (ctc/days 49)),
     :pentecost-monday (ctc/plus eastern (ctc/days 50)),
     :corpus-christi (ctc/plus eastern (ctc/days 60)),
     :augsburger-peace-feast (ctc/date-time year 08 08),
     :maria-ascension (ctc/date-time 08 15),
     :german-unity-day (ctc/date-time 10 03),
     :reformation-day (ctc/date-time 10 31),
     :all-saints-day (ctc/date-time 11 01),
     :day-of-repentance-and-prayer :foo}))

(defn date-tags
  "Tag a day with day of week and with holiday tags."
  [date]
  (let [week-tags [:mon :tue :wed :thu :fri :sat :sun]] 
    [(week-tags (- (ctc/day-of-week date) 1))]))