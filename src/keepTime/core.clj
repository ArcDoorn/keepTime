(ns keepTime.core
  (:gen-class)
  (:require keepTime.view)
  (:require keepTime.remotes)
  (:require [noir.server :as server]))

(defn -main [& args]
  (println "Starting a keepTime server, use your browser, ...")
  (server/start 8080))





