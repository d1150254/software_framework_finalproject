package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.example.canvas.UMLCanvas;
import org.example.core.lines.LineType;
import org.example.state.CreateClassMode;
import org.example.state.CreateConnectionMode;
import org.example.state.CreateInterfaceMode;
import org.example.state.SelectMode;
import org.example.ui.ObjectTreePanel;

public class Main extends Application {

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        
        // Setup Canvas
        UMLCanvas canvas = new UMLCanvas(800, 600);
        
        // Setup Toolbar
        VBox toolbar = new VBox(10);
        toolbar.setPadding(new Insets(10));
        toolbar.setStyle("-fx-background-color: #f0f0f0; -fx-border-color: #cccccc; -fx-border-width: 0 1 0 0;");
        
        ToggleGroup group = new ToggleGroup();
        
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
        btnAssoc.setToggleGroup(group); // We might not have association in LineType, but let's use what we have
        btnInherit.setToggleGroup(group);
        btnImpl.setToggleGroup(group);
        btnAggreg.setToggleGroup(group);
        btnCompos.setToggleGroup(group);
        
        // Note: I only have 4 LineTypes: INHERITANCE, IMPLEMENTATION, AGGREGATION, COMPOSITION. 
        // We'll map accordingly. We can reuse INHERITANCE for association if needed but let's just stick to the 4.
        
        toolbar.getChildren().addAll(
            btnSelect, btnClass, btnInterface, 
            btnInherit, btnImpl, btnAggreg, btnCompos
        );
        
        btnSelect.setOnAction(e -> canvas.setState(new SelectMode(canvas)));
        btnClass.setOnAction(e -> canvas.setState(new CreateClassMode(canvas)));
        btnInterface.setOnAction(e -> canvas.setState(new CreateInterfaceMode(canvas)));
        btnInherit.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.INHERITANCE)));
        btnImpl.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.IMPLEMENTATION)));
        btnAggreg.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.AGGREGATION)));
        btnCompos.setOnAction(e -> canvas.setState(new CreateConnectionMode(canvas, LineType.COMPOSITION)));
        
        // Default state
        btnSelect.setSelected(true);
        canvas.setState(new SelectMode(canvas));
        
        // Ensure buttons have same width
        toolbar.getChildren().forEach(node -> {
            if (node instanceof ToggleButton) {
                ((ToggleButton) node).setMaxWidth(Double.MAX_VALUE);
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
}
