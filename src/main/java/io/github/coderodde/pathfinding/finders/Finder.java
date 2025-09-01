package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public interface Finder {
    
    /**
     * Searches for a path from {@code source} to {@code target} in the grid 
     * {@code model} using a cell neighbour iterable {@code neighbourIterable}
     * and taking {@code pathfindingSettings} into account. The path is not 
     * necessarily optimal.
     * 
     * @param model
     * @param neighbourIterable
     * @param pathfindingSettings
     * @param searchState
     * @return 
     */
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState);
    
    public default List<Cell> 
        tracebackPath(Cell target, Map<Cell, Cell> parentMap) {
        List<Cell> path = new ArrayList<>();
        Cell current = target;
        
        while (current != null) {
            path.add(current);
            current = parentMap.get(current);
        }
        
        return path.reversed();
    }
     
    public default List<Cell> 
        tracebackPath(Cell target, 
                      Map<Cell, Cell> parentMapForward,
                      Map<Cell, Cell> parentMapBackward) {
        List<Cell> path = new ArrayList<>();
        Cell current = target;
        
        while (current != null) {
            path.add(current);
            current = parentMapForward.get(current);
        }
        
        current = parentMapBackward.get(target);
        
        while (current != null) {
            path.add(current);
            current = parentMapBackward.get(current);
        }
        
        return path.reversed();
    }
        
     
        
    public static void searchSleep(PathfindingSettings pathfindingSettings) {
        try {
            Thread.sleep(pathfindingSettings.getWaitTime());
        } catch (InterruptedException ex) {
            System.getLogger(Finder.class.getName())
                  .log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
}
