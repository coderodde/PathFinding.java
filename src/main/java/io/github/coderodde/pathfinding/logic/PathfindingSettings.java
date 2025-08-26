package io.github.coderodde.pathfinding.logic;

/**
 *
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 26, 2025)
 * @since 1.0.0 (Aug 26, 2025)
 */
public final class PathfindingSettings {
    
    private boolean allowDiagonals;
    private boolean dontCrossCorners;
    private boolean bidirectional;
    private int frequency;

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

    public int getFrequency() {
        return frequency;
    }

    public void setFrequency(int frequency) {
        checkFrequency(frequency);
        this.frequency = frequency;
    }
    
    private void checkFrequency(int frequency) {
        if (frequency < 1) {
            throw new IllegalArgumentException(
                    String.format("frequency(%d) < 1", 
                                  frequency));
        }
        
        if (frequency > 10) {
            throw new IllegalArgumentException(
                    String.format("frequency(%d) > 10", 
                                  frequency));
        }
    }
}
