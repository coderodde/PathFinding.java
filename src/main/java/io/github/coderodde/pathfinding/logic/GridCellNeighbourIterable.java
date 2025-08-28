package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.utils.Cell;
import java.util.Iterator;
import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class GridCellNeighbourIterable implements Iterable<Cell> {

    private Cell startingCell;
    private final GridNodeExpander gridNodeExpander;
    private final PathfindingSettings pathfindingSettings;
    
    public GridCellNeighbourIterable(GridNodeExpander gridNodeExpander,
                                     PathfindingSettings pathfindingSettings) {
        this.startingCell = 
                Objects.requireNonNull(
                        startingCell, 
                        "The input starting cell is null");
        
        this.gridNodeExpander = 
                Objects.requireNonNull(
                        gridNodeExpander,
                        "The input grid node expander is null");
        
        this.pathfindingSettings =
                Objects.requireNonNull(
                        pathfindingSettings, 
                        "The input pathfinding settings are null");
    }
    
    public void setStartingCell(Cell startingCell) {
        this.startingCell = startingCell;
    }
    
    @Override
    public Iterator<Cell> iterator() {
        return new GridCellNeighbourIterator(startingCell, 
                                             gridNodeExpander, 
                                             pathfindingSettings);
     
    }
}
