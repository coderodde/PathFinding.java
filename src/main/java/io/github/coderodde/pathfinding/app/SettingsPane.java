package io.github.coderodde.pathfinding.app;

import static io.github.coderodde.pathfinding.app.Configuration.FREQUENCIES;
import static io.github.coderodde.pathfinding.finders.Finder.computePathCost;
import io.github.coderodde.pathfinding.controller.GridController;
import io.github.coderodde.pathfinding.finders.AStarFinder;
import io.github.coderodde.pathfinding.finders.BFSFinder;
import io.github.coderodde.pathfinding.finders.BIDDFSFinder;
import io.github.coderodde.pathfinding.finders.BeamSearchFinder;
import io.github.coderodde.pathfinding.finders.BestFirstSearchFinder;
import io.github.coderodde.pathfinding.finders.BidirectionalBFSFinder;
import io.github.coderodde.pathfinding.finders.BidirectionalDijkstra;
import io.github.coderodde.pathfinding.finders.DijkstraFinder;
import io.github.coderodde.pathfinding.finders.Finder;
import io.github.coderodde.pathfinding.heuristics.ChebyshevHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.EuclideanHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.HeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.ManhattanHeuristicFunction;
import io.github.coderodde.pathfinding.heuristics.OctileHeuristicFunction;
import io.github.coderodde.pathfinding.logic.GridCellNeighbourIterable;
import io.github.coderodde.pathfinding.logic.GridNodeExpander;
import io.github.coderodde.pathfinding.logic.PathfindingSettings;
import io.github.coderodde.pathfinding.logic.PathfindingSettings.DiagonalWeight;
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
    
    private static final String ASTAR             = "A* search";
    private static final String DIJKSTRA          = "Dijkstra";
    private static final String BI_DIJKSTRA       = "Bidirectional Dijkstra";
    private static final String BFS               = "BFS";
    private static final String BI_BFS            = "Bidirectional BFS";
    private static final String BEST_FIRST_SEARCH = "Best First search";
    private static final String BEAM_SEARCH       = "Beam search";
    private static final String BIDDFS            = "Bidirectional IDDFS";
    
    private static final String[] HEURISTIC_NAMES = {
        MANHATTAN,
        EUCLIDEAN,
        OCTILE,
        CHEBYSHEV,
    };
    
    private static final String[] FINDER_NAMES = {
        ASTAR,
        DIJKSTRA,
        BI_DIJKSTRA,
        BFS,
        BI_BFS,
        BEST_FIRST_SEARCH,
        BEAM_SEARCH,
        BIDDFS,
    };
    
    private static final Map<String, HeuristicFunction> HEURISTIC_MAP =
            new HashMap<>();
    
    private static final Map<String, Finder> FINDER_MAP = 
            new HashMap<>();
    
    static {
        HEURISTIC_MAP.put(EUCLIDEAN, new EuclideanHeuristicFunction());
        HEURISTIC_MAP.put(MANHATTAN, new ManhattanHeuristicFunction());
        HEURISTIC_MAP.put(OCTILE,    new OctileHeuristicFunction());
        HEURISTIC_MAP.put(CHEBYSHEV, new ChebyshevHeuristicFunction());
        
        FINDER_MAP.put(ASTAR,             new AStarFinder());
        FINDER_MAP.put(DIJKSTRA,          new DijkstraFinder());
        FINDER_MAP.put(BI_DIJKSTRA,       new BidirectionalDijkstra());
        FINDER_MAP.put(BFS,               new BFSFinder());
        FINDER_MAP.put(BI_BFS,            new BidirectionalBFSFinder());
        FINDER_MAP.put(BEST_FIRST_SEARCH, new BestFirstSearchFinder());
        FINDER_MAP.put(BEAM_SEARCH,       new BeamSearchFinder());  
        FINDER_MAP.put(BIDDFS,            new BIDDFSFinder());
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
    
    private final ComboBox<String> comboBoxFrequency        = new ComboBox<>();
    private final ComboBox<String> comboBoxDiagonalWeight   = new ComboBox<>();
    private final ComboBox<String> comboBoxFinder           = new ComboBox<>();
    private final ComboBox<String> comboBoxHeuristic        = new ComboBox<>();
    private final ComboBox<String> comboBoxBeamWidth        = new ComboBox<>();
    
    private final CheckBox checkBoxAllowDiagonals = 
              new CheckBox("Allow diagonals");
    
    private final CheckBox checkBoxDontCrossCorners = 
              new CheckBox("Don't cross corners");
    
    private final TitledPane titledPaneFrequency = 
            new TitledPane("Frequency", comboBoxFrequency);
    
    private final TitledPane titledPaneDiagonalWeight = 
            new TitledPane("Diagonal weight", comboBoxDiagonalWeight);
    
    private final TitledPane titledPaneFinder = 
            new TitledPane("Finder", comboBoxFinder);
    
    private final TitledPane titledPaneHeuristic = 
            new TitledPane("Heuristic", comboBoxHeuristic);
    
    private final TitledPane titledPaneBeamWidth = 
            new TitledPane("Beam width", comboBoxBeamWidth);
    
    private final Label resultLabel = new Label();
    
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
        this.resultLabel.setText("Path cost: N/A");
        this.resultLabel.setStyle("-fx-background-color: white;" +                          
                                  "-fx-font-size: 13px;");
        this.resultLabel.setPrefWidth(PIXELS_WIDTH);
        
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
        
        for (int beamWidth = 1; beamWidth <= 8; ++beamWidth) {
            comboBoxBeamWidth.getItems()
                             .add(String.format("%d", beamWidth));
        }
        
        comboBoxBeamWidth.setValue("8");
        
        for (String heuristicName : HEURISTIC_NAMES) {
            comboBoxHeuristic.getItems().add(heuristicName);
        }
        
        comboBoxFrequency.setPrefWidth(PIXELS_WIDTH);
        
        for (Integer frequency : FREQUENCIES) {
            comboBoxFrequency.getItems()
                             .add(String.format("%d Hz", frequency));
        }
        
        for (String finder : FINDER_NAMES) {
            comboBoxFinder.getItems().add(finder);
        }
        
        comboBoxFinder.setValue(FINDER_NAMES[0]);
        comboBoxFinder.setPrefWidth(PIXELS_WIDTH);
        
        comboBoxFrequency.setValue(
                String.format("%d Hz", FREQUENCIES.getLast()));
        
        comboBoxDiagonalWeight  .getItems().add("1");
        comboBoxDiagonalWeight  .getItems().add("SQRT2");
        comboBoxDiagonalWeight  .setPrefWidth(PIXELS_WIDTH);
        comboBoxDiagonalWeight  .setValue("SQRT2");
        
        VBox bfsVBox = new VBox();
        
        bfsVBox.getChildren().addAll(checkBoxAllowDiagonals,
                                     checkBoxDontCrossCorners);
        
        checkBoxAllowDiagonals.setSelected(true);
        checkBoxDontCrossCorners.setSelected(true);
        
        mainVBox.setPrefWidth(bfsVBox.getWidth());
        
        comboBoxBeamWidth.setValue("3");
        comboBoxHeuristic.setValue(MANHATTAN);
        
        comboBoxBeamWidth.setPrefWidth(PIXELS_WIDTH);
        comboBoxHeuristic.setPrefWidth(PIXELS_WIDTH);
        
        Accordion accordion = new Accordion();  
        accordion.setPrefWidth(PIXELS_WIDTH);
        accordion.getPanes().addAll(titledPaneFrequency,
                                    titledPaneDiagonalWeight,
                                    titledPaneFinder,
                                    titledPaneHeuristic,
                                    titledPaneBeamWidth);
        
        accordion.setExpandedPane(titledPaneFinder);
        
        mainVBox.getChildren().addAll(accordion, resultLabel);
        
        getChildren().add(mainVBox);
        
        VBox buttonVBox = new VBox();
        
        Button startPauseButton = new Button("Start");
        Button clearWallsButton = new Button("Clear walls");
        
        startPauseButton.setPrefWidth(PIXELS_WIDTH);
        clearWallsButton.setPrefWidth(PIXELS_WIDTH);
        
        startPauseButton.setOnAction(event -> {
            
            PathfindingSettings pathfindingSettings = 
                    computePathfindingSettings();
                    
            if (searchState.getCurrentState().equals(CurrentState.IDLE)) {
                // Once here, start search:
                gridView.clearPath(path); // Clear the possible previous path!
                searchState.setCurrentState(CurrentState.SEARCHING);
                gridController.disableUserInteraction();
                gridModel.clearStateCells();
                startPauseButton.setText("Pause");
                
                finder = pathfindingSettings.getFinder();
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
                    resultLabel.setText(
                            "Path cost: " + 
                                    computePathCost(path, pathfindingSettings));
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
    
    private PathfindingSettings computePathfindingSettings() {
        PathfindingSettings ps = new PathfindingSettings();
        
        ps.setAllowDiagonals(checkBoxAllowDiagonals.isSelected());
        ps.setDontCrossCorners(checkBoxDontCrossCorners.isSelected());
        ps.setBeamWidth(Integer.parseInt(comboBoxBeamWidth.getValue()));
        ps.setDiagonalWeight(
                DiagonalWeight.convert(comboBoxDiagonalWeight.getValue()));
        
        ps.setHeuristicFunction(
                HEURISTIC_MAP.get(comboBoxHeuristic.getValue()));
        
        ps.setFinder(FINDER_MAP.get(comboBoxFinder.getValue()));
        
        return ps;
    }
}
    