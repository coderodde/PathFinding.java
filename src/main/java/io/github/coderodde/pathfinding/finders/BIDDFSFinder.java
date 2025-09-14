package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Sep 7, 2025)
 * @since 1.0.0 (Sep 7, 2025)
 */
public final class BIDDFSFinder implements Finder {

    @Override
    public List<Cell> findPath(GridModel model, 
                               GridCellNeighbourIterable neighbourIterable,
                               PathfindingSettings pathfindingSettings, 
                               SearchState searchState,
                               SearchStatistics searchStatistics) {
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        if (source.equals(target)) {
            return List.of(source);
        }
        
        Deque<Cell> backwardSearchStack = new ArrayDeque<>();
        Set<Cell> frontier              = new HashSet<>();
        Set<Cell> visitedForward        = new HashSet<>();
        Set<Cell> visitedBackward       = new HashSet<>();
        Map<Cell, Cell> parentForward   = new HashMap<>();
        int previousVisitedSizeForward  = 0;
        int previousVisitedSizeBackward = 0;
        
        for (int depth = 0; ; ++depth) {
            
            clearFrontier(frontier,
                          model);
            
            clearVisited(visitedForward,
                         model);
            
            visitedBackward.clear();
            backwardSearchStack.clear();
            parentForward.clear();
            
            depthLimitedSearchForward(source,
                                      depth, 
                                      visitedForward, 
                                      frontier,
                                      parentForward,
                                      model,
                                      neighbourIterable, 
                                      pathfindingSettings,
                                      searchState,
                                      searchStatistics);
            
            if (visitedForward.size() == previousVisitedSizeForward) {
                return List.of();
            }
            
            previousVisitedSizeForward = visitedForward.size();
            clearVisited(visitedBackward, model);
            
            Cell meetingCell = 
                    depthLimitedSearchBackward(
                            target,
                            depth, 
                            visitedBackward, 
                            frontier, 
                            backwardSearchStack, 
                            model, 
                            neighbourIterable,
                            pathfindingSettings, 
                            searchState,
                            searchStatistics);
            
            if (meetingCell != null) {
                return buildPath(meetingCell,
                                 parentForward,
                                 backwardSearchStack);
            }
            
            clearVisited(visitedBackward, 
                         model);

            meetingCell = 
                    depthLimitedSearchBackward(
                            target,
                            depth + 1, // We need this for correctness! 
                            visitedBackward, 
                            frontier, 
                            backwardSearchStack, 
                            model, 
                            neighbourIterable,
                            pathfindingSettings, 
                            searchState,
                            searchStatistics);
            
            if (meetingCell != null) {
                return buildPath(meetingCell,
                                 parentForward,
                                 backwardSearchStack);
            }
            
            if (visitedBackward.size() == previousVisitedSizeBackward) {
                return List.of();
            }
            
            previousVisitedSizeBackward = visitedBackward.size();
        }
    }
    
    private static void depthLimitedSearchForward(
            Cell node,
            int depth,
            Set<Cell> visitedForward,
            Set<Cell> frontier,
            Map<Cell, Cell> parents,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings ps,
            SearchState searchState,
            SearchStatistics searchStatistics) {
        
        searchStatistics.incrementTraced();
        
        if (searchState.haltRequested()) {
            return;
        }
        
        while (searchState.pauseRequested()) {
            searchSleep(ps);
            
            if (searchState.haltRequested()) {
                throw new HaltRequestedException();
            }
        }
        
        if (visitedForward.contains(node)) {
            return;
        }
        
        visitedForward.add(node);
        searchStatistics.incrementVisited();
        
        if (depth == 0) {
            frontier.add(node);
//            model.setCellType(node, CellType.TRACED);
            return;
        }
        
        if (!node.getCellType().equals(CellType.SOURCE)) {
            model.setCellType(node, CellType.TRACED);
        }
        
        iterable.setStartingCell(node);
        
        for (Cell child : iterable) {
            if (!visitedForward.contains(child)) {
                parents.put(child, node);
            }
            
            depthLimitedSearchForward(child,
                                      depth - 1, 
                                      visitedForward, 
                                      frontier,
                                      parents,
                                      model,
                                      iterable, 
                                      ps,
                                      searchState,
                                      searchStatistics);
        }
        
        model.setCellType(node, CellType.FREE);
    }
    
    private static Cell depthLimitedSearchBackward(
            Cell cell,
            int depth,
            Set<Cell> visitedBackward,
            Set<Cell> frontier,
            Deque<Cell> backwardsSearchStack,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings ps,
            SearchState searchState,
            SearchStatistics searchStatistics) {
        
        if (visitedBackward.contains(cell)) {
            return null;
        }
        
        visitedBackward.add(cell);
        backwardsSearchStack.addFirst(cell);
        searchStatistics.incrementTraced();
        
        if (frontier.contains(cell)) {
            return cell;
        }
        
        if (searchState.haltRequested()) {
            return null;
        }
        
        while (searchState.pauseRequested()) {
            searchSleep(ps);
            
            if (searchState.haltRequested()) {
                throw new HaltRequestedException();
            }
        }
        
        if (!cell.getCellType().equals(CellType.TARGET)) {
            model.setCellType(cell, CellType.TRACED);
        }
        
        searchSleep(ps);
        
        if (depth == 0) {
            backwardsSearchStack.removeFirst();
            return null;
        }
        
        iterable.setStartingCell(cell);
        
        for (Cell parent : iterable) {
            Cell meetingCell = 
                    depthLimitedSearchBackward(
                            parent, 
                            depth - 1, 
                            visitedBackward,
                            frontier, 
                            backwardsSearchStack, 
                            model,
                            iterable,
                            ps,
                            searchState,
                            searchStatistics);
            
            if (meetingCell != null) {
                return meetingCell;
            }
        }
        
        backwardsSearchStack.removeFirst();
        return null;
    }
    
    private static List<Cell> buildPath(Cell meetingNode,
                                        Map<Cell, Cell> parents,
                                        Deque<Cell> backwardSearchStack) {
        
        List<Cell> prefix = buildPathForwardOnly(meetingNode,
                                                 parents);
        List<Cell> path = new ArrayList<>();
        path.addAll(prefix);
        backwardSearchStack.removeFirst();
        path.addAll(backwardSearchStack);
        return path;
    }

    private static List<Cell> buildPathForwardOnly(Cell targetOrMeeting,
                                                   Map<Cell, Cell> parents) {
        List<Cell> path = new ArrayList<>();
        Cell current = targetOrMeeting;

        while (current != null) {
            path.add(current);
            current = parents.get(current);
        }

        Collections.reverse(path);
        return path;
    }
    
    private static void clearFrontier(Set<Cell> frontier, GridModel model) {
        for (Cell cell : frontier) {
            if (!cell.getCellType().equals(CellType.SOURCE)) {
                model.setCellType(cell, CellType.FREE);
            }
        }
        
        frontier.clear();
    }
    
    private static void clearVisited(Set<Cell> visited, GridModel model) {
        for (Cell cell : visited) {
            if (!cell.getCellType().equals(CellType.SOURCE) &&
                !cell.getCellType().equals(CellType.TARGET)) {
                model.setCellType(cell, CellType.FREE);
            }
        }
        
        visited.clear();
    }
}
