(defproject graph "0.1.0-SNAPSHOT"

  :dependencies [[org.clojure/clojure "1.11.1"]
                 [org.clojure/data.priority-map "1.2.0"]
                 [org.clojure/tools.cli "1.0.206"]]
  :main graph.core
  :repl-options {:init-ns graph.core})

