package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.canvas.UMLCanvas;
import org.example.core.lines.LineType;
import org.example.persistence.UMLProjectStore;
import org.example.state.CreateClassMode;
import org.example.state.CreateConnectionMode;
import org.example.state.CreateInterfaceMode;
import org.example.state.SelectMode;
import org.example.ui.ObjectTreePanel;

import java.io.File;
import java.io.IOException;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Setup Canvas
        UMLCanvas canvas = new UMLCanvas(800, 600);
        UMLProjectStore projectStore = new UMLProjectStore();
        
        // Setup Toolbar
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        
        ToggleGroup group = new ToggleGroup();
        
        Button btnOpen = new Button("Open");
        Button btnSave = new Button("Save");
        ToggleButton btnSelect = new ToggleButton("Select");
        ToggleButton btnClass = new ToggleButton("Class");
        ToggleButton btnInterface = new ToggleButton("Interface");
        ToggleButton btnAssoc = new ToggleButton("Association");
        ToggleButton btnInherit = new ToggleButton("Inheritance");
        ToggleButton btnImpl = new ToggleButton("Implementation");
        ToggleButton btnAggreg = new ToggleButton("Aggregation");
        ToggleButton btnCompos = new ToggleButton("Composition");
        
        btnSelect.setToggleGroup(group);
        btnClass.setToggleGroup(group);
        btnInterface.setToggleGroup(group);
        btnAssoc.setToggleGroup(group);
        btnInherit.setToggleGroup(group);
        btnImpl.setToggleGroup(group);
        btnAggreg.setToggleGroup(group);
        btnCompos.setToggleGroup(group);

        toolbar.getChildren().addAll(
            btnOpen, btnSave,
            btnSelect, btnClass, btnInterface, 
            btnAssoc, btnInherit, btnImpl, btnAggreg, btnCompos
        );
        
        btnOpen.setOnAction(e -> openProject(primaryStage, canvas, projectStore));
        btnSave.setOnAction(e -> saveProject(primaryStage, canvas, projectStore));
        btnSelect.setOnAction(e -> canvas.setState(new SelectMode(canvas)));
        btnClass.setOnAction(e -> canvas.setState(new CreateClassMode(canvas)));
        btnInterface.setOnAction(e -> canvas.setState(new CreateInterfaceMode(canvas)));
        btnAssoc.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.ASSOCIATION)));
        btnInherit.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.INHERITANCE)));
        btnImpl.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.IMPLEMENTATION)));
        btnAggreg.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.AGGREGATION)));
        btnCompos.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.COMPOSITION)));
        
        // Default state
        btnSelect.setSelected(true);
        canvas.setState(new SelectMode(canvas));
        
        // Listen to action completed and revert to select mode
        canvas.setOnActionCompleted(() -> {
            btnSelect.setSelected(true);
            canvas.setState(new SelectMode(canvas));
        });
        
        // Ensure buttons have same width
        toolbar.getChildren().forEach(node -> {
            if (node instanceof ButtonBase) {
                ((ButtonBase) node).setMaxWidth(Double.MAX_VALUE);
            }
        });

        // Setup Tree Panel
        ObjectTreePanel treePanel = new ObjectTreePanel(canvas);
        canvas.setSelectionListener(obj -> treePanel.bindObject(obj));

        root.setLeft(toolbar);
        root.setCenter(canvas);
        root.setRight(treePanel);
        
        Scene scene = new Scene(root, 1100, 600);
        primaryStage.setTitle("UML Editor - AI Assistant");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    private void openProject(Stage owner, UMLCanvas canvas, UMLProjectStore projectStore) {
        FileChooser fileChooser = createProjectFileChooser("Open UML Project");
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        try {
            projectStore.load(canvas, file.toPath());
        } catch (IOException | IllegalArgumentException ex) {
            showError("Open Failed", "Unable to open the selected UML project file.", ex);
        }
    }

    private void saveProject(Stage owner, UMLCanvas canvas, UMLProjectStore projectStore) {
        FileChooser fileChooser = createProjectFileChooser("Save UML Project");
        fileChooser.setInitialFileName("uml-project.json");
        File file = fileChooser.showSaveDialog(owner);
        if (file == null) {
            return;
        }

        try {
            projectStore.save(canvas, file.toPath());
        } catch (IOException | IllegalArgumentException ex) {
            showError("Save Failed", "Unable to save the UML project file.", ex);
        }
    }

    private FileChooser createProjectFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("UML Project JSON", "*.json")
        );
        return fileChooser;
    }

    private void showError(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}
