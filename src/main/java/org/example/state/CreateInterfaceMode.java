package org.example.state;

import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.UMLInterface;

public class CreateInterfaceMode implements ToolState {
    private UMLCanvas canvas;
    private double currentMouseX = -1;
    private double currentMouseY = -1;
    private UMLInterface previewInterface;

    public CreateInterfaceMode(UMLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onMousePress(MouseEvent e) {
        UMLInterface ui = new UMLInterface(e.getX(), e.getY());
        canvas.addObject(ui);
        canvas.repaint();
        canvas.triggerActionCompleted();
    }

    @Override
    public void onMouseDrag(MouseEvent e) { }
    @Override
    public void onMouseRelease(MouseEvent e) { }
    @Override
    public void onMouseMove(MouseEvent e) {
        currentMouseX = e.getX();
        currentMouseY = e.getY();
        if (previewInterface == null) {
            previewInterface = new UMLInterface(currentMouseX, currentMouseY);
        } else {
            previewInterface.setX(currentMouseX);
            previewInterface.setY(currentMouseY);
        }
    }

    @Override
    public void drawPreview(javafx.scene.canvas.GraphicsContext gc) {
        if (previewInterface != null && currentMouseX >= 0 && currentMouseY >= 0) {
            gc.save();
            gc.setGlobalAlpha(0.5);
            previewInterface.draw(gc);
            gc.restore();
        }
    }
}
