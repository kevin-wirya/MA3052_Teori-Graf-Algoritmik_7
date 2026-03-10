package src.utils;

import java.io.*;
import java.util.*;

public class GraphVisualizer {
    
    public void generateVisualization(Map<Integer, List<Integer>> graph, 
                                    List<Integer> dfsResult, 
                                    List<Integer> bfsResult, 
                                    String filename) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"en\">");
            writer.println("<head>");
            writer.println("    <meta charset=\"UTF-8\">");
            writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            writer.println("    <title>DFS & BFS Visualization</title>");
            writer.println("    <script src=\"https://d3js.org/d3.v7.min.js\"></script>");
            writer.println("    <style>");
            writer.println(getCSS());
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class=\"container\">");
            writer.println("        <h1>DFS & BFS Graph Traversal Visualization</h1>");
            writer.println("        <div class=\"results\">");
            writer.println("            <div class=\"result-box\">");
            writer.println("                <h3>DFS Result</h3>");
            writer.println("                <p>" + dfsResult + "</p>");
            writer.println("            </div>");
            writer.println("            <div class=\"result-box\">");
            writer.println("                <h3>BFS Result</h3>");
            writer.println("                <p>" + bfsResult + "</p>");
            writer.println("            </div>");
            writer.println("        </div>");
            writer.println("        <div class=\"controls\">");
            writer.println("            <button onclick=\"animateDFS()\">Animate DFS</button>");
            writer.println("            <button onclick=\"animateBFS()\">Animate BFS</button>");
            writer.println("            <button onclick=\"resetVisualization()\">Reset</button>");
            writer.println("        </div>");
            writer.println("        <div id=\"graph\"></div>");
            writer.println("    </div>");
            writer.println("    <script>");
            writer.println(getJavaScript(graph, dfsResult, bfsResult));
            writer.println("    </script>");
            writer.println("</body>");
            writer.println("</html>");
            
            writer.close();
            System.out.println("Visualization generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error generating visualization: " + e.getMessage());
        }
    }
    
    public void generateConnectivityVisualization(Map<Integer, List<Integer>> graph,
                                                List<List<Integer>> connectedComponents,
                                                List<String> bridges,
                                                List<Integer> articulationPoints,
                                                String filename) {
        try {
            PrintWriter writer = new PrintWriter(new FileWriter(filename));
            
            writer.println("<!DOCTYPE html>");
            writer.println("<html lang=\"en\">");
            writer.println("<head>");
            writer.println("    <meta charset=\"UTF-8\">");
            writer.println("    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">");
            writer.println("    <title>Graph Connectivity Analysis</title>");
            writer.println("    <script src=\"https://d3js.org/d3.v7.min.js\"></script>");
            writer.println("    <style>");
            writer.println(getConnectivityCSS());
            writer.println("    </style>");
            writer.println("</head>");
            writer.println("<body>");
            writer.println("    <div class=\"container\">");
            writer.println("        <h1>Graph Connectivity Analysis</h1>");
            writer.println("        <div class=\"analysis-results\">");
            writer.println("            <div class=\"result-box\">");
            writer.println("                <h3>Connected Components (" + connectedComponents.size() + ")</h3>");
            for (int i = 0; i < connectedComponents.size(); i++) {
                writer.println("                <p>Component " + (i+1) + ": " + connectedComponents.get(i) + "</p>");
            }
            writer.println("            </div>");
            writer.println("            <div class=\"result-box\">");
            writer.println("                <h3>Bridge Edges</h3>");
            writer.println("                <p>" + (bridges.isEmpty() ? "None" : bridges) + "</p>");
            writer.println("            </div>");
            writer.println("            <div class=\"result-box\">");
            writer.println("                <h3>Articulation Points</h3>");
            writer.println("                <p>" + (articulationPoints.isEmpty() ? "None" : articulationPoints) + "</p>");
            writer.println("            </div>");
            writer.println("        </div>");
            writer.println("        <div class=\"controls\">");
            writer.println("            <button onclick=\"highlightComponents()\">Highlight Components</button>");
            writer.println("            <button onclick=\"highlightBridges()\">Highlight Bridges</button>");
            writer.println("            <button onclick=\"highlightArticulationPoints()\">Highlight Articulation Points</button>");
            writer.println("            <button onclick=\"resetVisualization()\">Reset</button>");
            writer.println("        </div>");
            writer.println("        <div id=\"graph\"></div>");
            writer.println("    </div>");
            writer.println("    <script>");
            writer.println(getConnectivityJavaScript(graph, connectedComponents, bridges, articulationPoints));
            writer.println("    </script>");
            writer.println("</body>");
            writer.println("</html>");
            
            writer.close();
            System.out.println("Connectivity visualization generated: " + filename);
        } catch (IOException e) {
            System.err.println("Error generating connectivity visualization: " + e.getMessage());
        }
    }
    
    private String getCSS() {
        return """
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                margin: 0;
                padding: 20px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                min-height: 100vh;
            }
            
            .container {
                max-width: 1200px;
                margin: 0 auto;
                background: white;
                border-radius: 15px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                padding: 30px;
            }
            
            h1 {
                text-align: center;
                color: #333;
                margin-bottom: 30px;
                font-size: 2.5rem;
            }
            
            .results {
                display: flex;
                justify-content: space-around;
                margin-bottom: 30px;
            }
            
            .result-box {
                background: #f8f9fa;
                padding: 20px;
                border-radius: 10px;
                border-left: 5px solid #007bff;
                width: 45%;
            }
            
            .result-box h3 {
                color: #007bff;
                margin-top: 0;
            }
            
            .controls {
                text-align: center;
                margin-bottom: 30px;
            }
            
            button {
                background: linear-gradient(45deg, #007bff, #0056b3);
                color: white;
                border: none;
                padding: 12px 25px;
                margin: 0 10px;
                border-radius: 25px;
                cursor: pointer;
                font-size: 16px;
                font-weight: 500;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(0,123,255,0.3);
            }
            
            button:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(0,123,255,0.4);
            }
            
            button:active {
                transform: translateY(0);
            }
            
            #graph {
                border: 2px solid #dee2e6;
                border-radius: 15px;
                background: #ffffff;
                box-shadow: inset 0 2px 10px rgba(0,0,0,0.1);
            }
            
            .node {
                fill: #69b3ff;
                stroke: #2980b9;
                stroke-width: 3px;
                cursor: pointer;
                transition: all 0.3s ease;
            }
            
            .node:hover {
                fill: #ff6b6b;
                r: 25;
            }
            
            .node.visited {
                fill: #2ecc71;
                stroke: #27ae60;
            }
            
            .node.current {
                fill: #e74c3c;
                stroke: #c0392b;
                r: 25;
            }
            
            .link {
                stroke: #bdc3c7;
                stroke-width: 3;
                transition: all 0.3s ease;
            }
            
            .link.active {
                stroke: #e74c3c;
                stroke-width: 5;
            }
            
            .node-label {
                font-size: 16px;
                font-weight: bold;
                text-anchor: middle;
                dominant-baseline: central;
                fill: white;
                pointer-events: none;
            }
            """;
    }
    
    private String getConnectivityCSS() {
        return """
            body {
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                margin: 0;
                padding: 20px;
                background: linear-gradient(135deg, #ff9a9e 0%, #fecfef 50%, #fecfef 100%);
                min-height: 100vh;
            }
            
            .container {
                max-width: 1200px;
                margin: 0 auto;
                background: white;
                border-radius: 15px;
                box-shadow: 0 10px 30px rgba(0,0,0,0.2);
                padding: 30px;
            }
            
            h1 {
                text-align: center;
                color: #333;
                margin-bottom: 30px;
                font-size: 2.5rem;
            }
            
            .analysis-results {
                display: flex;
                justify-content: space-between;
                margin-bottom: 30px;
                flex-wrap: wrap;
            }
            
            .result-box {
                background: #f8f9fa;
                padding: 20px;
                border-radius: 10px;
                border-left: 5px solid #e91e63;
                width: 30%;
                margin-bottom: 15px;
            }
            
            .result-box h3 {
                color: #e91e63;
                margin-top: 0;
            }
            
            .controls {
                text-align: center;
                margin-bottom: 30px;
            }
            
            button {
                background: linear-gradient(45deg, #e91e63, #ad1457);
                color: white;
                border: none;
                padding: 12px 25px;
                margin: 0 5px;
                border-radius: 25px;
                cursor: pointer;
                font-size: 14px;
                font-weight: 500;
                transition: all 0.3s ease;
                box-shadow: 0 4px 15px rgba(233,30,99,0.3);
            }
            
            button:hover {
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(233,30,99,0.4);
            }
            
            #graph {
                border: 2px solid #dee2e6;
                border-radius: 15px;
                background: #ffffff;
                box-shadow: inset 0 2px 10px rgba(0,0,0,0.1);
            }
            
            .node {
                fill: #ff9a9e;
                stroke: #e91e63;
                stroke-width: 3px;
                cursor: pointer;
            }
            
            .node.component-1 { fill: #ff6b6b; }
            .node.component-2 { fill: #4ecdc4; }
            .node.component-3 { fill: #45b7d1; }
            .node.component-4 { fill: #96ceb4; }
            .node.component-5 { fill: #feca57; }
            
            .node.articulation {
                fill: #e74c3c;
                stroke: #c0392b;
                stroke-width: 5px;
            }
            
            .link {
                stroke: #bdc3c7;
                stroke-width: 3;
            }
            
            .link.bridge {
                stroke: #e74c3c;
                stroke-width: 6;
                stroke-dasharray: 5,5;
            }
            
            .node-label {
                font-size: 16px;
                font-weight: bold;
                text-anchor: middle;
                dominant-baseline: central;
                fill: white;
                pointer-events: none;
            }
            """;
    }
    
    private String getJavaScript(Map<Integer, List<Integer>> graph, List<Integer> dfsResult, List<Integer> bfsResult) {
        StringBuilder js = new StringBuilder();
        
        js.append("const graphData = ").append(graphToJSON(graph)).append(";\n");
        js.append("const dfsResult = ").append(listToJSON(dfsResult)).append(";\n");
        js.append("const bfsResult = ").append(listToJSON(bfsResult)).append(";\n\n");
        
        js.append("""
            const width = 800;
            const height = 600;
            
            const svg = d3.select('#graph')
                .append('svg')
                .attr('width', width)
                .attr('height', height);
            
            // Create force simulation
            const simulation = d3.forceSimulation(graphData.nodes)
                .force('link', d3.forceLink(graphData.links).id(d => d.id).distance(100))
                .force('charge', d3.forceManyBody().strength(-300))
                .force('center', d3.forceCenter(width / 2, height / 2));
            
            // Create links
            const link = svg.append('g')
                .selectAll('line')
                .data(graphData.links)
                .enter().append('line')
                .attr('class', 'link');
            
            // Create nodes
            const node = svg.append('g')
                .selectAll('circle')
                .data(graphData.nodes)
                .enter().append('circle')
                .attr('class', 'node')
                .attr('r', 20)
                .call(d3.drag()
                    .on('start', dragstarted)
                    .on('drag', dragged)
                    .on('end', dragended));
            
            // Add labels
            const label = svg.append('g')
                .selectAll('text')
                .data(graphData.nodes)
                .enter().append('text')
                .attr('class', 'node-label')
                .text(d => d.id);
            
            simulation.on('tick', () => {
                link.attr('x1', d => d.source.x)
                    .attr('y1', d => d.source.y)
                    .attr('x2', d => d.target.x)
                    .attr('y2', d => d.target.y);
                    
                node.attr('cx', d => d.x)
                    .attr('cy', d => d.y);
                    
                label.attr('x', d => d.x)
                     .attr('y', d => d.y);
            });
            
            function dragstarted(event, d) {
                if (!event.active) simulation.alphaTarget(0.3).restart();
                d.fx = d.x;
                d.fy = d.y;
            }
            
            function dragged(event, d) {
                d.fx = event.x;
                d.fy = event.y;
            }
            
            function dragended(event, d) {
                if (!event.active) simulation.alphaTarget(0);
                d.fx = null;
                d.fy = null;
            }
            
            function animateDFS() {
                resetVisualization();
                animateTraversal(dfsResult, 'DFS');
            }
            
            function animateBFS() {
                resetVisualization();
                animateTraversal(bfsResult, 'BFS');
            }
            
            function animateTraversal(result, type) {
                result.forEach((nodeId, index) => {
                    setTimeout(() => {
                        node.classed('visited', (d, i) => result.slice(0, index + 1).includes(d.id));
                        node.classed('current', d => d.id === nodeId);
                        
                        setTimeout(() => {
                            node.classed('current', false);
                        }, 800);
                    }, index * 1000);
                });
            }
            
            function resetVisualization() {
                node.classed('visited current', false);
                link.classed('active', false);
            }
            """);
        
        return js.toString();
    }
    
    private String getConnectivityJavaScript(Map<Integer, List<Integer>> graph,
                                           List<List<Integer>> connectedComponents,
                                           List<String> bridges,
                                           List<Integer> articulationPoints) {
        StringBuilder js = new StringBuilder();
        
        js.append("const graphData = ").append(graphToJSON(graph)).append(";\n");
        js.append("const connectedComponents = ").append(componentsToJSON(connectedComponents)).append(";\n");
        js.append("const bridges = ").append(bridgesToJSON(bridges)).append(";\n");
        js.append("const articulationPoints = ").append(listToJSON(articulationPoints)).append(";\n\n");
        
        js.append("""
            const width = 800;
            const height = 600;
            
            const svg = d3.select('#graph')
                .append('svg')
                .attr('width', width)
                .attr('height', height);
            
            // Create force simulation
            const simulation = d3.forceSimulation(graphData.nodes)
                .force('link', d3.forceLink(graphData.links).id(d => d.id).distance(100))
                .force('charge', d3.forceManyBody().strength(-300))
                .force('center', d3.forceCenter(width / 2, height / 2));
            
            // Create links
            const link = svg.append('g')
                .selectAll('line')
                .data(graphData.links)
                .enter().append('line')
                .attr('class', 'link');
            
            // Create nodes
            const node = svg.append('g')
                .selectAll('circle')
                .data(graphData.nodes)
                .enter().append('circle')
                .attr('class', 'node')
                .attr('r', 20)
                .call(d3.drag()
                    .on('start', dragstarted)
                    .on('drag', dragged)
                    .on('end', dragended));
            
            // Add labels
            const label = svg.append('g')
                .selectAll('text')
                .data(graphData.nodes)
                .enter().append('text')
                .attr('class', 'node-label')
                .text(d => d.id);
            
            simulation.on('tick', () => {
                link.attr('x1', d => d.source.x)
                    .attr('y1', d => d.source.y)
                    .attr('x2', d => d.target.x)
                    .attr('y2', d => d.target.y);
                    
                node.attr('cx', d => d.x)
                    .attr('cy', d => d.y);
                    
                label.attr('x', d => d.x)
                     .attr('y', d => d.y);
            });
            
            function dragstarted(event, d) {
                if (!event.active) simulation.alphaTarget(0.3).restart();
                d.fx = d.x;
                d.fy = d.y;
            }
            
            function dragged(event, d) {
                d.fx = event.x;
                d.fy = event.y;
            }
            
            function dragended(event, d) {
                if (!event.active) simulation.alphaTarget(0);
                d.fx = null;
                d.fy = null;
            }
            
            function highlightComponents() {
                resetVisualization();
                connectedComponents.forEach((component, index) => {
                    component.forEach(nodeId => {
                        node.filter(d => d.id === nodeId)
                            .classed(`component-${index + 1}`, true);
                    });
                });
            }
            
            function highlightBridges() {
                resetVisualization();
                bridges.forEach(bridge => {
                    const [source, target] = bridge.split('-').map(Number);
                    link.filter(d => 
                        (d.source.id === source && d.target.id === target) ||
                        (d.source.id === target && d.target.id === source)
                    ).classed('bridge', true);
                });
            }
            
            function highlightArticulationPoints() {
                resetVisualization();
                articulationPoints.forEach(nodeId => {
                    node.filter(d => d.id === nodeId)
                        .classed('articulation', true);
                });
            }
            
            function resetVisualization() {
                node.attr('class', 'node');
                link.attr('class', 'link');
            }
            """);
        
        return js.toString();
    }
    
    private String graphToJSON(Map<Integer, List<Integer>> graph) {
        StringBuilder json = new StringBuilder();
        json.append("{\"nodes\":[");
        
        Set<Integer> allNodes = new HashSet<>(graph.keySet());
        boolean first = true;
        for (int node : allNodes) {
            if (!first) json.append(",");
            json.append("{\"id\":").append(node).append("}");
            first = false;
        }
        
        json.append("],\"links\":[");
        Set<String> addedEdges = new HashSet<>();
        first = true;
        
        for (Map.Entry<Integer, List<Integer>> entry : graph.entrySet()) {
            int source = entry.getKey();
            for (int target : entry.getValue()) {
                String edge = Math.min(source, target) + "-" + Math.max(source, target);
                if (!addedEdges.contains(edge)) {
                    if (!first) json.append(",");
                    json.append("{\"source\":").append(source)
                        .append(",\"target\":").append(target).append("}");
                    addedEdges.add(edge);
                    first = false;
                }
            }
        }
        
        json.append("]}");
        return json.toString();
    }
    
    private String listToJSON(List<Integer> list) {
        return "[" + list.stream()
                .map(Object::toString)
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }
    
    private String componentsToJSON(List<List<Integer>> components) {
        StringBuilder json = new StringBuilder();
        json.append("[");
        for (int i = 0; i < components.size(); i++) {
            if (i > 0) json.append(",");
            json.append(listToJSON(components.get(i)));
        }
        json.append("]");
        return json.toString();
    }
    
    private String bridgesToJSON(List<String> bridges) {
        return "[" + bridges.stream()
                .map(s -> "\"" + s + "\"")
                .reduce((a, b) -> a + "," + b)
                .orElse("") + "]";
    }
}