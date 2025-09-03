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
 * @version 1.0.0 (Sep 1, 2025)
 * @since 1.0.0 (Sep 1, 2025)
 */
public final class BidirectionalBFSFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState) {
        Deque<Cell> queuef = new ArrayDeque<>();
        Deque<Cell> queueb = new ArrayDeque<>();
        Map<Cell, Cell> parentsf = new HashMap<>();
        Map<Cell, Cell> parentsb = new HashMap<>();
        Map<Cell, Integer> distancef = new HashMap<>();
        Map<Cell, Integer> distanceb = new HashMap<>();
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        queuef.addLast(source);
        queueb.addLast(target);
        
        parentsf.put(source, null);
        parentsb.put(target, null);
        
        distancef.put(source, 0);
        distanceb.put(target, 0);
        
        int bestCost = Integer.MAX_VALUE;
        Cell touchCell = null;
        
        while (!queuef.isEmpty() && !queueb.isEmpty()) {
            int distf = distancef.get(queuef.getFirst());
            int distb = distanceb.get(queueb.getFirst());
            
            if (touchCell != null && bestCost < distf + distb) {
                return tracebackPath(touchCell, 
                                     parentsf,
                                     parentsb);
            }

            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            if (distf < distb) {
                Cell current = queuef.removeFirst();
                
                if (!current.getCellType().equals(CellType.SOURCE)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                System.out.println("fds " + current);
                
                if (distanceb.containsKey(current) &&
                        bestCost > distf + distb) {
                    
                    bestCost  = distf + distb;
                    touchCell = current;
                }
                
                neighbourIterable.setStartingCell(current);
                
                for (Cell neighbour : neighbourIterable) {
                    if (searchState.haltRequested()) {
                        return List.of();
                    }    
                    
                    if (parentsf.containsKey(neighbour)) {
                        continue;
                    }

                    searchSleep(pathfindingSettings);
                    
                    if (!neighbour.equals(source)) {
                        model.setCellType(neighbour, 
                                          CellType.OPENED);
                    }
                    
                    distancef.put(neighbour,
                                  distancef.get(current) + 1);
                    
                    parentsf.put(neighbour, 
                                 current);
                    
                    queuef.addLast(neighbour);
                }
            } else {
                Cell current = queueb.removeFirst();
                
                if (!current.getCellType().equals(CellType.TARGET)) {
                    model.setCellType(current, CellType.VISITED);
                }
                
                System.out.println("back " + current);
                
                if (distancef.containsKey(current)
                        && bestCost > distf + distb) {
                    bestCost = distf + distb;
                    touchCell = current;
                }
                
                neighbourIterable.setStartingCell(current);
                
                for (Cell neighbour : neighbourIterable) {
                    if (searchState.haltRequested()) {
                        return List.of();
                    }

                    if (parentsb.containsKey(neighbour)) {
                        continue;
                    }

                    searchSleep(pathfindingSettings);
                    
                    if (!neighbour.equals(target)) {
                        model.setCellType(neighbour, CellType.OPENED);
                    }
                    
                    distanceb.put(neighbour, distanceb.get(current) + 1);
                    parentsb.put(neighbour, current);
                    queueb.addLast(neighbour);
                }
            }
        }
        
        return List.of();
    }
}
