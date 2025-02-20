(ns graph.algorithms-test
  (:require [clojure.test :refer :all]
            [graph.algorithms :as g]))

(deftest test-make-graph
  (testing "make-graph generates a graph with exactly N vertices and S edges"
    (let [n 5
          s 7
          graph (g/make-graph n s)]
      (is (= (count graph) n))
      (is (= (reduce + (map count (vals graph))) s))))
  
  (testing "make-graph does not produce self-loops"
    (let [n 10
          s 20
          graph (g/make-graph n s)]
      (doseq [[src edges] graph
              [dst _] edges]
        (is (not= src dst)))))
  
  (testing "make-graph throws an exception for non-positive number of vertices"
    (is (thrown? IllegalArgumentException (g/make-graph 0 5)))
    (is (thrown? IllegalArgumentException (g/make-graph -1 5)))))

(deftest test-shortest-path
  (testing "shortest-path finds a valid shortest path in a known graph"
    (let [test-graph {"A" [["B" 1] ["C" 4]]
                      "B" [["C" 2] ["D" 5]]
                      "C" [["D" 1]]
                      "D" []}
          result (g/shortest-path test-graph "A" "D")]
      (is (some? result))
      (let [[path total-weight] result]
        ;; Convert path to vector for comparison if it's a list
        (is (= (vec path) ["A" "B" "C" "D"]))
        (is (= total-weight 4)))))
  
  (testing "shortest-path returns nil when no path exists"
    (let [disconnected-graph {"A" [["B" 1]]
                              "B" []
                              "C" [["D" 2]]
                              "D" []}]
      (is (nil? (g/shortest-path disconnected-graph "A" "D")))))
  
  (testing "shortest-path computes correctly for a cycle graph"
    (let [cycle-graph {"A" [["B" 1]]
                       "B" [["C" 1]]
                       "C" [["A" 1]]}
          result (g/shortest-path cycle-graph "A" "C")]
      (is (some? result))
      (let [[path total-weight] result]
        (is (= (vec path) ["A" "B" "C"]))
        (is (= total-weight 2)))))
  
  (testing "shortest-path selects the optimal path when multiple paths exist"
    (let [test-graph {"A" [["B" 2] ["C" 1]]
                      "B" [["D" 1]]
                      "C" [["D" 3]]
                      "D" []}
          result (g/shortest-path test-graph "A" "D")]
      (is (some? result))
      (let [[path total-weight] result]
        (is (= (vec path) ["A" "B" "D"]))
        (is (= total-weight 3))))))

(deftest test-eccentricity
  (testing "eccentricity computes the maximum distance from a vertex in a simple graph"
    (let [test-graph {"A" [["B" 1]]
                      "B" [["C" 2]]
                      "C" [["D" 3]]
                      "D" []}]
      (is (= (g/eccentricity test-graph "A") 6))))
  
  (testing "eccentricity returns nil for a disconnected graph"
    (let [test-graph {"A" [["B" 1]]
                      "B" []
                      "C" []}]
      (is (nil? (g/eccentricity test-graph "A"))))))

(deftest test-radius-diameter
  (testing "radius and diameter are computed correctly for a symmetric graph"
    (let [symmetric-graph {"A" [["B" 1] ["C" 4]]
                           "B" [["A" 1] ["C" 2] ["D" 5]]
                           "C" [["A" 4] ["B" 2] ["D" 1]]
                           "D" [["B" 5] ["C" 1]]}]
      (is (= (g/eccentricity symmetric-graph "A") 4))
      (is (= (g/radius symmetric-graph) 3))
      (is (= (g/diameter symmetric-graph) 4)))))


