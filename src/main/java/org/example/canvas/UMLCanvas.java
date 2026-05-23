package org.example.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import org.example.core.BasicObject;
import org.example.core.lines.RelationshipLine;
import org.example.state.ToolState;

import java.util.ArrayList;
import java.util.List;

public class UMLCanvas extends Pane {
    private final Canvas canvas;
    private final List<BasicObject> objects;
    private final List<RelationshipLine> lines;
    
    private ToolState currentState;
    
    private Runnable onActionCompleted;

    public interface SelectionListener {
        void onSelectionChanged(BasicObject selectedObject);
    }
    private SelectionListener selectionListener;

    public UMLCanvas(double width, double height) {
        canvas = new Canvas(width, height);
        getChildren().add(canvas);
        
        objects = new ArrayList<>();
        lines = new ArrayList<>();

        canvas.setOnMousePressed(e -> { if(currentState != null) currentState.onMousePress(e); repaint(); });
        canvas.setOnMouseDragged(e -> { if(currentState != null) currentState.onMouseDrag(e); repaint(); });
        canvas.setOnMouseReleased(e -> { if(currentState != null) currentState.onMouseRelease(e); repaint(); });
        canvas.setOnMouseMoved(e -> { if(currentState != null) currentState.onMouseMove(e); repaint(); });
    }

    public void setState(ToolState state) {
        this.currentState = state;
    }

    public void setOnActionCompleted(Runnable onActionCompleted) {
        this.onActionCompleted = onActionCompleted;
    }

    public void triggerActionCompleted() {
        if (onActionCompleted != null) {
            onActionCompleted.run();
        }
    }

    public void setSelectionListener(SelectionListener listener) {
        this.selectionListener = listener;
    }

    public void notifySelectionChanged(BasicObject obj) {
        if (selectionListener != null) {
            selectionListener.onSelectionChanged(obj);
        }
    }

    public void setCursorStyle(Cursor cursor) {
        canvas.setCursor(cursor);
    }

    public void addObject(BasicObject obj) {
        objects.add(obj);
        repaint();
    }

    public void addLine(RelationshipLine line) {
        lines.add(line);
        repaint();
    }

    public List<BasicObject> getObjects() {
        return objects;
    }

    public List<RelationshipLine> getLines() {
        return lines;
    }

    public void clearSelection() {
        for (BasicObject obj : objects) {
            obj.setSelected(false);
        }
    }

    public void repaint() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        for (RelationshipLine line : lines) {
            line.draw(gc);
        }
        for (BasicObject obj : objects) {
            obj.draw(gc);
        }
    }
}
