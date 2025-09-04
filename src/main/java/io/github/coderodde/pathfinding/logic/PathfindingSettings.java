package io.github.coderodde.pathfinding.logic;

import static io.github.coderodde.pathfinding.app.Configuration.FREQUENCIES;
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
    }
    
    private boolean allowDiagonals;
    private boolean dontCrossCorners;
    private boolean bidirectional;
    private boolean dontSleep = true;
    private int frequency = FREQUENCIES.getLast();
    private DiagonalWeight diagonalWeight = DiagonalWeight.SQRT2;
    private int beamWidth;

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

    public void setBidirectional(boolean bidirectional) {
        this.bidirectional = bidirectional;
    }

    public boolean dontSleep() {
        return dontSleep;
    }
    
    public void setDontSleep(boolean dontSleep) {
        this.dontSleep = dontSleep;
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
        if (beamWidth < 1) {
            throw new IllegalArgumentException(
                    String.format("beamWidth(%d) < 1", beamWidth));
        }
        
        this.beamWidth = beamWidth;
    }
    
    public long getWaitTime() {
        return 1000L / frequency;
    }
}
