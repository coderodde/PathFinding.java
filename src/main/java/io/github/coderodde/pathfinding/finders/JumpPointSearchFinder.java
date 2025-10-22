package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.finders.jps.jumpers.DiagonalCrossingJumper;
import io.github.coderodde.pathfinding.finders.jps.jumpers.DiagonalNonCrossingJumper;
import io.github.coderodde.pathfinding.finders.jps.jumpers.NoDiagonalJumper;
import io.github.coderodde.pathfinding.finders.jps.neighbourfinders.DiagonalCrossingNeighbourFinder;
import io.github.coderodde.pathfinding.finders.jps.neighbourfinders.DiagonalNoCrossingNeighbourFinder;
import io.github.coderodde.pathfinding.finders.jps.neighbourfinders.NoDiagonalNeighbourFinder;
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
 * This class implements the Jump Point Search.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Oct 19, 2025)
 * @since 1.0.0 (Oct 19, 2025)
 */
public final class JumpPointSearchFinder implements Finder {

    /**
     * This interface defines the API for computing neighbour cells of a given 
     * cell.
     */
    public interface NeighbourFinder {
        
        /**
         * Finds the neighbour cells of the cell {@code current}.
         * 
         * @param current    the cell whose neighbours to find.
         * @param parentsMap the map which maps cells to their respective parent
         *                   cells.
         * @param model      the grid model.
         * @param ps         the pathfinding settings.
         * @return 
         */
        List<Cell> findNeighbours(Cell current,
                                  Map<Cell, Cell> parentsMap,
                                  GridModel model,
                                  PathfindingSettings ps);
    }
    
    /**
     * This interface defines the API for jumping algorithms.
     */
    public interface Jumper {
        
        /**
         * Jumps to the next jump point cell.
         * 
         * @param x          the {@code x} coordinate of the current cell.
         * @param y          the {@code y} coordinate of the current cell.
         * @param px         the {@code x} coordinate of the parent cell.
         * @param py         the {@code y} coordinate of the parent cell.
         * @param model      the grid model.
         * 
         * @return the next jump point cell or {@code null} if there is no such.
         */
        Cell jump(int x,
                  int y,
                  int px,
                  int py,
                  GridModel model);
    }
    
    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable, 
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState, 
                               SearchStatistics searchStatistics) {
        
        NeighbourFinder neighbourFinder = 
                getNeighbourFinder(pathfindingSettings);
        
        Jumper jumper = getJumper(pathfindingSettings);
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        Queue<HeapNode> open          = new PriorityQueue<>();
        Set<Cell> openSet             = new HashSet<>();
        Set<Cell> closed              = new HashSet<>();
        Map<Cell, Double> distanceMap = new HashMap<>();
        Map<Cell, Cell> parentsMap    = new HashMap<>();
        
        open.add(new HeapNode(source, 0.0));
        openSet.add(source);
        parentsMap.put(source, null);
        distanceMap.put(source, 0.0);
        
        while (!open.isEmpty()) {
            if (searchState.haltRequested()) {
                return List.of();
            }
            
            if (searchState.pauseRequested()) {
                searchSleep(pathfindingSettings);
                continue;
            }
            
            Cell current = open.remove().cell;
            
            if (model.getCellType(current) != CellType.SOURCE &&
                model.getCellType(current) != CellType.TARGET) {
                model.setCellType(current, CellType.VISITED);
            }
            
            if (current.equals(target)) {
                return tracebackPath(target, 
                                     parentsMap);
            }
            
            searchStatistics.incrementVisited();
            closed.add(current);
            
            identifySuccessors(current,
                               open,
                               closed,
                               openSet,
                               distanceMap,
                               parentsMap,
                               model,
                               pathfindingSettings,
                               searchState,
                               searchStatistics,
                               neighbourFinder,
                               jumper);
        }
        
        return List.of();
    }
    
    /**
     * Returns the required neighbour finder.
     * 
     * @param pathfindingSettings the pathfinding settings object.
     * 
     * @return an instance of the suitable 
     *         {@link io.github.coderodde.pathfinding.finders.JumpPointSearchFinder.NeighbourFinder}.
     */
    private static NeighbourFinder 
        getNeighbourFinder(PathfindingSettings pathfindingSettings) {
        
        if (pathfindingSettings.allowDiagonals()) {
            if (pathfindingSettings.dontCrossCorners()) {
                return new DiagonalNoCrossingNeighbourFinder();
            } else {
                return new DiagonalCrossingNeighbourFinder();
            }
        } else {
            return new NoDiagonalNeighbourFinder();
        }
    }
    
    /**
     * Returns the required jumper.
     * 
     * @param pathfindingSettings the pathfinding settings object.
     * 
     * @return an instance of the suitable 
     *         {@link io.github.coderodde.pathfinding.finders.JumpPointSearchFinder.Jumper}.
     */
    private static Jumper getJumper(PathfindingSettings pathfindingSettings) {
        
        if (pathfindingSettings.allowDiagonals()) {
            if (pathfindingSettings.dontCrossCorners()) {
                return new DiagonalNonCrossingJumper();
            } else {
                return new DiagonalCrossingJumper();
            }
        } else {
            return new NoDiagonalJumper();
        }
    }
           
    private static void identifySuccessors(Cell current,
                                           Queue<HeapNode> open,
                                           Set<Cell> closed,
                                           Set<Cell> openSet,
                                           Map<Cell, Double> distanceMap,
                                           Map<Cell, Cell> parentsMap,
                                           GridModel model,
                                           PathfindingSettings ps,
                                           SearchState searchState,
                                           SearchStatistics searchStatistics,
                                           NeighbourFinder neighbourFinder,
                                           Jumper jumper) {
        
        List<Cell> neighbors = 
                neighbourFinder.findNeighbours(current,
                                               parentsMap,
                                               model,
                                               ps);
        
        int x = current.getx();
        int y = current.gety();
        HeuristicFunction hf = ps.getHeuristicFunction();
        
        for (Cell child : neighbors) {
            if (searchState.haltRequested()) {
                throw new HaltRequestedException();
            }
            
            while (searchState.pauseRequested()) {
                searchSleep(ps);
                
                if (searchState.haltRequested()) {
                    throw new HaltRequestedException();
                }
            }
            
            Cell jumpCell = jumper.jump(child.getx(),
                                        child.gety(),
                                        x,
                                        y,
                                        model);
            
            if (jumpCell == null) {
                continue;
            }
            
            int jx = jumpCell.getx();
            int jy = jumpCell.gety();
            jumpCell = model.getCell(jx, jy);
            
            if (closed.contains(jumpCell)) {
                continue;
            }
            
            double distance = hf.estimate(jx - x,
                                          jy - y);
            
            double nextg = distanceMap.get(current) + distance;
            
            if (!openSet.contains(jumpCell) ||
                nextg < distanceMap.get(jumpCell)) {
                
                distanceMap.put(jumpCell, nextg);
                
                double f = 
                        nextg + 
                        hf.estimate(jx - model.getTargetGridCell().getx(),
                                    jy - model.getTargetGridCell().gety());
                
                parentsMap.put(jumpCell, current);
                
                
                if (!openSet.contains(jumpCell)) {
                     openSet.add(jumpCell);
                }
                
                open.add(new HeapNode(jumpCell, f));
                
                searchStatistics.incrementOpened();
                searchSleep(ps);
            }
        }
    }
}
