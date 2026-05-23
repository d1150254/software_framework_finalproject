package org.example.state;

import javafx.scene.input.MouseEvent;

/**
 * State pattern interface for toolbar tools.
 */
public interface ToolState {
    void onMousePress(MouseEvent e);
    void onMouseDrag(MouseEvent e);
    void onMouseRelease(MouseEvent e);
    void onMouseMove(MouseEvent e);
}
