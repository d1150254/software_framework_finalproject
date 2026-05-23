package org.example.state;

import javafx.scene.Cursor;
import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.BasicObject;
import org.example.core.ResizeZone;

public class SelectMode implements ToolState {
    private UMLCanvas canvas;
    private BasicObject selectedObject;
    private double lastX, lastY;
    private boolean isResizing = false;
    private ResizeZone activeZone = ResizeZone.NONE;

    public SelectMode(UMLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onMousePress(MouseEvent e) {
        lastX = e.getX();
        lastY = e.getY();
        
        if (selectedObject != null && activeZone != ResizeZone.NONE && activeZone != null) {
            isResizing = true;
            return;
        }

        selectedObject = null;
        canvas.clearSelection();
        isResizing = false;

        // Traverse backwards to pick front-most object
        for (int i = canvas.getObjects().size() - 1; i >= 0; i--) {
            BasicObject obj = canvas.getObjects().get(i);
            if (obj.contains(e.getX(), e.getY())) {
                selectedObject = obj;
                obj.setSelected(true);
                
                if (e.getClickCount() == 2) {
                    org.example.ui.ObjectEditorDialog.showEditDialog(obj, () -> canvas.repaint());
                }
                
                break;
            }
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
