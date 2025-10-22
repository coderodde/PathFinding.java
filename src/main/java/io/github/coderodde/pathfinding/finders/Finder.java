package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.ArrayList;
import java.util.Collections;
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
                               SearchState searchState,
                               SearchStatistics searchStatistics);
    
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
        tracebackPath(Cell touchCell,
                      Map<Cell, Cell> parentMapForward,
                      Map<Cell, Cell> parentMapBackward) {
            
        List<Cell> path = new ArrayList<>();
        Cell current = touchCell;
        
        while (current != null) {
            path.add(current);
            current = parentMapForward.get(current);
        }
        
        Collections.reverse(path);
        current = parentMapBackward.get(touchCell);
        
        while (current != null) {
            path.add(current);
            current = parentMapBackward.get(current);
        }
        
        return path;
    }
        
    public static List<Cell> expandPath(List<Cell> compressedPath,
                                        GridModel model) {
        
        if (compressedPath.size() < 2) {
            return List.of();
        }
        
        List<Cell> expandedPath = new ArrayList<>();
        
        for (int i = 0; i < compressedPath.size() - 1; ++i) {
            Cell cell1 = compressedPath.get(i);
            Cell cell2 = compressedPath.get(i + 1);
            
            List<Cell> interpolated = interpolate(cell1, 
                                                  cell2,
                                                  model);
            
            for (int j = 0; j < interpolated.size() - 1; ++j) {
                expandedPath.add(interpolated.get(j));
            }
        }
        
        expandedPath.add(compressedPath.getLast());
        
        return expandedPath;
    } 
        
    public static List<Cell> tracebackPathBiDijkstra(Cell touchf,
                                                     Cell touchb,
                                                     Map<Cell, Cell> parentsf,
                                                     Map<Cell, Cell> parentsB) {
        List<Cell> path = new ArrayList<>();
        
        Cell node = touchf;
        
        while (node != null) {
            path.add(node);
            node = parentsf.get(node);
        }
        
        Collections.reverse(path);
        node = touchb;
        
        while (node != null) {
            path.add(node);
            node = parentsB.get(node);
        }
        
        return path;
    }
        
    public static void searchSleep(PathfindingSettings pathfindingSettings) {
        try {
            Thread.sleep(pathfindingSettings.getWaitTime());
        } catch (InterruptedException ex) {
            System.getLogger(Finder.class.getName())
                  .log(System.Logger.Level.ERROR, (String) null, ex);
        }
    }
    
    public static double computePathCost(
            List<Cell> path,
            PathfindingSettings pathfindingSettings) {
        
        double cost = 0.0;
        
        for (int i = 0; i < path.size() - 1; ++i) {
            Cell c1 = path.get(i);
            Cell c2 = path.get(i + 1);
            cost += pathfindingSettings.getWeight(c1, c2);
        }
        
        return cost;
    }
    
    private static List<Cell> interpolate(Cell cell1, 
                                          Cell cell2,
                                          GridModel model) {
        
        List<Cell> line = new ArrayList<>();
        
        int x1 = cell1.getx();
        int y1 = cell1.gety();
        int x2 = cell2.getx();
        int y2 = cell2.gety();
        
        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);
        
        int sx = (x1 < x2) ? 1 : -1;
        int sy = (y1 < y2) ? 1 : -1;
        
        int err = dx - dy;
        
        while (true) {
            line.add(model.getCell(x1, y1));
            
            if (x1 == x2 && y1 == y2) {
                break;
            }
            
            int e2 = 2 * err;
            
            if (e2 > -dy) {
                err -= dy;
                x1 += sx;
            }
            
            if (e2 < dx) {
                err += dx;
                y1 += sy;
            }
        }
        
        return line;
    }
}
