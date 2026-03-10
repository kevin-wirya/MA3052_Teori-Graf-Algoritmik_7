package dfs;

/**
 * Kelas untuk testing berbagai fungsi DFS dan Graph
 */
public class DFSTest {
    
    public static void main(String[] args) {
        System.out.println("=== TESTING DFS IMPLEMENTATION ===\n");
        
        // Test 1: Basic DFS functionality
        testBasicDFS();
        
        // Test 2: Different graph types
        testDifferentGraphTypes();
        
        // Test 3: Path finding
        testPathFinding();
        
        // Test 4: Connectivity analysis
        testConnectivityAnalysis();
        
        // Test 5: Edge cases
        testEdgeCases();
        
        System.out.println("\n=== ALL TESTS COMPLETED ===");
    }
    
    /**
     * Test basic DFS functionality
     */
    private static void testBasicDFS() {
        System.out.println("--- Test 1: Basic DFS Functionality ---");
        
        Graph graph = new Graph(4);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(3, 0);
        
        DFS dfs = new DFS(graph);
        
        // Test recursive DFS
        java.util.List<Integer> recursiveResult = dfs.dfsRecursive(0);
        System.out.println("Recursive DFS result: " + recursiveResult);
        
        // Test iterative DFS
        java.util.List<Integer> iterativeResult = dfs.dfsIterative(0);
        System.out.println("Iterative DFS result: " + iterativeResult);
        
        // Verify both traverse all vertices
        boolean recursiveComplete = recursiveResult.size() == 4;
        boolean iterativeComplete = iterativeResult.size() == 4;
        
        System.out.println("Recursive DFS complete: " + recursiveComplete);
        System.out.println("Iterative DFS complete: " + iterativeComplete);
        System.out.println();
    }
    
    /**
     * Test different graph types
     */
    private static void testDifferentGraphTypes() {
        System.out.println("--- Test 2: Different Graph Types ---");
        
        // Linear graph
        System.out.println("Testing Linear Graph:");
        Graph linear = new Graph(4);
        linear.addEdge(0, 1);
        linear.addEdge(1, 2);
        linear.addEdge(2, 3);
        
        DFS linearDFS = new DFS(linear);
        System.out.println("Linear graph DFS: " + linearDFS.dfsRecursive(0));
        
        // Star graph
        System.out.println("Testing Star Graph:");
        Graph star = new Graph(5);
        star.addEdge(0, 1);
        star.addEdge(0, 2);
        star.addEdge(0, 3);
        star.addEdge(0, 4);
        
        DFS starDFS = new DFS(star);
        System.out.println("Star graph DFS: " + starDFS.dfsRecursive(0));
        
        // Complete graph
        System.out.println("Testing Complete Graph:");
        Graph complete = new Graph(4);
        complete.addEdge(0, 1);
        complete.addEdge(0, 2);
        complete.addEdge(0, 3);
        complete.addEdge(1, 2);
        complete.addEdge(1, 3);
        complete.addEdge(2, 3);
        
        DFS completeDFS = new DFS(complete);
        System.out.println("Complete graph DFS: " + completeDFS.dfsRecursive(0));
        System.out.println();
    }
    
    /**
     * Test path finding functionality
     */
    private static void testPathFinding() {
        System.out.println("--- Test 3: Path Finding ---");
        
        Graph graph = new Graph(6);
        graph.addEdge(0, 1);
        graph.addEdge(1, 2);
        graph.addEdge(2, 3);
        graph.addEdge(0, 4);
        graph.addEdge(4, 5);
        
        DFS dfs = new DFS(graph);
        
        // Test existing path
        java.util.List<Integer> path1 = dfs.findPath(0, 3);
        System.out.println("Path from 0 to 3: " + path1);
        
        // Test another path
        java.util.List<Integer> path2 = dfs.findPath(0, 5);
        System.out.println("Path from 0 to 5: " + path2);
        
        // Test non-existing path in disconnected components
        Graph disconnected = new Graph(4);
        disconnected.addEdge(0, 1);
        disconnected.addEdge(2, 3);
        
        DFS disconnectedDFS = new DFS(disconnected);
        java.util.List<Integer> noPath = disconnectedDFS.findPath(0, 2);
        System.out.println("Path from 0 to 2 (disconnected): " + noPath);
        System.out.println();
    }
    
    /**
     * Test connectivity analysis
     */
    private static void testConnectivityAnalysis() {
        System.out.println("--- Test 4: Connectivity Analysis ---");
        
        // Connected graph
        Graph connected = new Graph(4);
        connected.addEdge(0, 1);
        connected.addEdge(1, 2);
        connected.addEdge(2, 3);
        
        DFS connectedDFS = new DFS(connected);
        System.out.println("Connected graph is connected: " + connectedDFS.isConnected());
        System.out.println("Connected graph components: " + connectedDFS.countConnectedComponents());
        
        // Disconnected graph
        Graph disconnected = new Graph(5);
        disconnected.addEdge(0, 1);
        disconnected.addEdge(2, 3);
        // Vertex 4 is isolated
        
        DFS disconnectedDFS = new DFS(disconnected);
        System.out.println("Disconnected graph is connected: " + disconnectedDFS.isConnected());
        System.out.println("Disconnected graph components: " + disconnectedDFS.countConnectedComponents());
        System.out.println();
    }
    
    /**
     * Test edge cases
     */
    private static void testEdgeCases() {
        System.out.println("--- Test 5: Edge Cases ---");
        
        // Single vertex
        System.out.println("Testing Single Vertex:");
        Graph single = new Graph(1);
        DFS singleDFS = new DFS(single);
        System.out.println("Single vertex DFS: " + singleDFS.dfsRecursive(0));
        System.out.println("Single vertex connected: " + singleDFS.isConnected());
        
        // Empty graph (multiple isolated vertices)
        System.out.println("Testing Empty Graph (no edges):");
        Graph empty = new Graph(3);
        DFS emptyDFS = new DFS(empty);
        System.out.println("Empty graph DFS from 0: " + emptyDFS.dfsRecursive(0));
        System.out.println("Empty graph components: " + emptyDFS.countConnectedComponents());
        
        // Self-loop (if implemented)
        System.out.println("Testing Graph with Potential Issues:");
        Graph complex = new Graph(3);
        complex.addEdge(0, 1);
        complex.addEdge(1, 0); // This should be handled by undirected graph implementation
        
        DFS complexDFS = new DFS(complex);
        System.out.println("Complex graph DFS: " + complexDFS.dfsRecursive(0));
        System.out.println();
    }
}