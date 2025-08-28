package io.github.coderodde.pathfinding.logic;

/**
 *
 * @author rodio
 */
public final class SearchState {
    
    private boolean requestHalt;
    private boolean requestPause;
    
    public void resetState() {
        requestHalt  = false;
        requestPause = false;
    }
    
    public void requestHalt() {
        this.requestHalt = true;
    }
    
    public boolean haltRequested() {
        return requestHalt;
    }
    
    public void requestPause() {
        this.requestPause = true;
    }
    
    public boolean pauseRequested() {
        return requestPause;
    }
}
