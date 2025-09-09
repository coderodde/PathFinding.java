package io.github.coderodde.pathfinding.logic;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 9, 2025)
 * @since 1.0.0 (Sep 9, 2025)
 */
public final class SearchStatistics {
    
    private int visited;
    private int opened;
    private int traced;
    
    public void incrementVisited() {
        visited++;
    }
    
    public void incrementOpened() {
        opened++;
    }
    
    public void decrementOpened() {
        opened--;
    }
    
    public void incrementTraced() {
        traced++;
    }

    public int getVisited() {
        return visited;
    }

    public int getOpened() {
        return opened;
    }

    public int getTraced() {
        return traced;
    }
}
