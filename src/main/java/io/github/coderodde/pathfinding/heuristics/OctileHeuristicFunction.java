package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * Implements the octile heuristic function.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.1.0 (Oct 21, 2025) 
 * @since 1.0.0
 */
public final class OctileHeuristicFunction implements HeuristicFunction {

    private static final double SQRT2 = Math.sqrt(2.0);
    private static final double FACTOR = SQRT2 - 1.0;
    
    /**
     * {@inheritDoc } 
     */
    @Override
    public double estimate(Cell cell1, Cell cell2) {
        int dx = Math.abs(cell1.getx() - cell2.getx());
        int dy = Math.abs(cell1.gety() - cell2.gety());
        return Math.max(dx, dy) + FACTOR * Math.min(dx, dy);
    }

    /**
     * {@inheritDoc } 
     */
    @Override
    public double estimate(double dx, double dy) {
        dx = Math.abs(dx);
        dy = Math.abs(dy);
        
        return (dx < dy) 
                ? FACTOR * dx + dy 
                : FACTOR * dy + dx;
    }
}
