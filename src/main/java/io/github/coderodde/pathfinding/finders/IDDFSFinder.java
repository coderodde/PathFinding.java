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
 * @version 1.0.0
 * @since 1.0.0
 */
public final class IDDFSFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        SolutionFound solutionFound = new SolutionFound();
        Set<Cell> visited = new HashSet<>();
        List<Cell> tentativePath = new ArrayList<>();
        List<Cell> optimalPath = new ArrayList<>();
        int previousVisitedSize = 0;
        
        for (int depth = 0;; ++depth) {
            depthLimitedSearch(source,
                               target,
                               depth, 
                               solutionFound, 
                               tentativePath,
                               optimalPath, 
                               visited,
                               model,
                               neighbourIterable, 
                               pathfindingSettings, 
                               searchState, 
                               searchStatistics);
            
            if (solutionFound.found) {
                Collections.reverse(optimalPath);
                return optimalPath;
            }
            
            if (previousVisitedSize == visited.size()) {
                return List.of();
            }
            
            previousVisitedSize = visited.size();
            visited.clear();
        }
    }
    
    private static void depthLimitedSearch(
            Cell cell, 
            Cell target,
            int depth,
            SolutionFound solutionFound,
            List<Cell> tentativePath,
            List<Cell> optimalPath,
            Set<Cell> visited,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings pathfindingSettings,
            SearchState searchState,
            SearchStatistics searchStatistics) {
        
        if (solutionFound.found) {
            return;
        }
        
        if (depth == 0 && cell.equals(target)) {
            solutionFound.found = true;
            optimalPath.addAll(tentativePath);
            optimalPath.add(target);
            return;
        }
        
        if (visited.contains(cell)) {
            return;
        }
        
        tentativePath.add(cell);
        visited.add(cell);
        
        if (!cell.getCellType().equals(CellType.SOURCE) &&
            !cell.getCellType().equals(CellType.TARGET)) {
            model.setCellType(cell, CellType.VISITED);
        }
        
        if (depth > 0) {
            iterable.setStartingCell(cell);
            
            for (Cell child : iterable) {
                if (visited.contains(child)) {
                    continue;
                }
                
                if (!child.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(child, CellType.TRACED);
                }
                
                searchStatistics.incrementTraced();
                searchSleep(pathfindingSettings);
                
                depthLimitedSearch(child, 
                                   target,
                                   depth - 1,
                                   solutionFound, 
                                   tentativePath,
                                   optimalPath, 
                                   visited, 
                                   model,
                                   iterable, 
                                   pathfindingSettings,
                                   searchState, 
                                   searchStatistics);
                
                if (!child.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(child, CellType.FREE);
                }
                
                if (solutionFound.found) {
                    return;
                }
            }
        }
        
        tentativePath.removeLast();
    }
    
    private static final class SolutionFound {
        boolean found = false;
    }
    
    private static final class Path {
        List<Cell> path = new ArrayList<>();
    }
}
