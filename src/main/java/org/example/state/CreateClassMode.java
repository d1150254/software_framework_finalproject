package org.example.state;

import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.UMLClass;

public class CreateClassMode implements ToolState {
    private UMLCanvas canvas;

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
    public void onMouseMove(MouseEvent e) { }
}
