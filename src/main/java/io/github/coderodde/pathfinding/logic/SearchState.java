package io.github.coderodde.pathfinding.logic;

/**
 *
 * @author rodio
 */
public final class SearchState {
    
    private boolean requestHalt;
    
    public void resetState() {
        requestHalt = false;
    }
    
    public void requestHalt() {
        this.requestHalt = true;
    }
    
    public boolean haltRequested() {
        return requestHalt;
    }
}
