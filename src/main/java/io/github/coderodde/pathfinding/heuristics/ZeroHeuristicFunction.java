package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * Implements a zero heuristic used in A* for simulating Dijkstra's algorithm.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 4, 2025)
 * @since 1.0.0 (Sep 4, 2025)
 */
public final class ZeroHeuristicFunction implements HeuristicFunction {

    @Override
    public double estimate(Cell cell1, Cell cell2) {
        return 0.0;
    }
    
    @Override
    public double estimate(double dx, double dy) {
        return 0.0;
    }
}
