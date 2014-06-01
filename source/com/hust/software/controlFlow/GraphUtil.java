package com.hust.software.controlFlow;

import com.intellij.openapi.diagnostic.Logger;

import java.util.*;

/**
 * This graph util is used to analyse directed graph
 * We assume only one start node, and one specify node(named reached) is given,
 * then all nodes can be grouped into three kinds: unreachable, articulation, redundant,
 * according to their status in the paths between start node and the specify reached node.
 * <p>
 * Created by Yan Yu on 2014-05-29.
 */
public class GraphUtil<E> {
    private static final Logger LOG = Logger.getInstance("#com.hust.software.controlFlow.GraphUtil<E>");

    public Map<E, Node<E>> nodesMap = new HashMap<>();
    private Node<E> start = null;
    private Node<E> reached = null;
    private List<Node<E>> exits = null;

    private boolean hasAnalysed = false;

    private Map<E, Node<E>> unreachableNodesMap = new HashMap<>();
    private Map<E, Node<E>> articulationNodesMap = new HashMap<>();
    private Map<E, Node<E>> redundantNodesMap = new HashMap<>();
    private Set<Node<E>> reachableNodes = new HashSet<>();

    public Map<E, Node<E>> getUnreachableNodesMap() {
        if (nonFeasible()) {
            return Collections.EMPTY_MAP;
        }
        anlysisGraph();
        return unreachableNodesMap;
    }

    public Map<E, Node<E>> getArticulationNodesMap() {
        if (nonFeasible()) {
            return Collections.EMPTY_MAP;
        }
        anlysisGraph();
        return articulationNodesMap;
    }

    public Map<E, Node<E>> getRedundantNodesMap() {
        if (nonFeasible()) {
            return Collections.EMPTY_MAP;
        }
        anlysisGraph();
        return redundantNodesMap;
    }

    public boolean nonFeasible() {
        return !isFeasible();
    }

    public boolean isFeasible() {
        if (reached == null) {
            return false;
        }

        return true;
    }

    private void anlysisGraph() {
        if (hasAnalysed) {
            return;
        }
        initAnalysisGraph();
        pruneUnreachableNodes();
        computeArticulationNodes();
        computeRedundantNodes();
        hasAnalysed = true;
    }

    private void pruneUnreachableNodes() {
        nodesMap.entrySet().stream()
                .filter(entry -> !reachableNodes.contains(entry.getKey()))
                .forEach(unreachableEntry ->
                        unreachableNodesMap.put(unreachableEntry.getKey(), unreachableEntry.getValue()));
    }

    private void computeArticulationNodes() {
        reachableNodes.stream()
                .filter(this::isArticulationNode)
                .forEach(articulationNode -> articulationNodesMap.put(articulationNode.getValue(), articulationNode));
    }

    private void computeRedundantNodes() {
        reachableNodes.stream()
                .filter(reachableNode -> !articulationNodesMap.values().contains(reachableNode))
                .forEach(redundantNode -> redundantNodesMap.put(redundantNode.getValue(), redundantNode));
    }

    private void clearVisited() {
        nodesMap.values().stream()
                .forEach(node -> node.visited = false);
    }

    private void initAnalysisGraph() {
        if (reached == null) {
            LOG.error("init analysis graph failed. reached node = null");
            return;
        }

        clearVisited();

        Stack<Node<E>> processingNodes = new Stack<>();
        processingNodes.push(reached);
        while (!processingNodes.isEmpty()) {
            Node<E> current = processingNodes.pop();
            if (current.allPred.size() == 0) {
                start = current;
            }
            if (current.visited) {
                continue;
            }
            reachableNodes.add(current);
            current.visited = true;
            current.allPred.stream()
                    .filter(node -> !node.visited)
                    .forEach(processingNodes::add);
        }
    }

    private boolean isArticulationNode(Node<E> node) {
        clearVisited();

        Stack<Node<E>> processingStack = new Stack<>();
        processingStack.push(start);
        while (!processingStack.isEmpty()) {
            Node<E> current = processingStack.pop();
            if (current.visited) {
                continue;
            }
            current.visited = true;
            if (current == node || !reachableNodes.contains(node)) {
                continue;
            }
            if (current.equals(reached)) {
                return false;
            }
            processingStack.addAll(current.allSucc);
        }

        return true;
    }

    public void setReached(E reached) {
        this.reached = getNode(reached);
    }

    public void addEdge(E first, E second) {

        Node<E> firstNode = getNode(first);
        Node<E> secondNode = getNode(second);
        firstNode.allSucc.add(secondNode);
        secondNode.allPred.add(firstNode);
    }

    private Node<E> getNode(E key) {
        Node<E> node = nodesMap.get(key);
        if (node == null) {
            node = new Node<>(key);
            nodesMap.put(key, node);
        }
        return node;
    }

    public class Node<E> {
        //    Instruction instruction;
        private E value;
        public boolean visited = false;
        public Collection<Node<E>> allSucc = new HashSet<>();
        public Collection<Node<E>> allPred = new HashSet<>();


        public Node(E value) {
            this.value = value;
        }

        public E getValue() {
            return value;
        }
    }
}

