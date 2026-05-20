# Graph Algorithms Library

A Java/Kotlin graph library featuring classic algorithms (MST, SSSP, topological sort), an interactive shell, Graphviz visualization, and a benchmarking suite — all wired together with a custom dependency injection container.

---

## Features

**Graph core (`graph` package)**
- Generic, type-safe `Graph<VD, ED>` supporting directed and undirected graphs
- Prim's and Kruskal's MST on undirected graphs
- Dijkstra's SSSP on weighted directed/undirected graphs
- DAG shortest path via topological sort (linear time)
- Topological sort (recursive DFS + iterative DFS variant)
- `DisjointSet` (union-find with union by rank and path compression) for Kruskal's

**GShell (`gshell` package)**  
An interactive REPL for building and querying graphs by hand. The shell runs a Tokenizer → Parser pipeline that produces an AST evaluated against a live graph environment.

**Visualization (`gshelll.DotMapper` — Kotlin)**  
Generates Graphviz `.dot` files and renders them to PNG via a spawned `dot` process. MST edges are highlighted in green with a heavier pen width.

**Benchmarking (`benchmark` package)**  
A reproducible benchmark harness that runs algorithms over multiple graph sizes and topologies (sparse, dense, complete, DAG) and exports results to CSV for downstream analysis.

---

## Architecture

```
io.github.youssefrashidy
├── graph/
│   ├── Graph.java              # Core graph: adjacency list, algorithm implementations
│   ├── Vertex.java / Edge.java # Generic vertex and edge types
│   ├── GraphType.java          # DIRECTED / UNDIRECTED enum
│   ├── augumentingDS/
│   │   └── DisjointSet.java    # Union-find (used by Kruskal's)
│   └── exceptions/             # CycleDetectionException, EdgeMismatchException, …
├── gshell/
│   ├── GShell.java             # REPL loop
│   ├── Tokenizer.java          # Lexer
│   ├── Parser.java             # Recursive-descent parser → AST nodes
│   ├── nodes/                  # NewNode (graph creation), CommandNode (operations)
│   └── tokens/                 # Token hierarchy (CommandToken, IdentifierToken, …)
├── gshelll/
│   └── DotMapper.kt            # Kotlin: graph → Graphviz DOT string + MST overlay
├── benchmark/
│   ├── BenchmarkOrchestrator.java  # Composes runs across sizes and topologies
│   ├── BenchmarkRunner.java        # Executes and times a single algorithm run
│   ├── GraphGenerator.java         # Generates sparse / dense / complete / DAG graphs
│   ├── GraphBuilder.java           # Translates raw edge lists into Graph objects
│   ├── FeatureExtractor.java       # Flattens results into feature rows for CSV
│   ├── CSVExporter.java            # Writes feature rows to CSV
│   └── model/                      # Result records: MSTComparison, SSSPComparison, …
└── Main.java                   # Entry point: menu → GShell or benchmark runner
```

> The project uses a custom DI container (Mini-DI) via `@Component` / `@Inject` annotations. `BenchmarkOrchestrator`, `BenchmarkRunner`, `GraphGenerator`, and `GraphBuilder` are all managed beans resolved at startup.

---

## GShell Command Reference

### Creating a graph

```
Graph <name> = new Graph(Directed)
Graph <name> = new Graph(Undirected)
```

### Graph operations

| Command | Arguments | Description |
|---|---|---|
| `<g>.addVertex("v")` | vertex name | Add a single vertex |
| `<g>.addVertices("a", "b", …)` | one or more names | Add multiple vertices |
| `<g>.addEdge("u", "v", weight)` | source, dest, int weight | Add one edge |
| `<g>.addEdges("u1","v1",w1, …)` | triples | Add multiple edges at once |
| `<g>.primMST()` | — | Compute and print Prim's MST |
| `<g>.kruskalMST()` | — | Compute and print Kruskal's MST |
| `<g>.dijkstra("src")` | source vertex name | Print shortest distances from source |
| `<g>.dagShortestPath("src")` | source vertex name | DAG shortest path (directed graphs only) |
| `<g>.visualizeGraph()` | — | Render graph to PNG via Graphviz |
| `<g>.visualizePrimMST()` | — | Render graph with Prim's MST highlighted |
| `<g>.visualizeKruskalMST()` | — | Render graph with Kruskal's MST highlighted |

Type `exit` or `quit` to leave the shell.

**Example session:**

```
gshell> Graph g = new Graph(Undirected)
gshell> g.addVertices("A", "B", "C", "D")
gshell> g.addEdges("A", "B", 4, "A", "C", 2, "B", "D", 5, "C", "D", 1)
gshell> g.kruskalMST()
[C-D(1), A-C(2), B-D(5)]
gshell> g.visualizeKruskalMST()
```

---

## Benchmarks

The main menu exposes four benchmark modes. Each run collects timing data over 10 graphs per (size × topology) combination and exports results to CSV.

| Option | What it benchmarks | Graph sizes |
|---|---|---|
| MST benchmark | Prim vs Kruskal on sparse, dense, complete graphs | 1 000 · 2 500 · 5 000 · 10 000 |
| Dijkstra benchmark | Dijkstra on sparse, dense, DAG, complete graphs | same |
| SSSP-DAG benchmark | Dijkstra vs DAG-SP on DAGs | same |
| All benchmarks | All of the above, three CSV files | same |

