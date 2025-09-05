package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.app.Configuration.FREQUENCIES;
import io.github.coderodde.pathfinding.controller.GridController;
import io.github.coderodde.pathfinding.finders.BFSFinder;
import io.github.coderodde.pathfinding.finders.BeamSearchFinder;
import io.github.coderodde.pathfinding.finders.BestFirstSearchFinder;
import io.github.coderodde.pathfinding.finders.BidirectionalBFSFinder;
import io.github.coderodde.pathfinding.finders.Finder;
import io.github.coderodde.pathfinding.heuristics.ChebyshevHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.EuclideanHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.ManhattanHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.OctileHeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.SearchState;
import io.github.coderodde.pathfinding.logic.SearchState.CurrentState;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.view.GridView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutionException;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Rectangle2D;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
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
    
    private static final String EUCLIDEAN = "Euclidean";
    private static final String MANHATTAN = "Manhattan";
    private static final String OCTILE    = "Octile";
    private static final String CHEBYSHEV = "Chebyshev";
    
    private static final String[] HEURISTIC_NAMES = {
        EUCLIDEAN,
        MANHATTAN,
        OCTILE,
        CHEBYSHEV,
    };
    
    private static final Map<String, HeuristicFunction> HEURISTIC_MAP =
            new HashMap<>();
    
    static {
        HEURISTIC_MAP.put(EUCLIDEAN, new EuclideanHeuristicFunction());
        HEURISTIC_MAP.put(MANHATTAN, new ManhattanHeuristicFunction());
        HEURISTIC_MAP.put(OCTILE,    new OctileHeuristicFunction());
        HEURISTIC_MAP.put(CHEBYSHEV, new ChebyshevHeuristicFunction());
    }
    
    private static final int PIXELS_WIDTH  = 300;
    private static final int PIXELS_HEIGHT = 200;
    private static final int PIXELS_MARGIN = 20;
    private final double[] offset = new double[2];
    private GridModel gridModel;
    private GridView gridView;
    private GridController gridController;
    private GridNodeExpander gridNodeExpander;
    private final SearchState searchState;
    private Finder finder;
    private List<Cell> path = new ArrayList<>();
    
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
        
        setMinSize(PIXELS_WIDTH, 
                   PIXELS_HEIGHT);
        
        setMaxSize(PIXELS_WIDTH, 
                   PIXELS_HEIGHT);
        
        Rectangle2D screenRectangle = Screen.getPrimary().getBounds();
        
        setLayoutX(screenRectangle.getWidth() - PIXELS_WIDTH - PIXELS_MARGIN);
        setLayoutY(PIXELS_MARGIN);
        
        VBox mainVBox = new VBox();
        
        mainVBox.setPrefSize(PIXELS_WIDTH,
                             PIXELS_HEIGHT);
        
        mainVBox.setMinSize(PIXELS_WIDTH,
                            PIXELS_HEIGHT);   
        
        mainVBox.setMaxSize(PIXELS_WIDTH,
                            PIXELS_HEIGHT);   
        
        ComboBox<String> beamWidthComboBox = new ComboBox<>();
        
        for (int beamWidth = 1; beamWidth <= 8; ++beamWidth) {
            beamWidthComboBox.getItems().add(String.format("%d", beamWidth));
        }
        
        ComboBox<String> heuristicComboBox = new ComboBox<>();
        
        for (String heuristicName : HEURISTIC_NAMES) {
            heuristicComboBox.getItems().add(heuristicName);
        }
        
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
        
        TitledPane heuristicFunctionTitledPane = 
                new TitledPane(
                        "Heuristic function", 
                        heuristicComboBox);
        
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
                new CheckBox("Don't cross corenrs");
        
        bfsVBox.getChildren().addAll(bfsCheckBoxAllowDiagonal,
                                     bfsCehckBoxDontCrossCorners,
                                     bfsCheckBoxBidirectional);
        
        mainVBox.setPrefWidth(bfsVBox.getWidth());
        
        bfsCheckBoxAllowDiagonal.setSelected(true);
        
        TitledPane bfsFinderSettingsPane = new TitledPane("BFS", bfsVBox);
        
        bfsFinderSettingsPane.setExpanded(true);
        
        TitledPane beamFinderSettingsPane = 
                new TitledPane("Beam width", beamWidthComboBox);
        
        beamWidthComboBox.setValue("1");
        heuristicComboBox.setValue(MANHATTAN);
        
        beamWidthComboBox.setPrefWidth(PIXELS_WIDTH);
        heuristicComboBox.setPrefWidth(PIXELS_WIDTH);
        
        Accordion accordion = new Accordion();  
        accordion.setPrefWidth(PIXELS_WIDTH);
        accordion.getPanes().addAll(heuristicFunctionTitledPane,
                                    beamFinderSettingsPane,
                                    frequencyTitledPane, 
                                    diagonalWeightTitledPane,
                                    bfsFinderSettingsPane);
        
        accordion.setExpandedPane(bfsFinderSettingsPane);
        
        mainVBox.getChildren().add(accordion);
        
        getChildren().add(mainVBox);
        
        VBox buttonVBox = new VBox();
        
        Button startPauseButton = new Button("Start");
        Button clearWallsButton = new Button("Clear walls");
        
        startPauseButton.setPrefWidth(PIXELS_WIDTH);
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
            
            pathfindingSettings.setFrequency(
                    Integer.parseInt(
                            frequencyComboBox.getValue().split(" ")[0]));
                    
            pathfindingSettings.setBeamWidth(
                    Integer.parseInt(beamWidthComboBox.getValue()));
            
             // Search finder should sleep on neighbours:
            pathfindingSettings.setDontSleep(false);
            pathfindingSettings.setHeuristicFunction(
                    HEURISTIC_MAP.get(heuristicComboBox.getValue()));
            
            if (searchState.getCurrentState().equals(CurrentState.IDLE)) {
                // Once here, start search:
                gridView.clearPath(path); // Clear the possible previous path!
                searchState.setCurrentState(CurrentState.SEARCHING);
                gridController.disableUserInteraction();
                gridModel.clearStateCells();
                startPauseButton.setText("Pause");
                
                finder = null;
                
                if (pathfindingSettings.isBidirectional()) {
                    finder = new BidirectionalBFSFinder();
                } else {
                    finder = new BFSFinder();
                    finder = new BeamSearchFinder();
                    finder = new BestFirstSearchFinder();
                }
                
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
                    try {
                        this.path.clear();
                        this.path.addAll(task.get());
                        System.out.println(this.path);
                    } catch (InterruptedException | ExecutionException ex) {
                        System.getLogger(
                                SettingsPane.class.getName()).log(
                                        System.Logger.Level.ERROR,
                                        (String) null,
                                        ex);
                        Platform.exit();
                    }
                    
                    gridView.drawPath(this.path);
                    gridController.enableUserInteraction();
                    searchState.setCurrentState(CurrentState.IDLE);
                    startPauseButton.setText("Search");
                    searchState.resetState();
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
        
        clearWallsButton.setOnAction(event -> {
           searchState.requestHalt();
           gridModel.clearWalls();
        });    
        
        buttonVBox.getChildren().addAll(startPauseButton,
                                        clearWallsButton);
        
        mainVBox.getChildren().add(buttonVBox);
        
        setOnMousePressed(event -> {
            offset[0] = event.getSceneX() - getLayoutX();
            offset[1] = event.getSceneY() - getLayoutY();
        });
        
        setOnMouseDragged(event -> {
            setLayoutX(event.getSceneX() - offset[0]);
            setLayoutY(event.getSceneY() - offset[1]);
        });
        
        Label emptyLabel = new Label("");
        emptyLabel.setPrefSize(PIXELS_WIDTH, 40.0);
                                                                                    mainVBox.getChildren().add(emptyLabel);

        // after: VBox mainVBox = new VBox();
        mainVBox.setFillWidth(true);
        mainVBox.prefWidthProperty().bind(widthProperty());
        mainVBox.prefHeightProperty().bind(heightProperty()); // optional

        // Make children take full width of the VBox:
        accordion.setMaxWidth(Double.MAX_VALUE);
        frequencyTitledPane.setMaxWidth(Double.MAX_VALUE);
        diagonalWeightTitledPane.setMaxWidth(Double.MAX_VALUE);
        bfsFinderSettingsPane.setMaxWidth(Double.MAX_VALUE);
        startPauseButton.setMaxWidth(Double.MAX_VALUE);
        clearWallsButton.setMaxWidth(Double.MAX_VALUE);

        // If you keep Pane as the root, explicitly place the VBox at (0,0):
        mainVBox.relocate(0, 0);
    }
    
    public SearchState getSearchState() {
        return searchState;
    }
    
    @Override
    public boolean isResizable() {
        return false;
    }
}
    