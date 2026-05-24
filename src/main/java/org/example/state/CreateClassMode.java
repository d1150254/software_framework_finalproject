package org.example.state;

import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.UMLClass;

public class CreateClassMode implements ToolState {
    private UMLCanvas canvas;
    private double currentMouseX = -1;
    private double currentMouseY = -1;
    private UMLClass previewClass;

    public CreateClassMode(UMLCanvas canvas) {
        this.canvas = canvas;
    }

    @Override
    public void onMousePress(MouseEvent e) {
        UMLClass c = new UMLClass(e.getX(), e.getY());
        canvas.addObject(c);
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
        if (previewClass == null) {
            previewClass = new UMLClass(currentMouseX, currentMouseY);
        } else {
            previewClass.setX(currentMouseX);
            previewClass.setY(currentMouseY);
        }
    }

    @Override
    public void drawPreview(javafx.scene.canvas.GraphicsContext gc) {
        if (previewClass != null && currentMouseX >= 0 && currentMouseY >= 0) {
            gc.save();
            gc.setGlobalAlpha(0.5);
            previewClass.draw(gc);
            gc.restore();
        }
    }
}
