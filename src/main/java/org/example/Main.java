package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBase;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.ScrollPane;
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

    private static final String LABEL_OPEN = "Open";
    private static final String LABEL_SAVE = "Save";
    private static final String LABEL_EXPORT = "Export";
    private static final String LABEL_SELECT = "Select";
    private static final String LABEL_CLASS = "Class";
    private static final String LABEL_INTERFACE = "Interface";
    private static final String LABEL_ASSOCIATION = "Association";
    private static final String LABEL_INHERITANCE = "Inheritance";
    private static final String LABEL_IMPLEMENTATION = "Implementation";
    private static final String LABEL_AGGREGATION = "Aggregation";
    private static final String LABEL_COMPOSITION = "Composition";

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        UMLCanvas canvas = new UMLCanvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        UMLProjectStore projectStore = new UMLProjectStore();
        ProjectManager projectManager = new ProjectManager(projectStore);
        ExportManager exportManager = new ExportManager();
        VBox toolbar = createToolbar(primaryStage, canvas, projectManager, exportManager);
        ObjectTreePanel treePanel = createTreePanel(canvas);

        ScrollPane scrollPane = createScrollPane(canvas);

        root.setLeft(toolbar);
        root.setCenter(scrollPane);
        root.setRight(treePanel);

        configureStage(primaryStage, root);
    }

    private ScrollPane createScrollPane(UMLCanvas canvas) {
        ScrollPane scrollPane = new ScrollPane(canvas);
        scrollPane.setStyle("-fx-background-color: #555555; -fx-control-inner-background: #555555;");
        scrollPane.setPannable(false);
        canvas.setScrollPane(scrollPane);
        return scrollPane;
    }

    private VBox createToolbar(Stage primaryStage, UMLCanvas canvas, ProjectManager projectManager, ExportManager exportManager) {
        VBox toolbar = initializeToolbarContainer();
        ToggleGroup toolGroup = new ToggleGroup();

        addProjectManagementButtons(toolbar, primaryStage, canvas, projectManager, exportManager);
        ToggleButton selectButton = addShapeCreationButtons(toolbar, toolGroup, canvas);
        addConnectionCreationButtons(toolbar, toolGroup, canvas);

        setupDefaultSelectionBehavior(canvas, selectButton);
        applyFullWidthStyleToButtons(toolbar);

        return toolbar;
    }

    private VBox initializeToolbarContainer() {
        VBox toolbar = new VBox(TOOLBAR_SPACING);
        toolbar.setPadding(new Insets(TOOLBAR_SPACING));
        toolbar.setStyle(TOOLBAR_STYLE);
        return toolbar;
    }

    private void addProjectManagementButtons(VBox toolbar, Stage primaryStage, UMLCanvas canvas, ProjectManager projectManager, ExportManager exportManager) {
        Button openButton = new Button(LABEL_OPEN);
        Button saveButton = new Button(LABEL_SAVE);
        Button exportButton = new Button(LABEL_EXPORT);

        openButton.setOnAction(event -> projectManager.openProject(primaryStage, canvas));
        saveButton.setOnAction(event -> projectManager.saveProject(primaryStage, canvas));
        exportButton.setOnAction(event -> exportManager.exportCanvas(primaryStage, canvas));

        toolbar.getChildren().addAll(openButton, saveButton, exportButton);
    }

    private ToggleButton addShapeCreationButtons(VBox toolbar, ToggleGroup toolGroup, UMLCanvas canvas) {
        ToggleButton selectButton = createToolButton(LABEL_SELECT, toolGroup,
                button -> switchToSelectMode(canvas, button));
        ToggleButton classButton = createToolButton(LABEL_CLASS, toolGroup,
                button -> canvas.setState(new CreateClassMode(canvas)));
        ToggleButton interfaceButton = createToolButton(LABEL_INTERFACE, toolGroup,
                button -> canvas.setState(new CreateInterfaceMode(canvas)));

        toolbar.getChildren().addAll(selectButton, classButton, interfaceButton);
        return selectButton;
    }

    private void addConnectionCreationButtons(VBox toolbar, ToggleGroup toolGroup, UMLCanvas canvas) {
        ToggleButton associationButton = createConnectionButton(LABEL_ASSOCIATION, toolGroup, canvas, LineType.ASSOCIATION);
        ToggleButton inheritanceButton = createConnectionButton(LABEL_INHERITANCE, toolGroup, canvas, LineType.INHERITANCE);
        ToggleButton implementationButton = createConnectionButton(LABEL_IMPLEMENTATION, toolGroup, canvas, LineType.IMPLEMENTATION);
        ToggleButton aggregationButton = createConnectionButton(LABEL_AGGREGATION, toolGroup, canvas, LineType.AGGREGATION);
        ToggleButton compositionButton = createConnectionButton(LABEL_COMPOSITION, toolGroup, canvas, LineType.COMPOSITION);

        toolbar.getChildren().addAll(
                associationButton, inheritanceButton, implementationButton,
                aggregationButton, compositionButton
        );
    }

    private void setupDefaultSelectionBehavior(UMLCanvas canvas, ToggleButton selectButton) {
        switchToSelectMode(canvas, selectButton);
        canvas.setOnActionCompleted(() -> switchToSelectMode(canvas, selectButton));
    }

    private void applyFullWidthStyleToButtons(VBox toolbar) {
        toolbar.getChildren().forEach(node -> {
            if (node instanceof ButtonBase) {
                ((ButtonBase) node).setMaxWidth(Double.MAX_VALUE);
            }
        });
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
        canvas.setSelectionListener(treePanel::bindObject);
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
