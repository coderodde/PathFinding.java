package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
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
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState) {
        
        
        Map<Cell, Cell> parentMap = new HashMap<>();
        Deque<Cell> queue = new ArrayDeque<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        parentMap.put(source, null);
        queue.addLast(source);
        
        while (!queue.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = queue.removeFirst();
            
            System.out.println(current);
            
            if (!current.equals(source)) {
                model.setCellType(current, CellType.VISITED);
            }
            
            if (current.equals(target)) {
                return tracebackPath(target, parentMap);
            }
            
            neighbourIterable.setStartingCell(current);
            
            for (Cell neighbour : neighbourIterable) {
                if (searchState.haltRequested()) {
                    return List.of();
                }                
                
                if (neighbour == null) {
                    searchSleep(pathfindingSettings);
                    continue;
                }
                
                if (parentMap.containsKey(neighbour)) {
                    continue;
                }
                
                searchSleep(pathfindingSettings);
                
                if (!neighbour.equals(target)) {
                    model.setCellType(neighbour, CellType.OPENED);
                }
                    
                parentMap.put(neighbour, current);
                queue.addLast(neighbour);
            }
        }
        
        return List.of();
    }
}
