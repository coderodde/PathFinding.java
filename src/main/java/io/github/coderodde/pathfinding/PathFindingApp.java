package io.github.coderodde.pathfinding;

import static io.github.coderodde.pathfinding.Configuration.DEFAULT_CELL_WIDTH_HEIGHT;
import io.github.coderodde.pathfinding.controller.GridController;
import io.github.coderodde.pathfinding.model.GridModel;
import io.github.coderodde.pathfinding.utils.Cell;
import io.github.coderodde.pathfinding.utils.CellType;
import io.github.coderodde.pathfinding.utils.GridBounds;
import io.github.coderodde.pathfinding.view.GridView;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 * This class implements a JavaFX program showcasing the pathfinding in grid-
 * based games.
 * 
 * @author Rodion "rodde" Efremov
 * @version 1.0.0 (Aug 24, 2025)
 * @since 1.0.0 (Aug 24, 2025)
 */
public final class PathFindingApp extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        GridView view = new GridView();
        view.setCellWidthHeight(DEFAULT_CELL_WIDTH_HEIGHT);
        view.initializeState(); // Mandatory!
        
        GridBounds bounds = 
                new GridBounds(
                        Screen.getPrimary().getBounds(),
                        DEFAULT_CELL_WIDTH_HEIGHT);
        
        GridModel model = new GridModel(bounds.horizontalCells, 
                                        bounds.verticalCells);
        
        model.setGridView(view);    
        
        GridController controller = new GridController();

        controller.setGridModel(model);
        controller.setGridView(view);
        controller.setEventHandlers();
        
        Cell cell = model.getCell(0, 0);
        cell.setCellType(CellType.WALL);
        
        cell = model.getCell(1, 1);
        cell.setCellType(CellType.PATH);
        
        cell = model.getCell(2, 2);
        cell.setCellType(CellType.OPENED);
        
        cell = model.getCell(3, 3);
        cell.setCellType(CellType.VISITED);
        
        cell = model.getCell(4, 4);
        cell.setCellType(CellType.TRACED);
        
        view.setGridModel(model);
        view.setCellWidthHeight(DEFAULT_CELL_WIDTH_HEIGHT);
        
        view.drawBorders();
        view.drawAllCels();

        // Add canvas to a layout (StackPane preserves fixed size)
        StackPane root = new StackPane(view);

        // Create the scene
        Scene scene = new Scene(root,
                                view.getWidth(),
                                view.getHeight());

        stage.initStyle(StageStyle.UNDECORATED);
        stage.setMaximized(true);
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}
