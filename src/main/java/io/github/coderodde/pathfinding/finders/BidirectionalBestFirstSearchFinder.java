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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 10, 2025)
 * @since 1.0.0 (Sep 10, 2025)
 */
public final class BidirectionalBestFirstSearchFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        Queue<HeapNode> openf = new PriorityQueue<>();
        Queue<HeapNode> openb = new PriorityQueue<>();
        
        Set<Cell> closedf = new HashSet<>();
        Set<Cell> closedb = new HashSet<>();
        
        Map<Cell, Cell> parentsf = new HashMap<>();
        Map<Cell, Cell> parentsb = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        HeuristicFunction h = pathfindingSettings.getHeuristicFunction();
        
        openf.add(new HeapNode(source, 0.0));
        openb.add(new HeapNode(target, 0.0));
        
        searchStatistics.addToOpened(2); // Count source and target.
        
        parentsf.put(source, null);
        parentsb.put(target, null);
        
        while (!openf.isEmpty() && !openb.isEmpty()) {
            if (openf.size() <= openb.size()) {
                Cell current = openf.remove().cell;
                
                if (closedb.contains(current)) {
                    return tracebackPath(current, 
                                         parentsf, 
                                         parentsb);
                }
                
                if (!current.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                closedf.add(current);
                searchStatistics.decrementOpened();
                searchStatistics.incrementVisited();
                neighbourIterable.setStartingCell(current);
                
                for (Cell child : neighbourIterable) {
                    if (closedf.contains(child)) {
                        continue;
                    }
                    
                    if (!parentsf.containsKey(child)) {
                        searchSleep(pathfindingSettings);
                        model.setCellType(child, CellType.OPENED);
                        
                        parentsf.put(child, current);
                        openf.add(
                                new HeapNode(
                                        child,
                                        h.estimate(child, target)));
                        
                        searchStatistics.incrementOpened();
                    }
                }
            } else {
                Cell current = openb.remove().cell;
                
                if (closedf.contains(current)) {
                    return tracebackPath(current,
                                         parentsf, 
                                         parentsb);
                }
                
                if (!current.getCellType().equals(CellType.TARGET)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                closedb.add(current);
                searchStatistics.decrementOpened();
                searchStatistics.incrementVisited();
                neighbourIterable.setStartingCell(current);
                
                for (Cell parent : neighbourIterable) {
                    if (closedb.contains(parent)) {
                        continue;
                    }
                    
                    if (!parentsb.containsKey(parent)) {
                        searchSleep(pathfindingSettings);
                        model.setCellType(parent, CellType.OPENED);
                        
                        parentsb.put(parent, current);
                        openb.add(
                                new HeapNode(
                                        parent,
                                        h.estimate(parent, source)));
                        
                        searchStatistics.incrementOpened();
                    }
                }
            }
        }
    
        return List.of();
    }
}
