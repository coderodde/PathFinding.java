package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * Implements the Chebyshev heuristic function.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.1.0 (Oct 21, 2025) 
 * @since 1.0.0
 */
public final class ChebyshevHeuristicFunction implements HeuristicFunction {

    /**
     * {@inheritDoc }
     */
    @Override
    public double estimate(Cell cell1, Cell cell2) {
        int dx = Math.abs(cell1.getx() - cell2.getx());
        int dy = Math.abs(cell1.gety() - cell2.gety());
        return Math.max(dx, dy);
    }

    /**
     * {@inheritDoc }.
     */
    @Override
    public double estimate(double dx, double dy) {
        return Math.max(Math.abs(dx), Math.abs(dy));
    }
}
