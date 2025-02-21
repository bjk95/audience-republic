import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers

class GraphAlgorithmsSpec extends AnyFlatSpec with Matchers {
  import GraphAlgorithms._

  "makeGraph" should "generate a graph with exactly N vertices and S edges" in {
    val n = 5
    val s = 7
    val graph = makeGraph(n, s)
    graph.size shouldBe n
    // Count total number of edges in the graph
    val totalEdges = graph.values.map(_.size).sum
    totalEdges shouldBe s
  }

  it should "not produce self-loops" in {
    val n = 10
    val s = 20
    val graph = makeGraph(n, s)
    for ((src, edges) <- graph; (dst, _) <- edges) {
      src should not equal dst
    }
  }

  it should "throw an exception for non-positive number of vertices" in {
    an [IllegalArgumentException] should be thrownBy {
      makeGraph(0, 5)
    }
    an [IllegalArgumentException] should be thrownBy {
      makeGraph(-1, 5)
    }
  }

  "shortestPath" should "find a valid shortest path in a known graph" in {
    // Construct a test graph:
    // A -> B (1), A -> C (4)
    // B -> C (2), B -> D (5)
    // C -> D (1)
    val testGraph = Map(
      "A" -> List(("B", 1), ("C", 4)),
      "B" -> List(("C", 2), ("D", 5)),
      "C" -> List(("D", 1)),
      "D" -> Nil
    )
    val result = shortestPath(testGraph, "A", "D")
    result.isDefined shouldBe true
    val (path, totalWeight) = result.get
    path shouldEqual List("A", "B", "C", "D")
    totalWeight shouldEqual 4
  }

  it should "return None when no path exists" in {
    // Construct a disconnected graph:
    // A -> B (1)
    // C -> D (2)
    val disconnectedGraph = Map(
      "A" -> List(("B", 1)),
      "B" -> Nil,
      "C" -> List(("D", 2)),
      "D" -> Nil
    )
    shortestPath(disconnectedGraph, "A", "D") shouldEqual None
  }

  "eccentricity" should "compute the maximum distance from a vertex in a simple graph" in {
    // A linear graph: A -> B (1), B -> C (2), C -> D (3)
    val testGraph = Map(
      "A" -> List(("B", 1)),
      "B" -> List(("C", 2)),
      "C" -> List(("D", 3)),
      "D" -> Nil
    )
    // From A, the path is A->B->C->D: total weight 1+2+3 = 6
    eccentricity(testGraph, "A") shouldEqual Some(6)
  }

  it should "return None for eccentricity in a disconnected graph" in {
    // Disconnected graph: A -> B (1), C isolated
    val testGraph = Map(
      "A" -> List(("B", 1)),
      "B" -> Nil,
      "C" -> Nil
    )
    eccentricity(testGraph, "A") shouldEqual None
  }

  "radius and diameter" should "be computed correctly for a symmetric graph" in {
    // Define a symmetric graph for clarity:
    // A <-> B (1), A <-> C (4)
    // B <-> C (2), B <-> D (5)
    // C <-> D (1)
    val symmetricGraph = Map(
      "A" -> List(("B", 1), ("C", 4)),
      "B" -> List(("A", 1), ("C", 2), ("D", 5)),
      "C" -> List(("A", 4), ("B", 2), ("D", 1)),
      "D" -> List(("B", 5), ("C", 1))
    )
    // For vertex A, the shortest distances are: A->A:0, A->B:1, A->C:3, A->D:4
    eccentricity(symmetricGraph, "A") shouldEqual Some(4)
    // Compute radius and diameter over all vertices
    radius(symmetricGraph) shouldEqual Some(3)   // minimal eccentricity (likely for C)
    diameter(symmetricGraph) shouldEqual Some(4) // maximum eccentricity (for A or D)
  }

  "shortestPath" should "compute shortest path correctly for a cycle graph" in {
    val cycleGraph = Map(
      "A" -> List(("B", 1)),
      "B" -> List(("C", 1)),
      "C" -> List(("A", 1))
    )
    val result = shortestPath(cycleGraph, "A", "C")
    result shouldEqual Some((List("A", "B", "C"), 2))
  }

  it should "select the optimal path when multiple paths exist" in {
    // Graph: A -> B (2), A -> C (1), B -> D (1), C -> D (3)
    val testGraph = Map(
      "A" -> List(("B", 2), ("C", 1)),
      "B" -> List(("D", 1)),
      "C" -> List(("D", 3)),
      "D" -> Nil
    )
    val result = shortestPath(testGraph, "A", "D")
    result shouldEqual Some((List("A", "B", "D"), 3))
  }
}
