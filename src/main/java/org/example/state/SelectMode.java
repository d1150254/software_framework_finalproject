package org.example.state;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import org.example.canvas.UMLCanvas;
import org.example.core.BasicObject;
import org.example.core.ResizeZone;

public class SelectMode implements ToolState {
    private final UMLCanvas canvas;
    private BasicObject selectedObject;
    private double lastX, lastY;
    private boolean isResizing = false;
    private ResizeZone activeZone = ResizeZone.NONE;
    private ContextMenu contextMenu;

    public SelectMode(UMLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onMousePress(MouseEvent e) {
        if (contextMenu != null && contextMenu.isShowing()) {
            contextMenu.hide();
        }

        lastX = e.getX();
        lastY = e.getY();
        
        if (selectedObject != null && activeZone != ResizeZone.NONE && activeZone != null) {
            isResizing = true;
            return;
        }

        selectedObject = null;
        canvas.clearSelection();
        isResizing = false;
        canvas.notifySelectionChanged(null); // Notify null if clicking empty space

        // Traverse backwards to pick front-most object
        for (int i = canvas.getObjects().size() - 1; i >= 0; i--) {
            BasicObject obj = canvas.getObjects().get(i);
            if (obj.contains(e.getX(), e.getY())) {
                selectedObject = obj;
                obj.setSelected(true);
                canvas.notifySelectionChanged(obj); // Notify the selected object
                break;
            }
        }
        
        if (e.getButton() == MouseButton.SECONDARY && selectedObject != null) {
            contextMenu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("Delete");
            deleteItem.setOnAction(event -> {
                canvas.removeObject(selectedObject);
                selectedObject = null;
            });
            contextMenu.getItems().add(deleteItem);
            
            contextMenu.show(canvas, e.getScreenX(), e.getScreenY());
        }
    }

    @Override
    public void onMouseDrag(MouseEvent e) {
        double dx = e.getX() - lastX;
        double dy = e.getY() - lastY;

        if (selectedObject != null) {
            if (isResizing) {
                selectedObject.resize(activeZone, dx, dy);
            } else {
                selectedObject.setX(selectedObject.getX() + dx);
                selectedObject.setY(selectedObject.getY() + dy);
            }
            canvas.ensureCapacity(
                selectedObject.getX() + selectedObject.getWidth(),
                selectedObject.getY() + selectedObject.getHeight()
            );
        }

        lastX = e.getX();
        lastY = e.getY();
    }

    @Override
    public void onMouseRelease(MouseEvent e) {
        isResizing = false;
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        if (selectedObject != null && selectedObject.isSelected()) {
            activeZone = selectedObject.getResizeZone(e.getX(), e.getY());
            setCursorByZone(activeZone);
        } else {
            boolean overAny = false;
            for (BasicObject obj : canvas.getObjects()) {
                if (obj.contains(e.getX(), e.getY())) {
                    overAny = true; break;
                }
            }
            if (overAny) canvas.setCursorStyle(Cursor.HAND);
            else canvas.setCursorStyle(Cursor.DEFAULT);
            activeZone = ResizeZone.NONE;
        }
    }

    @Override
    public void onKeyPress(KeyEvent e) {
        if (e.getCode() == KeyCode.DELETE) {
            if (selectedObject != null) {
                canvas.removeObject(selectedObject);
                selectedObject = null;
                
                if (contextMenu != null && contextMenu.isShowing()) {
                    contextMenu.hide();
                }
            }
        }
    }

    private void setCursorByZone(ResizeZone zone) {
        if (zone == null) {
            canvas.setCursorStyle(Cursor.DEFAULT);
            return;
        }
        switch (zone) {
            case NW: canvas.setCursorStyle(Cursor.NW_RESIZE); break;
            case NE: canvas.setCursorStyle(Cursor.NE_RESIZE); break;
            case SW: canvas.setCursorStyle(Cursor.SW_RESIZE); break;
            case SE: canvas.setCursorStyle(Cursor.SE_RESIZE); break;
            case N: canvas.setCursorStyle(Cursor.N_RESIZE); break;
            case S: canvas.setCursorStyle(Cursor.S_RESIZE); break;
            case W: canvas.setCursorStyle(Cursor.W_RESIZE); break;
            case E: canvas.setCursorStyle(Cursor.E_RESIZE); break;
            case NONE: default: canvas.setCursorStyle(Cursor.HAND); break;
        }
    }
}
