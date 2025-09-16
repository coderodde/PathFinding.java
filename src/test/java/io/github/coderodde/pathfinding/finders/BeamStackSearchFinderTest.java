package io.github.coderodde.pathfinding.finders;

import io.github.coderodde.pathfinding.heuristics.OctileHeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchStatistics;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.List;
import org.junit.Test;
import static org.junit.Assert.*;

public class BeamStackSearchFinderTest {
    
    private final PathfindingSettings ps = new PathfindingSettings();
    private final SearchState searchState = new SearchState();
    private final SearchStatistics searchStatistics =
            new SearchStatistics(null, 
                                 null,
                                 null,
                                 null);
    
    public BeamStackSearchFinderTest() {
        ps.setDontCrossCorners(true);
        ps.setFinder(new BeamStackSearchFinder());
        ps.setHeuristicFunction(new OctileHeuristicFunction());
        ps.setFrequency(1000);
    }
    
    @Test
    public void hasSolutionPath() {
        GridModel model = new GridModel(20, 5);
        model.moveTarget(17, 2);
        model.moveSource(15, 2);
        model.setCellType(16, 2, CellType.WALL);
        ps.setBeamWidth(1);
        
        GridNodeExpander expander = new GridNodeExpander(model, ps);
        
        GridCellNeighbourIterable iterable =
                new GridCellNeighbourIterable(model, 
                                              expander,
                                              ps);
        
        List<Cell> pathBreadthFirstSearch = 
                new BFSFinder()
                        .findPath(model, 
                                  iterable, 
                                  ps,
                                  searchState, 
                                  searchStatistics);
        
        List<Cell> pathBeamStackSearch = 
                new BeamStackSearchFinder()
                        .findPath(model, 
                                  iterable, 
                                  ps,
                                  searchState, 
                                  searchStatistics);
        
        System.out.println("BSS: " + pathBeamStackSearch.size());
        System.out.println("BFS: " + pathBreadthFirstSearch.size());
        
        assertEquals(pathBreadthFirstSearch.size(),
                     pathBeamStackSearch.size());
    }
    
    @Test
    public void debugNoPath() {
        GridModel model = new GridModel(53, 33);
        model.moveTarget(6, 4);
        model.moveSource(3, 4);
        model.setCellType(4, 2, CellType.WALL);
        model.setCellType(4, 3, CellType.WALL);
        model.setCellType(5, 3, CellType.WALL);
        model.setCellType(5, 4, CellType.WALL);
        model.setCellType(5, 5, CellType.WALL);
        
        ps.setBeamWidth(1);
        
        GridNodeExpander expander = new GridNodeExpander(model, ps);
        
        GridCellNeighbourIterable iterable =
                new GridCellNeighbourIterable(model, 
                                              expander,
                                              ps);
        
        List<Cell> pathBreadthFirstSearch = 
                new BFSFinder()
                        .findPath(model, 
                                  iterable, 
                                  ps,
                                  searchState, 
                                  searchStatistics);
        
        List<Cell> pathBeamStackSearch = 
                new BeamStackSearchFinder()
                        .findPath(model, 
                                  iterable, 
                                  ps,
                                  searchState, 
                                  searchStatistics);
        
        System.out.println("BSS: " + pathBeamStackSearch.size());
        System.out.println("BFS: " + pathBreadthFirstSearch.size());
        
        assertEquals(pathBreadthFirstSearch.size(),
                     pathBeamStackSearch.size());
    }
}
