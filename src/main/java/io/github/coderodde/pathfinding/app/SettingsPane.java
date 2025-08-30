package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.Configuration.FREQUENCIES;
import io.github.coderodde.pathfinding.controller.GridController;
import io.github.coderodde.pathfinding.finders.BFSFinder;
import io.github.coderodde.pathfinding.finders.Finder;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchState.CurrentState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.List;
import java.util.Objects;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TitledPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.Screen;

/**
 *
 * @author Rodion "rodde" EFremov
 * @version 1.0.0 (Aug 27, 2025)
 * @since 1.0.0 (Aug 27, 2025)
 */
public final class SettingsPane extends Pane {
    
    private static final int PIXELS_WIDTH  = 150;
    private static final int PIXELS_HEIGHT = 300;
    private static final int PIXELS_MARGIN = 20;
    private final double[] offset = new double[2];
    private GridModel gridModel;
    private GridView gridView;
    private GridController gridController;
    private GridNodeExpander gridNodeExpander;
    private final SearchState searchState;
    private Finder finder;
    
    public SettingsPane(GridModel gridModel,
                        GridView gridView,
                        GridController gridController,
                        SearchState searchState) {
        this.gridModel = 
                Objects.requireNonNull(
                        gridModel, 
                        "The input grid model is null");
        
        this.gridView = 
                Objects.requireNonNull(
                        gridView, 
                        "The input grid view is null");
        
        this.searchState = searchState;
        this.searchState.setCurrentState(CurrentState.IDLE);
        
        setPrefSize(PIXELS_WIDTH,
                    PIXELS_HEIGHT);
        
        setMinSize(USE_PREF_SIZE, USE_PREF_SIZE);
        setMaxSize(USE_PREF_SIZE, USE_PREF_SIZE);
        
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        
        setLayoutX(screenRectangle.getWidth()  - PIXELS_WIDTH - PIXELS_MARGIN);
        setLayoutY(PIXELS_MARGIN);
        
        VBox mainVBox = new VBox();
        
        ComboBox<String> frequencyComboBox = new ComboBox<>();
        frequencyComboBox.setPrefWidth(PIXELS_WIDTH);
        
        for (Integer frequency : FREQUENCIES) {
            
            frequencyComboBox.getItems()
                             .add(String.format("%d Hz", frequency));
        }
        
        frequencyComboBox.setValue(
                String.format("%d Hz", FREQUENCIES.getLast()));
        
        TitledPane frequencyTitledPane = new TitledPane("Frequency", 
                                                        frequencyComboBox);
        
        ComboBox<String> diagonalWeightComboBox = new ComboBox<>();
        diagonalWeightComboBox.getItems().add("1");
        diagonalWeightComboBox.getItems().add("SQRT2");
        diagonalWeightComboBox.setPrefWidth(PIXELS_WIDTH);
        diagonalWeightComboBox.setValue("SQRT2");
        
        TitledPane diagonalWeightTitledPane = 
                new TitledPane(
                        "Diagonal weight", 
                        diagonalWeightComboBox);
        
        VBox bfsVBox = new VBox();
        
        CheckBox bfsCheckBoxAllowDiagonal    = new CheckBox("Allow diagonal");
        CheckBox bfsCheckBoxBidirectional    = new CheckBox("Bidirectinal");
        CheckBox bfsCehckBoxDontCrossCorners = 
                new CheckBox("Don't cross coreners");
        
        bfsVBox.getChildren().addAll(bfsCheckBoxAllowDiagonal,
                                     bfsCehckBoxDontCrossCorners,
                                     bfsCheckBoxBidirectional);
        
        TitledPane bfsFinderSettingsPane = new TitledPane("BFS", bfsVBox);
        bfsFinderSettingsPane.setExpanded(true);
        
        Accordion accordion = new Accordion();  
        accordion.setPrefWidth(PIXELS_WIDTH);
        accordion.getPanes().addAll(frequencyTitledPane, 
                                    diagonalWeightTitledPane,
                                    bfsFinderSettingsPane);
        
        mainVBox.getChildren().add(accordion);
        
        getChildren().add(mainVBox);
        
        VBox buttonVBox = new VBox();
        
        Button startPauseButton = new Button("Start");
        Button resetButton      = new Button("Reset");
        Button clearWallsButton = new Button("Clear walls");
        
        startPauseButton.setPrefWidth(PIXELS_WIDTH);
        resetButton     .setPrefWidth(PIXELS_WIDTH);
        clearWallsButton.setPrefWidth(PIXELS_WIDTH);
        
        startPauseButton.setOnAction(event -> {
            
            PathfindingSettings pathfindingSettings = new PathfindingSettings();
            
            pathfindingSettings.setAllowDiagonals(
                    bfsCheckBoxAllowDiagonal.isSelected());
            
            pathfindingSettings.setBidirectional(
                    bfsCheckBoxBidirectional.isSelected());
            
            pathfindingSettings.setDontCrossCorners(
                    bfsCehckBoxDontCrossCorners.isSelected());
            
            pathfindingSettings.setDiagonalWeight(
                    PathfindingSettings.DiagonalWeight.SQRT2);
            
             // Search finder should sleep on neighbours:
            pathfindingSettings.setDontSleep(false);
            
            if (searchState.getCurrentState().equals(CurrentState.IDLE)) {
                // Once here, start search:
                searchState.setCurrentState(CurrentState.SEARCHING);
                gridController.disableUserInteraction();
                startPauseButton.setText("Pause");
                
                BFSFinder finder = new BFSFinder();
                gridNodeExpander = new GridNodeExpander(gridModel,
                                                        pathfindingSettings);
                
                Task<List<Cell>> task = new Task<>() {
                    @Override
                    protected List<Cell> call() throws Exception {
                        return finder.findPath(
                                    gridModel,
                                    new GridCellNeighbourIterable(
                                            gridModel,
                                            gridNodeExpander, 
                                            pathfindingSettings),
                                    pathfindingSettings,
                                    searchState);
                    }
                };
                
                task.setOnSucceeded(e -> {
                    List<Cell> path = task.getValue();
                    gridView.drawPath(path);
                    gridController.enableUserInteraction();
                    searchState.setCurrentState(CurrentState.IDLE);
                    startPauseButton.setText("Search");
                });
                
                new Thread(task).start();
            } else if (searchState
                    .getCurrentState()
                    .equals(CurrentState.SEARCHING)) {
                
                // Once here, we need to pause the search:
                searchState.requestPause();
                searchState.setCurrentState(CurrentState.PAUSED);
                startPauseButton.setText("Continue");
            } else if (searchState.getCurrentState()
                                  .equals(CurrentState.PAUSED)) {
                searchState.resetState();
                searchState.setCurrentState(CurrentState.SEARCHING);
                startPauseButton.setText("Pause");
            }
        });
        
        resetButton.setOnAction(event -> {
            searchState.requestHalt();
            gridModel.reinit();
        });
        
        clearWallsButton.setOnAction(event -> {
            searchState.requestHalt();
            gridModel.clearWalls();
        });    
        
        buttonVBox.getChildren().addAll(startPauseButton,
                                        resetButton,
                                        clearWallsButton);
        
        mainVBox.getChildren().add(buttonVBox);
        
        setStyle("-fx-background-color: rgba(0, 0, 0, 0.3);"
               + "-fx-background-radius: 8;"
               + "-fx-border-color: gray;"
               + "-fx-border-radius: 8;");
        
        setOnMousePressed(event -> {
            offset[0] = event.getSceneX() - getLayoutX();
            offset[1] = event.getSceneY() - getLayoutY();
        });
        
        setOnMouseDragged(event -> {
            setLayoutX(event.getSceneX() - offset[0]);
            setLayoutY(event.getSceneY() - offset[1]);
        });
    }
    
    public SearchState getSearchState() {
        return searchState;
    }
}
