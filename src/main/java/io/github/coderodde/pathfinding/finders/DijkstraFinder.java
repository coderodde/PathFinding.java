package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.ZeroHeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.List;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 7, 2025)
 * @since 1.0.0 (Sep 7, 2025)
 */
public final class DijkstraFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model,
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings,
                               SearchState searchState,
                               SearchStatistics searchStatistics) {
        
        HeuristicFunction oldHeuristicFunction = 
                pathfindingSettings.getHeuristicFunction();
        
        pathfindingSettings.setHeuristicFunction(new ZeroHeuristicFunction());
        
        List<Cell> path = 
                new AStarFinder()
                        .findPath(
                                model, 
                                neighbourIterable, 
                                pathfindingSettings,
                                searchState,
                                searchStatistics);
        
        pathfindingSettings.setHeuristicFunction(oldHeuristicFunction);
        return path;
    
    }
}
