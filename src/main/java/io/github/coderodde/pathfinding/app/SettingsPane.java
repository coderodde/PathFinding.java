package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.Configuration.MAXIMUM_FREQUENCY;
import static io.github.coderodde.pathfinding.Configuration.MINIMUM_FREQUENCY;
import io.github.coderodde.pathfinding.finders.BFSFinder;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchState.CurrentState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import java.util.List;
import java.util.Objects;
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
    private GridNodeExpander gridNodeExpander;
    private final SearchState searchState;
    
    public SettingsPane(GridModel gridModel, SearchState searchState) {
        this.gridModel = 
                Objects.requireNonNull(
                        gridModel, 
                        "The input grid model is null");
        
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
        
        for (int frequency = MINIMUM_FREQUENCY; 
                 frequency < MAXIMUM_FREQUENCY + 1; 
                 frequency++) {
            
            frequencyComboBox.getItems()
                             .add(String.format("%d Hz", frequency));
        }
        
        frequencyComboBox.setValue("10 Hz");
        
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
        
        
//        bfsVBoxAllowDiagonal.setOnAction(e -> {
//            this.bfsAllowDiagonals = bfsVBoxAllowDiagonal.isSelected();
//        });
//        
//        bfsVBoxDontCrossCorners.setOnAction(e -> {
//            this.bfsDontCrossCorners = bfsVBoxDontCrossCorners.isSelected();
//        });
//        
//        bfsVBoxBidirectional.setOnAction(e -> {
//            this.requestBiditectinal = bfsVBoxBidirectional.isSelected();
//        });
        
        bfsVBox.getChildren().addAll(bfsCheckBoxAllowDiagonal,
                                     bfsCehckBoxDontCrossCorners,
                                     bfsCheckBoxBidirectional);
        
        TitledPane bfsFinderSettingsPane = new TitledPane("BFS", bfsVBox);
        
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
            
            pathfindingSettings.setDiagonalWeight(PathfindingSettings.DiagonalWeight.UNIFORM);
            
            if (searchState.getCurrentState().equals(CurrentState.IDLE)) {
                // Once here, start search:
                searchState.setCurrentState(CurrentState.SEARCHING);
                startPauseButton.setText("Pause");
                
                BFSFinder finder = new BFSFinder();
                gridNodeExpander = new GridNodeExpander(gridModel, pathfindingSettings);
                
                List<Cell> path = 
                    finder.findPath(
                        this.gridModel,
                        new GridCellNeighbourIterable(
                                this.gridModel,
                                gridNodeExpander, 
                                pathfindingSettings),
                        pathfindingSettings,
                        searchState);
                
                System.out.println("Path: " + path);
                searchState.setCurrentState(CurrentState.IDLE);
                startPauseButton.setText("Search");
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
}
