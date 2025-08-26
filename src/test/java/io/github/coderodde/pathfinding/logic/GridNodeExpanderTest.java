package io.github.coderodde.pathfinding.logic;

import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import java.util.HashSet;
import java.util.Set;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public class GridNodeExpanderTest {
    
    private int wallBitFlags;
    private final GridNodeExpander expander = new GridNodeExpander();
    
    private Set<Cell> convertWallBitFlagsToFreeCellSet(GridModel model,
                                                       Cell cell,
                                                       PathfindingSettings ps) { 
        Set<Cell> set = new HashSet<>();
        
        for (int bitIndex = 0; bitIndex < Byte.SIZE; ++bitIndex) {
            int x = cell.getx() + X_DELTAS[bitIndex];
            int y = cell.gety() + Y_DELTAS[bitIndex];
            
            if (!model.isValidCellLocation(x, y)) {
                continue;
            }
            
            if (!ps.allowDiagonals()) {
                if (DIAGONAL_MOVES_FLAGS[bitIndex]) {
                    continue;
                }
                
                if (model.isValidCellLocation(x, y)) {
                    if (!wallBitOn(bitIndex)) {
                        set.add(new Cell(CellType.FREE, x, y));
                    }
                }
            } else if (ps.dontCrossCorners()) {
                // Here, diagonals with no crossing allowed!
                if (expander.canCrossNorthWest(cell.getx(), cell.gety())) {
                    if (!wallBitOn(bitIndex)) {
                        set.add(new Cell(CellType.FREE, x, y));
                    }
                }
                
                if (expander.canCrossNorthEast(cell.getx(), cell.gety())) {
                    
                }
            } else {
                // Here, diagonals with crossing allowed!
            }
            
            if (!wallBitOn(bitIndex)) {
                set.add(new Cell(CellType.FREE, x, y));
            }
        }
        
        return set;
    }
    
    private static final int[] X_DELTAS = {
        -1, 
        0,
        1,
        1,
        1,
        0,
        -1,
        -1,
    };
    
    private static final int[] Y_DELTAS = {
        -1,
        -1,
        -1,
        0,
        1,
        1,
        1,
        0,
    };
    
    private static final boolean[] DIAGONAL_MOVES_FLAGS = {
        true,
        false,
        true,
        false,
        true,
        false,
        true, 
        false,
    };
    
    public GridNodeExpanderTest() {
        
    }
    
    /**
     * Test of expand method, of class GridNodeExpander.
     */
    @Test
    public void stressTestExpandNoDiagonals() {
        PathfindingSettings pathfindingSettings = new PathfindingSettings();
        pathfindingSettings.setAllowDiagonals(false);
        
        final int N = 4;
        
        for (int cellY = 0; cellY < N; ++cellY) {
            for (int cellX = 0; cellX < N; ++cellX) {
                wallBitFlags = 0;
                
                for (int iteration = 0; 
                         iteration < 256; 
                         iteration++) {
                    
                    System.out.println("Y = " + cellY + ", X = " + cellX + ", iter = " + iteration);
                    
                    GridModel model = new GridModel(N, N);
                    Cell cell = model.getCell(cellX, cellY);
                    drawWalls(model, cell);
                    
                    expander.setGridModel(model);
                    expander.setPathfindingSettings(pathfindingSettings);
                    
                    Set<Cell> neighbours = new HashSet<>(expander.expand(cell));
                    ++wallBitFlags;
                    
//                    assertEquals(neighbours, 
//                                 convertWallBitFlagsToFreeCellSet(model,
//                                                                  cell));
                }
            }
        }
    }
    
    private void drawWalls(GridModel model, Cell cell) {
        int x = cell.getx();
        int y = cell.gety();
        
        for (int bitIndex = 0; 
                 bitIndex < Byte.SIZE; 
                 bitIndex++) {
            
            int nextXIndex = x + X_DELTAS[bitIndex];
            int nextYIndex = y + Y_DELTAS[bitIndex];

            if (!model.isValidCellLocation(nextXIndex, nextYIndex)) {
                continue;
            }
            
            if (wallBitOn(bitIndex)) {
                model.setCellType(nextXIndex,
                                  nextYIndex,
                                  CellType.WALL);
            } else {
                model.setCellType(nextXIndex,
                                  nextYIndex, 
                                  CellType.FREE);
            }
        }
    }
    
    private boolean wallBitOn(int bitIndex) {
        return ((wallBitFlags >>> bitIndex) & 1) != 0;
    }
}
