package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.finders.Finder;
import static io.github.coderodde.pathfinding.finders.Finder.searchSleep;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class GridCellNeighbourIterator implements Iterator<Cell> {

    private final PathfindingSettings pathfindingSettings;
    private final List<Cell> gridCellNeighbours;
    private int iterated = 0;
    
    public GridCellNeighbourIterator(Cell startingCell,
                                     GridModel gridModel,
                                     GridNodeExpander gridNodeExpander,
                                     PathfindingSettings pathfindingSettings) {
        
        this.pathfindingSettings = pathfindingSettings;
        this.gridCellNeighbours = gridNodeExpander.expand(startingCell);
    }
    
    @Override
    public boolean hasNext() {
        return iterated < gridCellNeighbours.size();
    }

    @Override
    public Cell next() {
        searchSleep(pathfindingSettings);
        return gridCellNeighbours.get(iterated++);
    }    
}
