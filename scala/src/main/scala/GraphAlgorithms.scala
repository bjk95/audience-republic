import scala.annotation.tailrec
import scala.collection.mutable
import scala.util.Random

object GraphAlgorithms {
  private type Vertex = String
  private type Edge = (Vertex, Int) // (destination, weight)
  private type Graph = Map[Vertex, List[Edge]]

  /** Generates a strongly-connected directed graph with n vertices and s edges.
    * Vertices are labeled "1", "2", …, "n". The weights are random integers in
    * 1 to 10. To guarantee strong connectivity we first create a cycle.
    *
    * If s is less than n, we bump it to n.
    */
  def makeGraph(n: Int, s: Int): Graph = {
    require(n > 0, "Number of vertices must be positive")
    val totalEdges = if (s < n) n else s

    val vertices: List[Vertex] = (1 to n).map(_.toString).toList
    val rand = new Random

    val permuted = rand.shuffle(vertices)
    val cycleEdges: List[(Vertex, Vertex)] =
      permuted.zip(permuted.tail :+ permuted.head)

    val edgeSet: mutable.Set[(Vertex, Vertex)] = mutable.Set() ++ cycleEdges

    val graphMutable: mutable.Map[Vertex, mutable.ListBuffer[Edge]] =
      mutable.Map(vertices.map(v => v -> mutable.ListBuffer.empty[Edge]): _*)

    for ((src, dst) <- cycleEdges) {
      val weight = rand.nextInt(10) + 1
      graphMutable(src).append((dst, weight))
    }
    var currentEdgeCount = cycleEdges.size

    while (currentEdgeCount < totalEdges) {
      val src = vertices(rand.nextInt(vertices.size))
      val dst = vertices(rand.nextInt(vertices.size))
      if (src != dst && !edgeSet.contains((src, dst))) {
        val weight = rand.nextInt(10) + 1
        graphMutable(src).append((dst, weight))
        edgeSet.add((src, dst))
        currentEdgeCount += 1
      }
    }
    graphMutable.map { case (v, edges) => v -> edges.toList }.toMap
  }

  /** Dijkstra's algorithm. Returns a tuple of:
    *   - distances: Map from each vertex to its shortest distance from source.
    *   - previous: Map to help reconstruct the shortest-path tree.
    */
  private def dijkstra(
      graph: Graph,
      source: Vertex
  ): (Map[Vertex, Int], Map[Vertex, Vertex]) = {
    // Pre-populate distances with all vertices in the graph
    val distances =
      mutable.Map[Vertex, Int]() ++ graph.keys.map(v => v -> Int.MaxValue)
    val previous = mutable.Map[Vertex, Vertex]()
    distances(source) = 0

    case class NodeDistance(vertex: Vertex, distance: Int)
    implicit val ord: Ordering[NodeDistance] = Ordering.by(nd => -nd.distance)
    val queue = mutable.PriorityQueue[NodeDistance]()
    queue.enqueue(NodeDistance(source, 0))

    while (queue.nonEmpty) {
      val NodeDistance(u, distU) = queue.dequeue()
      if (distU <= distances(u)) {
        for ((v, weight) <- graph.getOrElse(u, Nil)) {
          val alt = distances(u) + weight
          if (alt < distances(v)) {
            distances(v) = alt
            previous(v) = u
            queue.enqueue(NodeDistance(v, alt))
          }
        }
      }
    }
    (distances.toMap, previous.toMap)
  }

  /** Reconstructs the shortest path from source to target using the 'previous'
    * map. Returns a list of vertices representing the path.
    */
  private def reconstructPath(
      previous: Map[Vertex, Vertex],
      source: Vertex,
      target: Vertex
  ): List[Vertex] = {
    @tailrec
    def buildPath(v: Vertex, acc: List[Vertex]): List[Vertex] =
      if (v == source) source :: acc
      else buildPath(previous(v), v :: acc)
    if (source == target) List(source)
    else if (!previous.contains(target)) List() // no path found
    else buildPath(target, Nil)
  }

  /** Finds the shortest path from source to target. Returns Some((path,
    * totalWeight)) if a path exists, or None if unreachable.
    */
  def shortestPath(
      graph: Graph,
      source: Vertex,
      target: Vertex
  ): Option[(List[Vertex], Int)] = {
    val (distances, previous) = dijkstra(graph, source)
    if (distances(target) == Int.MaxValue) None
    else {
      val path = reconstructPath(previous, source, target)
      Some((path, distances(target)))
    }
  }

  /** Computes the eccentricity of a vertex. That is, the maximum shortest-path
    * distance from the vertex to any other vertex. Returns None if some
    * vertices are unreachable.
    */
  def eccentricity(graph: Graph, source: Vertex): Option[Int] = {
    val (distances, _) = dijkstra(graph, source)
    if (distances.values.exists(_ == Int.MaxValue)) None
    else Some(distances.values.max)
  }

  /** Computes the radius of the graph. The radius is the minimum eccentricity
    * among all vertices.
    */
  def radius(graph: Graph): Option[Int] = {
    val eccs =
      for (v <- graph.keys.toList; ecc <- eccentricity(graph, v)) yield ecc
    if (eccs.isEmpty) None else Some(eccs.min)
  }

  /** Computes the diameter of the graph. The diameter is the maximum
    * eccentricity among all vertices.
    */
  def diameter(graph: Graph): Option[Int] = {
    val eccs =
      for (v <- graph.keys.toList; ecc <- eccentricity(graph, v)) yield ecc
    if (eccs.isEmpty) None else Some(eccs.max)
  }

  /** Prints the graph in a human-friendly format.
    */
  private def printGraph(graph: Graph): Unit = {
    println("{")
    for ((src, edges) <- graph) {
      val edgeStr = edges
        .map { case (dst, weight) => s"($dst, $weight)" }
        .mkString("[", ", ", "]")
      println(s"  $src -> $edgeStr")
    }
    println("}")
  }

  /** A simple command-line interface.
    *
    * Example usage: scala GraphAlgorithms -N 4 -S 4
    *
    * It prints the randomly generated graph, the radius, the diameter, a
    * shortest path between two randomly chosen vertices, and the eccentricity
    * of one vertex.
    */
  def run(number_of_vertices: Int = 10, sparseness: Int = 15): Unit = {
    val n = number_of_vertices
    val s = sparseness

    println(s"Generating graph with $n vertices and $s edges:")
    val graph = makeGraph(n, s)
    printGraph(graph)

    println("\nGraph properties:")
    println(s"Radius: ${radius(graph).getOrElse("undefined")}")
    println(s"Diameter: ${diameter(graph).getOrElse("undefined")}")

    val vertices = graph.keys.toList
    val rand = new Random
    if (vertices.size >= 2) {
      val src = vertices(rand.nextInt(vertices.size))
      var dst = vertices(rand.nextInt(vertices.size))
      while (dst == src) {
        dst = vertices(rand.nextInt(vertices.size))
      }
      println(s"\nComputing shortest path from $src to $dst:")
      shortestPath(graph, src, dst) match {
        case Some((path, totalWeight)) =>
          println(
            s"Path: ${path.mkString(" -> ")} (Total weight: $totalWeight)"
          )
        case None =>
          println("No path found.")
      }
    }

    val randomVertex = vertices(rand.nextInt(vertices.size))
    println(s"\nEccentricity for vertex $randomVertex:")
    println(eccentricity(graph, randomVertex).getOrElse("undefined"))
  }
}
