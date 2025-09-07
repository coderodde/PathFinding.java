package io.github.coderodde.pathfinding.finders;

import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
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
                               SearchState searchState) {
        
        Cell source = model.getSourceGridCell();
        Cell target = model.getTargetGridCell();
        
        if (source.equals(target)) {
            return List.of(source);
        }
        
        Deque<Cell> backwardSearchStack = new ArrayDeque<>();
        Set<Cell> frontier              = new HashSet<>();
        Set<Cell> visitedForward        = new HashSet<>();
        Set<Cell> visitedBackward       = new HashSet<>();
        int previousVisitedSizeForward  = 0;
        int previousVisitedSizeBackward = 0;
        
        for (int depth = 0; ; ++depth) {
            
            clearFrontier(frontier,
                          model);
            
            clearVisited(visitedForward,
                         model);
            
            depthLimitedSearchForward(source,
                                      depth, 
                                      visitedForward, 
                                      frontier, 
                                      model,
                                      neighbourIterable, 
                                      pathfindingSettings,
                                      searchState);
            
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
                            searchState);
            
            if (meetingCell != null) {
                return buildPath(meetingCell,
                                 backwardSearchStack,
                                 model,
                                 neighbourIterable,
                                 pathfindingSettings, 
                                 searchState);
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
                            searchState);
            
            if (meetingCell != null) {
                return buildPath(meetingCell,
                                 backwardSearchStack,
                                 model,
                                 neighbourIterable,
                                 pathfindingSettings, 
                                 searchState);
            }
            
            if (visitedBackward.size() == previousVisitedSizeBackward) {
                return List.of();
            }
            
            previousVisitedSizeBackward = visitedBackward.size();
        }
    }
    
    private static List<Cell> buildPath(Cell meetingCell,
                                        Deque<Cell> backwardSearhStack,
                                        GridModel model,
                                        GridCellNeighbourIterable iterable,
                                        PathfindingSettings pathfindingSettings,
                                        SearchState searchState) {
        List<Cell> path = new ArrayList<>();
        model.moveTarget(meetingCell.getx(),
                         meetingCell.gety());
        
        List<Cell> prefixPath = 
                new BIDDFSFinder()
                        .findPath(model, 
                                  iterable, 
                                  pathfindingSettings, 
                                  searchState);
        
        path.addAll(prefixPath);
        path.remove(path.size() - 1);
        path.addAll(backwardSearhStack);
        
        return path;
    }
    
    private static void depthLimitedSearchForward(
            Cell node,
            int depth,
            Set<Cell> visitedForward,
            Set<Cell> frontier,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings ps,
            SearchState searchState) {
        
        if (searchState.haltRequested()) {
            return;
        }
        
        while (searchState.pauseRequested()) {
            searchSleep(ps);
        }
        
        searchSleep(ps);
        
        if (visitedForward.contains(node)) {
            return;
        }
        
        visitedForward.add(node);
        
        if (depth == 0) {
            frontier.add(node);
            model.setCellType(node, CellType.FREE);
            return;
        }
        
        model.setCellType(node, CellType.TRACED);
        iterable.setStartingCell(node);
        
        for (Cell child : iterable) {
            depthLimitedSearchForward(child,
                                      depth - 1, 
                                      visitedForward, 
                                      frontier, 
                                      model,
                                      iterable, 
                                      ps,
                                      searchState);
        }
        
        model.setCellType(node, CellType.FREE);
    }
    
    private static Cell depthLimitedSearchBackward(
            Cell cell,
            int depth,
            Set<Cell> visited,
            Set<Cell> frontier,
            Deque<Cell> backwardsStack,
            GridModel model,
            GridCellNeighbourIterable iterable,
            PathfindingSettings ps,
            SearchState searchState) {
        
        if (searchState.haltRequested()) {
            return null;
        }
        
        while (searchState.pauseRequested()) {
            searchSleep(ps);
        }
        
        model.setCellType(cell, CellType.TRACED);
        searchSleep(ps);
        
        if (visited.contains(cell)) {
            return null;
        }
        
        backwardsStack.addFirst(cell);
        
        if (depth == 0) {
            if (frontier.contains(cell)) {
                model.setCellType(cell, CellType.FREE);
                return cell;
            }
            
            model.setCellType(cell, CellType.FREE);
            backwardsStack.removeFirst();
            return null;
        }
        
        visited.add(cell);
        model.setCellType(cell, CellType.VISITED);
        
        iterable.setStartingCell(cell);
        
        for (Cell parent : iterable) {
            Cell meetingCell = 
                    depthLimitedSearchBackward(
                            parent, 
                            depth - 1, 
                            visited,
                            frontier, 
                            backwardsStack, 
                            model,
                            iterable,
                            ps,
                            searchState);
            
            if (meetingCell != null) {
                return meetingCell;
            }
        }
        
        backwardsStack.removeFirst();
        return null;
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
