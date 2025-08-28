package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class BFSFinder implements Finder {

    @Override
    public List<Cell> findPath(Cell source, 
                               Cell target, 
                               GridModel model,
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState) {
        
        Map<Cell, Cell> parentMap = new HashMap<>();
        Deque<Cell> queue = new ArrayDeque<>();
        parentMap.put(source, null);
        queue.addLast(source);
        model.setCellType(source, CellType.OPENED);
        
        while (!queue.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = queue.removeFirst();
            model.setCellType(current, CellType.VISITED);
            
            if (current.equals(target)) {
                return tracebackPath(target, parentMap);
            }
            
            for (Cell neighbour : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }                
                
                if (neighbour == null) {
                    searchSleep(pathfindingSettings);
                    continue;
                }
                
                if (model.getCellType(neighbour).equals(CellType.VISITED)) {
                    continue;
                }
                
                model.setCellType(current, CellType.VISITED);
                parentMap.put(neighbour, current);
                queue.addLast(current);
            }
        }
        
        return List.of();
    }
}
