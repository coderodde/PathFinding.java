package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 10, 2025)
 * @since 1.0.0 (Sep 10, 2025)
 */
public final class IDAStarFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        double bound = h.estimate(source, target);
        List<Cell> path = new ArrayList<>(List.of(source));
        SolutionFound solutionFound = new SolutionFound();
        
        while (true) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            double t = search(path,
                              0.0,
                              bound,
                              model,
                              target,
                              neighbourIterable,
                              pathfindingSettings,
                              searchStatistics,
                              solutionFound,
                              searchState);
            
            if (solutionFound.found) {
                return path;
            }
            
            if (t == Double.POSITIVE_INFINITY) {
                return List.of();
            }
            
            bound = t;
        }
    }
    
    private static double search(List<Cell> path, 
                                 double g, 
                                 double bound,
                                 GridModel model,
                                 Cell target,
                                 GridCellNeighbourIterable iterable,
                                 PathfindingSettings pathfindingSettings,
                                 SearchStatistics searchStatistics,
                                 SolutionFound solutionFound,
                                 SearchState searchState) {
        
        if (searchState.haltRequested()) {
            return Double.NaN;
        }
        
        while (searchState.pauseRequested()) {
            searchSleep(pathfindingSettings);
            
            if (searchState.haltRequested()) {
                return Double.NaN;
            }
        }
        
        searchSleep(pathfindingSettings);
        searchStatistics.incrementTraced();
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        
        
        Cell cell = path.getLast();
        double f = g + h.estimate(cell, target);
        
        if (f > bound) {
            return f;
        }
        
        if (cell.equals(target)) {
            solutionFound.found = true;
            return f;
        }
        
        double min = Double.POSITIVE_INFINITY;
        iterable.setStartingCell(cell);
        
        for (Cell child : iterable) {
            if (!path.contains(child)) {
                path.add(child);
                // Color as TRACED:
                model.setCellType(path.getLast(), CellType.TRACED);
                
                double t = 
                        search(path,
                               g + pathfindingSettings.getWeight(cell, child), 
                               bound,
                               model,
                               target,
                               iterable,
                               pathfindingSettings,
                               searchStatistics,
                               solutionFound,
                               searchState);
                
                // UNcolor as TRACED:
                model.setCellType(path.getLast(), CellType.FREE);
                
                if (solutionFound.found) {
                    return Double.NaN;
                }
                
                if (min > t) {
                    min = t;
                }
                
                path.removeLast();
            }
        } 
        
        return min;
    }
    
    private static final class SolutionFound {
        boolean found = false;
    }
}
