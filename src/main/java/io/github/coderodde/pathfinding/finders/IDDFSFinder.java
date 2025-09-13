package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 13, 2025)
 * @since 1.0.0 (Sep 13, 2025)
 */
public final class IDDFSFinder implements Finder {

    private enum Result {
        FOUND,
        CUTOFF,
        FAIL,
    }
    
    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        Set<Cell> onPath = new HashSet<>();
        List<Cell> path = new ArrayList<>();
        
        for (int depth = 0;; ++depth) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            while (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                
                if (searchState.haltRequested()) {
                    return List.of();
                }
            }
            
            path.clear();
            onPath.clear();
            
            Result result = depthLimitedSearch(
                               source,
                               target,
                               depth, 
                               path,
                               onPath,
                               model,
                               neighbourIterable, 
                               pathfindingSettings, 
                               searchState, 
                               searchStatistics);
            
            if (result == Result.FOUND) {
                Collections.reverse(path);
                return path;
            }
            
            if (result == Result.FAIL) {
                return List.of();
            }
        }
    }
    
    private static Result depthLimitedSearch(
            Cell cell, 
            Cell target,
            int depth,
            List<Cell> path,
            Set<Cell> onPath,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings pathfindingSettings,
            SearchState searchState,
            SearchStatistics searchStatistics) {
        
        if (cell.equals(target)) {
            path.add(target);
            return Result.FOUND;
        }
        
        if (depth == 0) {
            return Result.CUTOFF;
        }
        
        if (!onPath.add(cell)) {
            return Result.FAIL;
        }

        boolean anyCutoff = false;
        iterable.setStartingCell(cell);
        
        if (!cell.getCellType().equals(CellType.SOURCE) && 
            !cell.getCellType().equals(CellType.TARGET)) {
            model.setCellType(cell, CellType.TRACED);
        }
            
        searchStatistics.incrementTraced();
        
        for (Cell child : iterable) {
            if (onPath.contains(child)) {
                continue;
            }
            
            if (searchState.haltRequested()) {
                return Result.FAIL;
            }

            while (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);

                if (searchState.haltRequested()) {
                    return Result.FAIL;
                }
            }
            
            searchSleep(pathfindingSettings);
            
            Result result = 
                    depthLimitedSearch(child,
                                       target, 
                                       depth - 1, 
                                       path, 
                                       onPath,
                                       model,
                                       iterable, 
                                       pathfindingSettings, 
                                       searchState, 
                                       searchStatistics);
            
            if (result == Result.FOUND) {
                path.add(cell);
                onPath.remove(cell);
                return Result.FOUND;
            } else if (result == Result.CUTOFF) {
                anyCutoff = true;
            }
        }
        
        onPath.remove(cell);
        
        if (!cell.getCellType().equals(CellType.SOURCE) &&
            !cell.getCellType().equals(CellType.TARGET)) {
            model.setCellType(cell, CellType.FREE);
        }
        
        return anyCutoff ? Result.CUTOFF : Result.FAIL;
    }
}
