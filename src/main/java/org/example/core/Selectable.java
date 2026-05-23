package org.example.core;

/**
 * Interface for all objects that can be interacting and selected by mouse.
 */
public interface Selectable {
    /**
     * Checks if the given coordinates fall within the boundaries of this object.
     * @param x The mouse X coordinate.
     * @param y The mouse Y coordinate.
     * @return true if the coordinate is inside the object, false otherwise.
     */
    boolean contains(double x, double y);

    /**
     * Sets the selection state of the object.
     * @param selected true to select, false to deselect.
     */
    void setSelected(boolean selected);
}

