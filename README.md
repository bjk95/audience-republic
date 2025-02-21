# Audience Republic Code Challenge

## TL;DR

### Clojure

First, move to the clojure directory

``` bash
cd clojure
```

Run with default graph dimensions using:

``` bash
lein run 
```

Run with custom graph dimensions using:

``` bash
lein run -N 4 -S 4
```

N - number of vertices
S - number of edges

Tests can be run with:

``` bash
lein test
```

Finally, you can run the REPL using:

``` bash
lein repl
```

Then, inside the repl:

``` clj
(load-file "src/graph/algorithms.clj")
(in-ns 'graph.algorithms)
(def random-graph (make-graph 10 10))

(eccentricity random-graph (first (keys random-graph))) ; => number expressing eccentricity for `first` vertex in random-graph

(radius random-graph) ; => minimal eccentricity
(diameter random-graph) ; => maximal eccentricity
(run)
```

### Scala

I've dockerised the project to save you needing to install Scala. Run it using the following commands. Specifying the graph dimensions is optional.

``` bash
cd scala
docker build -t audience-republic .
```

``` bash
docker run --rm audience-republic -N 100 -S 150
```

or

``` bash
docker run --rm audience-republic
```

## Detailed breakdown

Both implementations represent the graph as a mapping from vertex labels (strings) to a collection of weighted edges.

**Scala:**
The graph is modeled as an immutable Map[String, List[(String, Int)]].

**Clojure:**
The graph is represented as a map from vertices to vectors (or lists) of edge pairs.
Graph Generation Strategy:

- **Strong Connectivity:**
Both projects ensure strong connectivity by first constructing a cycle through all vertices. This guarantees at least one path exists between every pair of vertices.
- **Edge Addition:**
After forming the cycle, additional edges are added randomly until the total count meets the desired sparseness. Edge weights are randomly generated integers between 1 and 10.
- **Tradeoffs:**
This method simplifies the design and ensures connectivity. However, it may not produce the most optimal edge distribution in very sparse or very dense graphs. The focus here is on clarity and maintainability.

### Shortest Path Computation

Both implementations use Dijkstra's algorithm to compute the shortest path between vertices.

- **Scala Implementation:**
Employs a mutable priority queue in the inner loop for performance reasons.
The mutable state is confined within the algorithm to maintain a functional public API.
- **Clojure Implementation:**
Utilizes clojure.data.priority-map for managing the priority queue functionally.
Uses atoms to control local mutability during the execution of the algorithm.

#### Tradeoffs

**Performance vs. Purity:**
The Scala version uses controlled mutability to optimize performance, while the Clojure version emphasizes functional purity and easier reasoning with persistent data structures.

### Graph Distance Properties

Both projects provide functions to compute:

- **Eccentricity:** Maximum shortest-path distance from a vertex.
- **Radius:** The minimum eccentricity among all vertices.
- **Diameter:** The maximum eccentricity among all vertices.

#### Tradeoffs

These functions run Dijkstra’s algorithm for each vertex, which is not optimal for very large graphs. However, this approach favors clarity and modularity, making it easier to understand, test, and maintain.

### Testing & Modularity

- **Testing:**
  - Each project includes comprehensive tests:
    - **Graph Generation Tests:** Ensure strong connectivity and absence of self-loops.
    - **Shortest Path & Distance Property Tests:** Validate the correctness of Dijkstra’s algorithm and derived graph metrics.
- **Modularity:**
  - Each function performs one well-defined task. This separation of concerns improves reusability and maintainability, which are key in real-world applications.

### Functional Programming Principles

- **Immutability:**
  - Both implementations use immutable data structures. Scala leverages immutable maps and lists, while Clojure uses persistent maps and vectors.
- **Pure Functions:**
  - Most functions are pure and side-effect free. While Dijkstra’s algorithm employs local mutability (Scala’s mutable queue or Clojure’s atoms), this is encapsulated and does not affect the external API.
- **Encapsulated Mutability for Performance:**
  - Limited, controlled mutability is employed in performance-critical sections to balance efficiency with functional purity.
- **Modularity & Readability:**
  - Functions are designed to do one thing only, making the code easier to reason about, test, and extend.

## Potential Improvements & Considerations

While the current implementation is robust and well-structured, several potential enhancements could further improve clarity, performance, and maintainability:

- **Refactoring Dijkstra's Algorithm:**
  - Current State: The algorithm uses local mutable state (via atoms) to manage distances and the priority queue.
  - Potential Improvement: Refactoring the loop into a recursive function that threads state through parameters could encapsulate mutability even more effectively and improve overall clarity, while still maintaining performance.
- **Optimizing Graph Distance Calculations:**
  - Current State: The functions for eccentricity, radius, and diameter each invoke Dijkstra’s algorithm separately.
  - Potential Improvement: Investigating caching strategies or utilizing an all-pairs shortest path algorithm (such as Floyd–Warshall) might yield more efficient computations, particularly for larger graphs.
- **Streamlining Random Vertex Selection in the CLI:**
  - Current State: A recursive loop is used to ensure the destination vertex differs from the source.
  - Potential Improvement: Simplifying the process by filtering the source out of the vertex list and then randomly selecting from the remaining vertices could make the code more concise and readable.
- **Enhancing Error Handling and Input Validation:**
  - Current State: Basic validations are in place for the vertex count and sparseness.
  - Potential Improvement: Adding additional checks—such as ensuring the sparseness does not exceed the maximum possible for a simple directed graph (i.e., N(N-1))—and providing more detailed messaging around unreachable vertices could further strengthen robustness.
- **Expanding Testing & Documentation:**
  - Current State: A test suite is available and can be executed with lein test.
  - Potential Improvement: Expanding the test coverage to include more edge cases (e.g., degenerate graphs) and offering deeper documentation on algorithmic tradeoffs and complexity considerations could be very beneficial for future maintainability.