Complete graphs are capped at 5 000 vertices to avoid generating graphs with tens of millions of edges.

---

## Dependencies

| Library | Purpose |
|---|---|
| Eclipse Collections | Primitive maps/lists (`IntObjectHashMap`, `IntIntHashMap`, `FastList`, …) throughout the graph and benchmark code |
| Kotlin stdlib | `DotMapper` visualization helper |
| Graphviz (`dot`) | External process for PNG rendering — must be installed and on `PATH` |
| Mini-DI (internal) | `@Component` / `@Inject` DI container used to wire benchmark beans |

---

## Building and Running

```bash
# Build
mvn package

# Run
mvn exec:java -Dexec.mainClass="io.github.youssefrashidy.Main"
```

On startup the application bootstraps the DI container, scanning the `benchmark`, `graph`, and `gshell` packages, then presents the main menu.

**Visualization prerequisite:** Install [Graphviz](https://graphviz.org/download/) and ensure `dot` is on your system `PATH`. The shell currently opens the rendered PNG via `cmd /c start` (Windows). On Linux/macOS, update the `ProcessBuilder` call in `CommandNode` accordingly.

---

## Data Structures

### Graph representation

The core `Graph<VD, ED>` maintains three parallel structures:

**Adjacency list** — `IntObjectHashMap<MutableList<Edge<ED>>>`  
Maps each vertex id (primitive `int`) to a `FastList` of outgoing edges. Using Eclipse Collections' primitive-keyed map avoids boxing vertex ids to `Integer` on every lookup, which matters at sizes of 10 000+ vertices and dense edge sets. For undirected graphs, every logical edge is decomposed into two directed half-edges — one stored under each endpoint — so adjacency traversal is symmetric without any extra branching in the algorithm.

**Edge list** — `FastList<Edge<ED>>`  
A flat list of every *unique* edge, regardless of direction. Kruskal's needs to sort all edges by weight without touching the adjacency list; having this as a separate list means `kruskalMST()` can call `toSortedList()` on it and leave the adjacency list untouched. It also serves as the canonical source for Graphviz export.

**Vertex map** — `IntObjectHashMap<Vertex<VD>>`  
Provides O(1) vertex lookup by id for formatting output and resolving source vertices in SSSP calls.

---

### Vertex and Edge

`Vertex<D>` is a thin wrapper around a monotonically assigned `int id` and a generic payload `D`. Vertices are always referenced by their id inside algorithm code, keeping all hot-path maps primitive.

`Edge<D>` stores `u`, `v`, `id`, and `weight` as plain `int` fields. Equality and hashing are defined solely on `id`, which makes Kruskal's MST set membership check (`edge in mstSet`) work correctly even for undirected half-edges — both half-edges of the same logical edge share the same `id`.

---

### Priority queue (Prim's and Dijkstra's)

Both algorithms use `java.util.PriorityQueue<QueueEntry>` ordered by the `key` field (edge weight for Prim's, tentative distance for Dijkstra's). Rather than implementing a decrease-key operation, the code uses a **lazy deletion** pattern: stale entries are left in the queue and discarded when popped by checking the `inMST` / `foundShortestPath` boolean maps. This trades some extra memory for a simpler implementation with no handle bookkeeping.

---

### Union-Find (`DisjointSet`)

Used exclusively by Kruskal's MST. Backed by two `IntIntHashMap`s — `parent` and `rank` — both primitive to avoid boxing. Implements:

- **Union by rank** — the tree of lower rank is always attached under the tree of higher rank, keeping trees shallow.
- **Path compression** (`findSet`) — on each call the chain of ancestors is flattened to point directly at the root, so subsequent finds on the same element are O(1). Together these give an amortised O(α(n)) per operation.

---

### Topological sort stack

`topologicalSort()` returns an `IntArrayStack` (Eclipse Collections primitive stack). Vertices are pushed in post-order during DFS, so popping the stack yields topological order. The same stack is consumed immediately by `dagShortestPath()` without materialising the full ordering into a list.

A gray/black distinction for cycle detection is maintained via two `IntHashSet`s (`visited` and `onStack`). If a vertex on the current DFS stack is encountered again, a `CycleDetectionException` is thrown immediately — Kahn's algorithm was considered but the DFS approach integrates naturally with the topological order needed by `dagShortestPath`.

An iterative DFS variant (`dfsIterative`) is also implemented using an explicit `Deque<DFSFrame>`, avoiding stack-overflow risk on graphs with very deep chains. It is not yet wired into the public API but is present as a drop-in replacement.

---

### Benchmark timing

`BenchmarkRunner` times each algorithm with `System.nanoTime()` over **20 iterations** after **10 warm-up rounds** (allowing the JIT to compile the hot path before measurements begin). A `System.gc()` call followed by a 100 ms sleep separates each run to reduce GC interference between measurements. Raw `long[]` timing arrays are passed to `FeatureExtractor` for aggregation.

---

## Testing

JUnit tests live under `src/test/java`:

- `GraphTest` — correctness tests for MST, Dijkstra, DAG shortest path, topological sort, and edge type enforcement
- `GraphGeneratorTest` — validates generated graph topology (connectivity, acyclicity for DAGs, edge density)
- `DisjointSetTest` — union-find correctness (union by rank, path compression)
