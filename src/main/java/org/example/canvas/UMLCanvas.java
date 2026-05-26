package org.example.canvas;

import javafx.geometry.BoundingBox;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.SnapshotParameters;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.Pane;
import javafx.scene.Cursor;
import org.example.core.BasicObject;
import org.example.core.lines.RelationshipLine;
import org.example.state.ToolState;

import java.util.ArrayList;
import java.util.List;

public class UMLCanvas extends Pane {
    private static final double DEFAULT_CANVAS_WIDTH = 800;
    private static final double DEFAULT_CANVAS_HEIGHT = 600;
    private static final double EXPORT_PADDING = 20.0;
    
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
        ensureContentVisible();
        repaint();
    }

    public void addLine(RelationshipLine line) {
        lines.add(line);
        ensureContentVisible();
        repaint();
    }

    public List<BasicObject> getObjects() {
        return objects;
    }

    public List<RelationshipLine> getLines() {
        return lines;
    }

    public void replaceDiagram(List<BasicObject> newObjects, List<RelationshipLine> newLines) {
        objects.clear();
        lines.clear();
        objects.addAll(newObjects);
        lines.addAll(newLines);
        notifySelectionChanged(null);
        ensureContentVisible();
        repaint();
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

    public void ensureContentVisible() {
        BoundingBox bounds = getContentBounds();
        ensureCapacity(bounds.getMaxX(), bounds.getMaxY());
    }

    public WritableImage createContentSnapshot() {
        ensureContentVisible();
        repaint();

        BoundingBox bounds = getContentBounds();
        int width = Math.max(1, (int) Math.ceil(bounds.getWidth()));
        int height = Math.max(1, (int) Math.ceil(bounds.getHeight()));

        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new Rectangle2D(bounds.getMinX(), bounds.getMinY(), width, height));

        return canvas.snapshot(params, new WritableImage(width, height));
    }

    public BoundingBox getContentBounds() {
        if (objects.isEmpty() && lines.isEmpty()) {
            return new BoundingBox(0, 0, DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (BasicObject obj : objects) {
            minX = Math.min(minX, obj.getX());
            minY = Math.min(minY, obj.getY());
            maxX = Math.max(maxX, obj.getX() + obj.getWidth());
            maxY = Math.max(maxY, obj.getY() + obj.getHeight());
        }

        for (RelationshipLine line : lines) {
            BoundingBox lineBounds = line.getVisualBounds();
            minX = Math.min(minX, lineBounds.getMinX());
            minY = Math.min(minY, lineBounds.getMinY());
            maxX = Math.max(maxX, lineBounds.getMaxX());
            maxY = Math.max(maxY, lineBounds.getMaxY());
        }

        minX -= EXPORT_PADDING;
        minY -= EXPORT_PADDING;
        maxX += EXPORT_PADDING;
        maxY += EXPORT_PADDING;

        minX = Math.max(0, minX);
        minY = Math.max(0, minY);

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }

    public void clearSelection() {
        for (BasicObject obj : objects) {
            obj.setSelected(false);
        }
        for (RelationshipLine line : lines) {
            line.setSelected(false);
        }
    }

    public void removeLine(RelationshipLine line) {
        if (line == null) return;
        lines.remove(line);
        notifySelectionChanged(null);
        repaint();
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
