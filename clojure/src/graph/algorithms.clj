(ns graph.algorithms
    (:require [clojure.data.priority-map :as pm]
              [clojure.string :as str]))

(defn make-graph
  "Generates a strongly-connected directed graph with n vertices and s edges.
  Vertices are labeled \"1\", \"2\", …, \"n\". Edge weights are random integers 1–10.
  If s is less than n, it is bumped to n."
  [n s]
  (when (<= n 0)
        (throw (IllegalArgumentException. "Number of vertices must be positive")))
  (let [total-edges (if (< s n) n s)
        vertices (mapv str (range 1 (inc n)))
        permuted (shuffle vertices)
        cycle-edges (mapv vector permuted (concat (rest permuted) [(first permuted)]))
        ;; Initialize graph: map each vertex to an empty vector of edges.
        init-graph (into {} (for [v vertices] [v []]))
        ;; Add cycle edges to guarantee strong connectivity.
        init-graph (reduce (fn [g [src dst]]
                             (update g src conj [dst (+ 1 (rand-int 10))]))
                           init-graph cycle-edges)
        init-edge-set (set cycle-edges)
        init-count (count cycle-edges)]
    (loop [edge-set init-edge-set
           graph init-graph
           current-count init-count]
      (if (< current-count total-edges)
        (let [src (nth vertices (rand-int (count vertices)))
              dst (nth vertices (rand-int (count vertices)))]
          (if (or (= src dst) (contains? edge-set [src dst]))
            (recur edge-set graph current-count)
            (recur (conj edge-set [src dst])
              (update graph src conj [dst (+ 1 (rand-int 10))])
              (inc current-count))))
        graph))))

(defn dijkstra
;  Good opportunity to talk about structural sharing
  "Computes Dijkstra's algorithm on graph starting at source.
  Returns a vector of two maps: distances and previous."
  [graph source]
  (let [infty Integer/MAX_VALUE
        distances (atom (into {} (for [v (keys graph)] [v infty])))
        previous (atom {})]
    (swap! distances assoc source 0)
    (let [queue (atom (pm/priority-map source 0))]
      (loop []
        (if (seq @queue)
          (do
            (let [[u dist-u] (peek @queue)]
              (swap! queue dissoc u)
              (when (<= dist-u (get @distances u infty))
                    (doseq [[v weight] (get graph u)]
                      (let [alt (+ dist-u weight)]
                        (when (< alt (get @distances v infty))
                              (swap! distances assoc v alt)
                              (swap! previous assoc v u)
                              (swap! queue assoc v alt))))))
            (recur))
          [@distances @previous])))))


(defn reconstruct-path
  "Reconstructs the shortest path from source to target using the previous map.
  Returns a list of vertices representing the path (empty list if no path exists)."
  [previous source target]
  (if (= source target)
    [source]
    (if (not (contains? previous target))
      []
      (loop [v target, acc []]
        (if (= v source)
          (cons source acc)
          (recur (previous v) (cons v acc)))))))

(defn shortest-path
  "Finds the shortest path from source to target.
  Returns a vector [path total-weight] if a path exists, or nil if unreachable."
  [graph source target]
  (let [[distances previous] (dijkstra graph source)
        infty Integer/MAX_VALUE]
    (if (= (get distances target infty) infty)
      nil
      (let [path (reconstruct-path previous source target)]
        [path (get distances target)]))))

(defn eccentricity
  "Computes the eccentricity of a vertex (max shortest-path distance to any other vertex).
  Returns nil if some vertices are unreachable."
  [graph source]
  (let [[distances _] (dijkstra graph source)
        infty Integer/MAX_VALUE]
    (if (some #(= % infty) (vals distances))
      nil
      (apply max (vals distances)))))

(defn radius
  "Computes the radius of the graph (minimum eccentricity among vertices)."
  [graph]
  (let [eccs (keep #(eccentricity graph %) (keys graph))]
    (when (seq eccs)
          (apply min eccs))))

(defn diameter
  "Computes the diameter of the graph (maximum eccentricity among vertices)."
  [graph]
  (let [eccs (keep #(eccentricity graph %) (keys graph))]
    (when (seq eccs)
          (apply max eccs))))

(defn print-graph
  "Prints the graph in a human-friendly format."
  [graph]
  (println "{")
  (doseq [[src edges] graph]
    (let [edge-str (str "[" (str/join ", " (map (fn [[dst weight]]
                                                  (str "(" dst ", " weight ")"))
                                                edges))
                        "]")]
      (println " " src "->" edge-str)))
  (println "}"))

(defn run
  "A simple command-line interface.
  Generates a random graph with number-of-vertices and sparseness (number of edges),
  prints the graph, its radius and diameter, a shortest path between two random vertices,
  and the eccentricity of a random vertex."
  ([]
   (run 10 15))
  ([number-of-vertices sparseness]
   (let [n number-of-vertices
         s sparseness
         vertices (mapv str (range 1 (inc n)))]
     (println (str "Generating graph with " n " vertices and " s " edges:"))
     (let [graph (make-graph n s)]
       (print-graph graph)
       (println "\nGraph properties:")
       (println "Radius:" (or (radius graph) "undefined"))
       (println "Diameter:" (or (diameter graph) "undefined"))
       (let [verts (vec (keys graph))
             src (nth verts (rand-int (count verts)))
             dst (loop [d (nth verts (rand-int (count verts)))]
                   (if (= d src)
                     (recur (nth verts (rand-int (count verts)))
                       )
                     d))]
         (println (str "\nComputing shortest path from " src " to " dst ":"))
         (if-let [[path total-weight] (shortest-path graph src dst)]
           (println (str "Path: " (str/join " -> " path) " (Total weight: " total-weight ")"))
           (println "No path found.")))
       (let [random-vertex (nth (vec (keys graph)) (rand-int (count (keys graph))))]
         (println (str "\nEccentricity for vertex " random-vertex ":"))
         (println (or (eccentricity graph random-vertex) "undefined")))))))

;; To run in the REPL, simply evaluate the namespace definitions and call:
;; (load-file "graph.clj")
;; (in-ns "graph")
;; (run)
