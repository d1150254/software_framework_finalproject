package org.example.core;

import javafx.scene.canvas.GraphicsContext;

/**
 * Interface for all objects that can be drawn on the canvas.
 */
public interface Drawable {
    /**
     * Draws the object using the provided GraphicsContext.
     * @param gc The GraphicsContext of the canvas.
     */
    void draw(GraphicsContext gc);
}

