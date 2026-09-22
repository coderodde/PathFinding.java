package io.github.coderodde.pathfinding.logic;

import static io.github.coderodde.pathfinding.app.Configuration.FREQUENCIES;
import io.github.coderodde.pathfinding.finders.Finder;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.Objects;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class PathfindingSettings {
    
    public enum DiagonalWeight {
        UNIFORM(1.0),
        SQRT2(Math.sqrt(2.0));
        
        private final double weight;
        
        private DiagonalWeight(double weight) {
            this.weight = weight;
        }
        
        public double getWeight() {
            return weight;
        }
        
        public static DiagonalWeight convert(String txt) {
            switch (txt) {
                case "1" -> { 
                    return DiagonalWeight.UNIFORM;
                }
                    
                case "SQRT2" -> {
                    return DiagonalWeight.SQRT2;
                }
                    
                default -> throw new IllegalArgumentException(
                            String.format("%s not recognized", txt));
            }
        }
    }
    
    private boolean allowDiagonals;
    private boolean dontCrossCorners;
    private boolean bidirectional;
    private boolean dontSleep = false;
    private boolean dontColorCells = false;
    private int frequency = FREQUENCIES.getLast();
    private DiagonalWeight diagonalWeight = DiagonalWeight.SQRT2;
    private int beamWidth;
    private Finder finder;
    private double peaStarCutoff = 0.0;
    private int bfhsUpperBound = Integer.MAX_VALUE; // TODO: do I need this?

    private HeuristicFunction heuristicFunction;

    public boolean allowDiagonals() {
        return allowDiagonals;
    }

    public void setAllowDiagonals(boolean allowDiagonals) {
        this.allowDiagonals = allowDiagonals;
    }

    public boolean dontCrossCorners() {
        return dontCrossCorners;
    }

    public void setDontCrossCorners(boolean dontCrossCorners) {
        this.dontCrossCorners = dontCrossCorners;
    }

    public boolean isBidirectional() {
        return bidirectional;
    }
    
    public boolean dontColorCells() {
        return dontColorCells;
    }

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public boolean dontSleep() {
        return dontSleep;
    }
    
    public void setDontSleep(boolean dontSleep) {
        this.dontSleep = dontSleep;
    }
    
    public void setDontColorCells(boolean dontColorCells) {
        this.dontColorCells = dontColorCells;
    }
    
    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        this.frequency = frequency;
    }
    
    public DiagonalWeight getDiagonalWeight() {
        return diagonalWeight;
    }
    
    public void setDiagonalWeight(DiagonalWeight diagonalWeight) {
        this.diagonalWeight = 
                Objects.requireNonNull(
                        diagonalWeight, 
                        "The diagonal weight is null");
    }
    
    public int getBeamWidth() {
        return beamWidth;
    }
    
    public void setBeamWidth(int beamWidth) {
        System.out.println("Setting beam width " + beamWidth);
        if (beamWidth < 1) {
            throw new IllegalArgumentException(
                    String.format("beamWidth(%d) < 1", beamWidth));
        }
        
        this.beamWidth = beamWidth;
    }
    
    public HeuristicFunction getHeuristicFunction() {
        return heuristicFunction;
    }
    
    public void setHeuristicFunction(HeuristicFunction heuristicFunction) {
        this.heuristicFunction = heuristicFunction;
    }
    
    public long getWaitTime() {
        return 1000L / frequency;
    }
    
    public Finder getFinder() {
        return finder;
    }

    public void setFinder(Finder finder) {
        this.finder = finder;
    }
    
    public double getWeight(Cell cell1, Cell cell2) {
        int dx = Math.abs(cell1.getx() - cell2.getx());
        int dy = Math.abs(cell1.gety() - cell2.gety());
        
        if (dx > 0 && dy == 0) {
            return dx;
        }
        
        if (dx == 0 && dy > 0) {
            return dy;
        }
        
        if (dx > 0 && dy > 0) {
            if (dx != dy) {
                throw new IllegalStateException("Should not get here");
            }
            
//            System.out.println("[DEBUG] dx = " + dx + ", dy = " + dy);
            return dx * diagonalWeight.getWeight();
        }
        
        throw new IllegalStateException("Should not get here");
    }
    
    public double getPeaStarCutoff() {
        return peaStarCutoff;
    }

    public void setPeaStarCutoff(double cutoff) {
        if (cutoff < 0.0) {
            throw new IllegalArgumentException(
                    String.format("cutoff(%f) < 0.0", cutoff));
        }
        
        this.peaStarCutoff = cutoff;
    }
    
    public int getBfhsUpperBound() {
        return bfhsUpperBound;
    }
    
    public void setBfhsUpperBound(int bfhsUpperBound) {
        this.bfhsUpperBound = bfhsUpperBound;
    }
}
