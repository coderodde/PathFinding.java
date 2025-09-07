package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 7, 2025)
 * @since 1.0.0 (Sep 7, 2025)
 */
final class HeapNode implements Comparable<HeapNode> {

    Cell cell;
    double f;

    public HeapNode(Cell cell, double f) {
        this.cell = cell;
        this.f = f;
    }

    @Override
    public int compareTo(HeapNode o) {
        return Double.compare(f, o.f);
    }
}
