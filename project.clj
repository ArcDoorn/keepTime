(defproject keepTime "0.5.0-SNAPSHOT"
  :description "A small time tracker and project management software."
  :main keepTime.core
  :aot [keepTime.core]
  :dependencies [[org.clojure/clojure "1.4.0"]
                 [cheshire "3.1.0"] ;; only needed for testing catnip
                 [korma "0.3.0-beta11"]
                 [noir "1.3.0-beta1"]
                 [clj-time "0.4.4"]
                 [org.xerial/sqlite-jdbc "3.7.2"]
                 ;; clojurescript stuff
                 [jayq "2.0.0"]
                 [fetch "0.1.0-alpha2"]]
  :plugins [[lein-cljsbuild "0.2.10"]]
  :cljsbuild
  {:builds
   [{:builds nil
     :source-path "src-cljs"
     :compiler
     {:output-to "resources/public/js/cljs.js"
      :optimizations :simple
      :pretty-print true}}]})