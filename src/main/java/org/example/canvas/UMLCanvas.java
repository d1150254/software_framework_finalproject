package org.example.canvas;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyEvent;
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

        this.setFocusTraversable(true);
        canvas.setFocusTraversable(true);

        canvas.setOnMousePressed(e -> { 
            this.requestFocus();
            if(currentState != null) currentState.onMousePress(e); 
            repaint(); 
        });
        canvas.setOnMouseDragged(e -> { if(currentState != null) currentState.onMouseDrag(e); repaint(); });
        canvas.setOnMouseReleased(e -> { if(currentState != null) currentState.onMouseRelease(e); repaint(); });
        canvas.setOnMouseMoved(e -> { if(currentState != null) currentState.onMouseMove(e); repaint(); });

        this.setOnKeyPressed(e -> {
            if(currentState != null) currentState.onKeyPress(e);
            repaint();
        });
    }

    public void removeObject(BasicObject obj) {
        if (obj == null) return;
        
        List<RelationshipLine> linesToRemove = new ArrayList<>();
        for (RelationshipLine line : lines) {
            if (line.getStartPort().getParent() == obj || line.getEndPort().getParent() == obj) {
                linesToRemove.add(line);
            }
        }
        lines.removeAll(linesToRemove);
        objects.remove(obj);
        notifySelectionChanged(null);
        repaint();
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

    public void ensureCapacity(double minWidth, double minHeight) {
        boolean changed = false;
        if (minWidth > canvas.getWidth()) {
            canvas.setWidth(minWidth + 200);
            changed = true;
        }
        if (minHeight > canvas.getHeight()) {
            canvas.setHeight(minHeight + 200);
            changed = true;
        }
        if (changed) {
            setPrefSize(canvas.getWidth(), canvas.getHeight());
        }
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
        
        if (currentState != null) {
            currentState.drawPreview(gc);
        }
    }
}
