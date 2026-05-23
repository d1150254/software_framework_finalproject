package org.example.core;

import java.util.ArrayList;
import java.util.List;

/**
 * Abstract base class for all UML objects.
 * Implements core properties, port management, and base selection logic.
 */
public abstract class BasicObject implements Drawable, Selectable {
    protected double x;
    protected double y;
    protected double width;
    protected double height;
    protected boolean isSelected;
    protected List<Port> ports;

    protected static final double MIN_WIDTH = 100;
    protected static final double MIN_HEIGHT = 80;
    protected static final double RESIZE_MARGIN = 5.0;

    public BasicObject(double x, double y, double width, double height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.isSelected = false;
        
        // Initialize the four directional connection ports
        this.ports = new ArrayList<>();
        this.ports.add(new Port(this, Direction.TOP));
        this.ports.add(new Port(this, Direction.BOTTOM));
        this.ports.add(new Port(this, Direction.LEFT));
        this.ports.add(new Port(this, Direction.RIGHT));
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }

    public void setX(double x) { this.x = x; }
    public void setY(double y) { this.y = y; }
    public void setWidth(double width) { this.width = width; }
    public void setHeight(double height) { this.height = height; }

    public List<Port> getPorts() {
        return ports;
    }

    @Override
    public boolean contains(double px, double py) {
        // Bounding box hit testing
        return px >= x && px <= (x + width) && py >= y && py <= (y + height);
    }

    @Override
    public void setSelected(boolean selected) {
        this.isSelected = selected;
    }

    public boolean isSelected() {
        return isSelected;
    }

    /**
     * Determines which resize zone the coordinates fall into.
     * @param px Mouse X
     * @param py Mouse Y
     * @return ResizeZone enum
     */
    public ResizeZone getResizeZone(double px, double py) {
        if (!containsExpanded(px, py)) return ResizeZone.NONE;

        boolean top = Math.abs(py - y) <= RESIZE_MARGIN;
        boolean bottom = Math.abs(py - (y + height)) <= RESIZE_MARGIN;
        boolean left = Math.abs(px - x) <= RESIZE_MARGIN;
        boolean right = Math.abs(px - (x + width)) <= RESIZE_MARGIN;

        if (top && left) return ResizeZone.NW;
        if (top && right) return ResizeZone.NE;
        if (bottom && left) return ResizeZone.SW;
        if (bottom && right) return ResizeZone.SE;
        if (top) return ResizeZone.N;
        if (bottom) return ResizeZone.S;
        if (left) return ResizeZone.W;
        if (right) return ResizeZone.E;

        return ResizeZone.NONE;
    }

    private boolean containsExpanded(double px, double py) {
        return px >= x - RESIZE_MARGIN && px <= (x + width) + RESIZE_MARGIN &&
               py >= y - RESIZE_MARGIN && py <= (y + height) + RESIZE_MARGIN;
    }

    /**
     * Resizes the object based on the dragged delta and zone.
     */
    public void resize(ResizeZone zone, double dx, double dy) {
        double newWidth, newHeight, newX, newY;
        
        switch (zone) {
            case E:
                width = Math.max(MIN_WIDTH, width + dx);
                break;
            case S:
                height = Math.max(MIN_HEIGHT, height + dy);
                break;
            case SE:
                width = Math.max(MIN_WIDTH, width + dx);
                height = Math.max(MIN_HEIGHT, height + dy);
                break;
            case W:
                newWidth = Math.max(MIN_WIDTH, width - dx);
                if (newWidth > MIN_WIDTH) { x += dx; width = newWidth; }
                break;
            case N:
                newHeight = Math.max(MIN_HEIGHT, height - dy);
                if (newHeight > MIN_HEIGHT) { y += dy; height = newHeight; }
                break;
            case NW:
                newWidth = Math.max(MIN_WIDTH, width - dx);
                newHeight = Math.max(MIN_HEIGHT, height - dy);
                if (newWidth > MIN_WIDTH) { x += dx; width = newWidth; }
                if (newHeight > MIN_HEIGHT) { y += dy; height = newHeight; }
                break;
            case NE:
                width = Math.max(MIN_WIDTH, width + dx);
                newHeight = Math.max(MIN_HEIGHT, height - dy);
                if (newHeight > MIN_HEIGHT) { y += dy; height = newHeight; }
                break;
            case SW:
                newWidth = Math.max(MIN_WIDTH, width - dx);
                if (newWidth > MIN_WIDTH) { x += dx; width = newWidth; }
                height = Math.max(MIN_HEIGHT, height + dy);
                break;
            default:
                break;
        }
    }
}
