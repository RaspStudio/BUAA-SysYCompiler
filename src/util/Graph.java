package util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

public class Graph<T extends Comparable<? super T>> {
    private final Map<T, Node<T>> nodeMap = new LinkedHashMap<>();
    private final Map<Node<T>, Map<Node<T>, Integer>> edgeMap = new LinkedHashMap<>();
    private final boolean directed;

    public Graph(boolean directed) {
        this.directed = directed;
    }

    /*========== 存活时间 ==========*/
    private final Map<T, Integer> liveTime = new LinkedHashMap<>();

    public void setLiveTime(T t, int time) {
        liveTime.merge(t, time, Integer::sum);
    }

    private int liveTime(T o) {
        return liveTime.getOrDefault(o, 0);
    }

    /*========== 节点操作 ==========*/
    public void addNodeIfAbsent(T value) {
        if (!nodeMap.containsKey(value)) {
            Node<T> node = new Node<>(value);
            nodeMap.put(value, node);
            edgeMap.put(node, new TreeMap<>());
        }
    }

    public void removeNode(T value) {
        Node<T> node = nodeMap.get(value);
        if (node == null) {
            return;
        }
        // 删除指向该节点的边
        if (directed) {
            // 如果是有向图，遍历所有节点，删除与该节点相关的边
            for (Node<T> n : edgeMap.keySet()) {
                edgeMap.get(n).remove(node);
            }
        } else {
            // 如果是无向图，可以根据无向边进行优化
            for (Node<T> n : edgeMap.get(node).keySet()) {
                edgeMap.get(n).remove(node);
            }
        }
        // 删除从节点出发的边
        edgeMap.remove(node);
        nodeMap.remove(value);
    }

    /*========== 边操作 ==========*/
    public void addEdge(T from, T to, int weight) {
        Node<T> fromNode = nodeMap.get(from);
        Node<T> toNode = nodeMap.get(to);
        edgeMap.get(fromNode).put(toNode, weight);
        if (!directed) {
            edgeMap.get(toNode).put(fromNode, weight);
        }
    }

    public void addEdgeAndNodeIfAbsent(T def, T use, int weight) {
        if (!hasNode(def)) {
            addNodeIfAbsent(def);
        }
        if (!hasNode(use)) {
            addNodeIfAbsent(use);
        }
        addEdge(def, use, weight);
    }

    public void addEdgeIfPresent(T from, T to, int weight) {
        Node<T> fromNode = nodeMap.get(from);
        Node<T> toNode = nodeMap.get(to);
        if (fromNode != null && toNode != null) {
            edgeMap.get(fromNode).put(toNode, weight);
            if (!directed) {
                edgeMap.get(toNode).put(fromNode, weight);
            }
        }
    }

    public void removeEdge(T from, T to) {
        Node<T> fromNode = nodeMap.get(from);
        Node<T> toNode = nodeMap.get(to);
        edgeMap.get(fromNode).remove(toNode);
        if (!directed) {
            edgeMap.get(toNode).remove(fromNode);
        }
    }

    /*========== 查询 ==========*/
    public boolean hasEdge(T from, T to) {
        Node<T> fromNode = nodeMap.get(from);
        Node<T> toNode = nodeMap.get(to);
        return edgeMap.get(fromNode).containsKey(toNode);
    }

    public SortedSet<T> getNodes() {
        TreeSet<T> ret = new TreeSet<>(((o1, o2) -> {
            // 存活长的节点放后面
            if (liveTime(o1) != liveTime(o2)) {
                return liveTime(o1) - liveTime(o2);
            }
            // 度数高的节点放后面（冲突多）
            if (getDegree(o1) != getDegree(o2)) {
                return getDegree(o1) - getDegree(o2);
            }
            // 度数相同，则按照原有溢出优先级排序
            return o1.compareTo(o2);
        }));
        ret.addAll(nodeMap.keySet());
        return ret;
    }

    public int getDegree(T value) {
        Node<T> node = nodeMap.get(value);
        return edgeMap.get(node).size();
    }

    public Graph<T> copy() {
        Graph<T> graph = new Graph<>(directed);
        for (T value : nodeMap.keySet()) {
            graph.addNodeIfAbsent(value);
        }
        for (Node<T> from : edgeMap.keySet()) {
            for (Node<T> to : edgeMap.get(from).keySet()) {
                graph.addEdge(from.value, to.value, edgeMap.get(from).get(to));
            }
        }
        for (T t : liveTime.keySet()) {
            graph.setLiveTime(t, liveTime(t));
        }
        return graph;
    }

    public SortedSet<T> getNeighbors(T value) {
        Node<T> node = nodeMap.get(value);
        SortedSet<T> neighbors = new TreeSet<>();
        for (Node<T> n : edgeMap.get(node).keySet()) {
            neighbors.add(n.value);
        }
        return neighbors;
    }

    public boolean hasNode(T node) {
        return nodeMap.containsKey(node);
    }

    public boolean isEmpty() {
        return nodeMap.isEmpty();
    }

    static class Node<T extends Comparable<? super T>> implements Comparable<Node<T>> {
        private final T value;

        public Node(T value) {
            if (value == null) {
                throw new IllegalArgumentException("value cannot be null");
            }
            this.value = value;
        }

        public T getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Node && ((Node<?>) o).value.equals(value);
        }

        @Override
        public int hashCode() {
            return value.hashCode();
        }

        @Override
        public int compareTo(Node<T> o) {
            return value.compareTo(o.value);
        }
    }

}
