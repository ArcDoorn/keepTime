(ns keepTime.view
  (:use noir.core) 
  (:use hiccup.core) 
  (:use hiccup.page) 
  (:use hiccup.element) 
  (:use keepTime.model)
  (:use [keepTime.workTime :only [calculate-work-hours]])
  (:use keepTime.tools))

;; centrale page skeleton:
;; the page is devided into a top a content part,
;; the content part provides three regions 
(defpartial layout [& {:keys [browser-title title
                              init-script
                              left middle right]}]
  (html (html5  [:header
                 [:title (str "Keep Time: " browser-title)]
                 (include-css "/css/style.css")
                 (include-css (str "/css/datepicker.css"))]
                [:body
                 (include-js "http://code.jquery.com/jquery-1.8.3.js")
                 (include-js "/js/cljs.js")
                 (javascript-tag init-script)
                 [:div#top
                  [:div#logo [:h1.logo "&nbsp"]]
                  [:div#title title]]
                 [:div#content
                  [:div#left left]
                  [:div#middle middle]
                  [:div#right right]]])))

;; the tool box for the right hand side
(defpartial tool-box [day]
  (html [:div#calendar]))

;; add classes and buttons, so that a given field can be edited
(defpartial editable-field [content, table, field, id]
  (let [data-classes (data-to-class-string {:tab table,
                                            :field field,
                                            :id id})]
    (html [:span {:class (str "editable " data-classes)} content]
          [:span {:style "display:none",
                  :class (str "button submit " data-classes)
                  :title "Ok"} "o"]
          [:span {:style "display:none",
                  :class (str "button cancel " data-classes)
                  :title "Abbrechen"} "x"])))

;; work and time span details
(defpartial timespan-details [time-span]
  (html [:p [:span.time
             (editable-field
              (format-time (:startTime time-span))
              "timespan" "startTime" (:id time-span))]
         " - "
         [:span.time (editable-field
                      (format-time (:endTime time-span))
                      "timespan" "endTime" (:id time-span))]
         " ("
         [:span.time (formated-duration-of-time-span time-span)]
         ") "
         [:span {:class (str
                         (data-to-class-string
                          {:tab "timespan",
                           :id (:id time-span)})
                         " button delete")
                 :title "Löschen"}
          "-"]]))

(defpartial work-details [work day]
  (html [:h2 (editable-field (:name work) "work" "name" (:id work))]
        [:p (editable-field (:text work) "work" "text" (:id work))]
        [:div.colcontainer
         [:div.col "&nbsp;"
          (map #(timespan-details %)
                        (filter #(in-the-morning? (:startTime %))
                                (:timespan work)))]
         [:div.col "&nbsp;"
          (map #(timespan-details %)
                        (filter #(in-the-afternoon? (:startTime %))
                                (:timespan work)))]]
        [:p [:span {:class (str "button new "
                           (data-to-class-string
                            {:work (:id work),
                             :day day,
                             :tab "timespan"}))
                    :title "Neues Zeit erfassen"}
             "+"]]))
;; list of work packages
(defpartial work-list [day works]
  (html
   [:ul.worklist
    (map #(html [:li.work
                 (link-to (str "/day/" day "/" (:id %))
                          [:span.workName {:title (:text %)} (:name %)])
                 " "
                 [:span.time "("
                  (formated-duration-of-work %)
                  ")"]
                 [:span {:class (str
                                 (data-to-class-string
                                  {:tab "work",
                                   :id (:id %)})
                                 " button delete")
                         :title "Löschen"} "-"
                  ]])
         works)]
   [:p [:span {:class (str "button new "
                           (data-to-class-string
                            {:day day,
                             :tab "work"}))
               :title "Neues Arbeitspaket"} "+"]]))

;; provided pages:
;; day overview w/o selected work package
(defpage "/day/:day" {:keys [day]}
  (let [day-record (report-time day)]
    (layout
     :browser-title day
     :title (html [:h1 "Keep Time: " day]
                  [:p [:span.time 
                       "Geplante Zeit: "
                       (format-duration (calculate-work-hours day))
                       [:br]
                       "Gebuchte Zeit: "
                       (formated-duration-of-day day-record)]])
     :init-script
     (str "$(function() {keepTime.main.init_page('" day "');});")
     :left
     (work-list day (:work day-record))
     :middle
     "Please select a item."
     :right
     (tool-box day))))

;; day overview with a selected work
(defpage "/day/:day/:id" {:keys [day id]}
  (let [day-record (report-time day)
        work (first (filter #(= id (str (:id %)))
                          (:work day-record)))
        ]
    (layout
     :browser-title day
     :title (html [:h1 "Keep Time: " day]
                  [:p [:span.time 
                       "Geplante Zeit: "
                       (format-duration (calculate-work-hours day))
                       [:br]
                       "Gebuchte Zeit: "
                       (formated-duration-of-day day-record)]])
     :init-script
     (str "$(function() {keepTime.main.init_page('" day "', '" id "');});")
     :left
     (work-list day (:work day-record))
     :middle
     (work-details work day)
     :right
     (tool-box day))))
