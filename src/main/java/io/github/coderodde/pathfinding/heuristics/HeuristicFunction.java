package io.github.coderodde.pathfinding.heuristics;

import io.github.coderodde.pathfinding.utils.Cell;

/**
 * This interface defines the API for heuristic functions.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 
 * @since 1.0.0
 */
public interface HeuristicFunction {

    /**
     * Computes the distance estimate between {@code cell1} and {@code cell2}.
     * Must be optimistic, in other words, should not overestimate the distance.
     * 
     * @param cell1 a first cell.
     * @param cell2 a second cell.
     * @return the distance estimate.
     */
    public double estimate(Cell cell1, Cell cell2);
}
