package org.example.state;

import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.UMLInterface;

public class CreateInterfaceMode implements ToolState {
    private UMLCanvas canvas;

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
    public void onMouseMove(MouseEvent e) { }
}
