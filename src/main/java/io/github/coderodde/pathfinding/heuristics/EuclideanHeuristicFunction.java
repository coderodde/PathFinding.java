package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * Implements the Euclidean-heuristic function.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 
 * @since 1.0.0
 */
public final class EuclideanHeuristicFunction implements HeuristicFunction {

    @Override
    public double estimate(Cell cell1, Cell cell2) {
        int dx = cell1.getx() - cell2.getx();
        int dy = cell1.gety() - cell2.gety();
        return Math.sqrt(dx * dx + dy * dy);
    }
}
