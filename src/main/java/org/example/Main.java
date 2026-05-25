package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.canvas.UMLCanvas;
import org.example.core.lines.LineType;
import org.example.persistence.UMLProjectStore;
import org.example.state.CreateClassMode;
import org.example.state.CreateConnectionMode;
import org.example.state.CreateInterfaceMode;
import org.example.state.SelectMode;
import org.example.ui.ObjectTreePanel;

import java.util.function.Consumer;

public class Main extends Application {

    private static final int CANVAS_WIDTH = 800;
    private static final int CANVAS_HEIGHT = 600;
    private static final int WINDOW_WIDTH = 1100;
    private static final int WINDOW_HEIGHT = 600;
    private static final int TOOLBAR_SPACING = 10;
    private static final String TOOLBAR_STYLE = "-fx-background-color: #f0f0f0; "
            + "-fx-border-color: #cccccc; "
            + "-fx-border-width: 0 1 0 0;";
    private static final String WINDOW_TITLE = "UML Editor - AI Assistant";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        UMLCanvas canvas = new UMLCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        UMLProjectStore projectStore = new UMLProjectStore();
        ProjectManager projectManager = new ProjectManager(projectStore);
        VBox toolbar = createToolbar(primaryStage, canvas, projectManager);
        ObjectTreePanel treePanel = createTreePanel(canvas);

        root.setLeft(toolbar);
        root.setCenter(canvas);
        root.setRight(treePanel);

        configureStage(primaryStage, root);
    }

    private VBox createToolbar(Stage primaryStage, UMLCanvas canvas, ProjectManager projectManager) {
        VBox toolbar = new VBox(TOOLBAR_SPACING);
        toolbar.setPadding(new Insets(TOOLBAR_SPACING));
        toolbar.setStyle(TOOLBAR_STYLE);

        ToggleGroup group = new ToggleGroup();

        Button btnOpen = new Button("Open");
        Button btnSave = new Button("Save");
        ToggleButton btnSelect = createToolButton("Select", group,
                button -> switchToSelectMode(canvas, button));
        ToggleButton btnClass = createToolButton("Class", group,
                button -> canvas.setState(new CreateClassMode(canvas)));
        ToggleButton btnInterface = createToolButton("Interface", group,
                button -> canvas.setState(new CreateInterfaceMode(canvas)));
        ToggleButton btnAssoc = createConnectionButton("Association", group, canvas, LineType.ASSOCIATION);
        ToggleButton btnInherit = createConnectionButton("Inheritance", group, canvas, LineType.INHERITANCE);
        ToggleButton btnImpl = createConnectionButton("Implementation", group, canvas, LineType.IMPLEMENTATION);
        ToggleButton btnAggreg = createConnectionButton("Aggregation", group, canvas, LineType.AGGREGATION);
        ToggleButton btnCompos = createConnectionButton("Composition", group, canvas, LineType.COMPOSITION);

        toolbar.getChildren().addAll(
            btnOpen, btnSave,
            btnSelect, btnClass, btnInterface,
            btnAssoc, btnInherit, btnImpl, btnAggreg, btnCompos
        );

        btnOpen.setOnAction(e -> projectManager.openProject(primaryStage, canvas));
        btnSave.setOnAction(e -> projectManager.saveProject(primaryStage, canvas));

        switchToSelectMode(canvas, btnSelect);
        canvas.setOnActionCompleted(() -> switchToSelectMode(canvas, btnSelect));

        toolbar.getChildren().forEach(node -> {
            if (node instanceof ButtonBase) {
                ((ButtonBase) node).setMaxWidth(Double.MAX_VALUE);
            }
        });

        return toolbar;
    }

    private ToggleButton createConnectionButton(String label, ToggleGroup group, UMLCanvas canvas, LineType lineType) {
        return createToolButton(label, group,
                button -> canvas.setState(new CreateConnectionMode(canvas, lineType)));
    }

    private ToggleButton createToolButton(String label, ToggleGroup group, Consumer<ToggleButton> action) {
        ToggleButton button = new ToggleButton(label);
        button.setToggleGroup(group);
        button.setOnAction(e -> action.accept(button));
        return button;
    }

    private ObjectTreePanel createTreePanel(UMLCanvas canvas) {
        ObjectTreePanel treePanel = new ObjectTreePanel(canvas);
        canvas.setSelectionListener(obj -> treePanel.bindObject(obj));
        return treePanel;
    }

    private void switchToSelectMode(UMLCanvas canvas, ToggleButton selectButton) {
        selectButton.setSelected(true);
        canvas.setState(new SelectMode(canvas));
    }

    private void configureStage(Stage primaryStage, BorderPane root) {
        Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
        primaryStage.setTitle(WINDOW_TITLE);
        primaryStage.setScene(scene);
        primaryStage.show();
    }
}
