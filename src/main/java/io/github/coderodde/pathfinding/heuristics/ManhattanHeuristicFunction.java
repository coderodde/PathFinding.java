package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * Implements the Manhattan-heuristic function.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.1.0 (Oct 21, 2025) 
 * @since 1.0.0
 */
public class ManhattanHeuristicFunction implements HeuristicFunction {
    
    /**
     * {@inheritDoc }
     */
    @Override
    public double estimate(Cell cell1, Cell cell2) {
        int dx = Math.abs(cell1.getx() - cell2.getx());
        int dy = Math.abs(cell1.gety() - cell2.gety());
        return dx + dy;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public double estimate(double dx, double dy) {
        return Math.abs(dx) + Math.abs(dy);
    }
}
