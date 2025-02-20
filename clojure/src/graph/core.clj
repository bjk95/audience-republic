(ns graph.core
  (:require [graph.algorithms :as g]
            [clojure.tools.cli :refer [parse-opts]]))

(def cli-options
  [["-N" "--vertices N" "Number of vertices"
    :default 10
    :parse-fn #(Integer/parseInt %)
    :validate [#(> % 0) "Must be a positive integer."]]
   ["-S" "--sparseness S" "Sparseness (number of edges)"
    :default 15
    :parse-fn #(Integer/parseInt %)
    :validate [#(>= % 0) "Must be a non-negative integer."]]])

(defn usage [options-summary]
  (->> ["Usage: graph -N <number-of-vertices> -S <sparseness>"
        ""
        "Options:"
        options-summary]
       (clojure.string/join \newline)))

(defn error-msg [errors]
  (str "Error parsing command-line arguments:\n" (clojure.string/join \newline errors)))

(defn -main
  "Entry point for the application."
  [& args]
  (let [{:keys [options arguments errors summary]} (parse-opts args cli-options)]
    (cond
      errors (do
               (println (error-msg errors))
               (System/exit 1))
      (:help options) (do
                        (println (usage summary))
                        (System/exit 0))
      :else (let [n (:vertices options)
                  s (:sparseness options)]
              (println "Running graph application with:")
              (println "  Number of vertices:" n)
              (println "  Sparseness:" s)
              (g/run n s)))))