package org.example.core;

/**
 * Represents a connection port on a UML object, located at one of its four directions.
 */
public class Port {
    private BasicObject parent;
    private Direction direction;
    private boolean isConnected;

    /**
     * Constructs a port with its parent object and its relative direction.
     * @param parent the parent UML object.
     * @param direction the direction this port is located.
     */
    public Port(BasicObject parent, Direction direction) {
        this.parent = parent;
        this.direction = direction;
        this.isConnected = false;
    }

    public BasicObject getParent() {
        return parent;
    }

    public Direction getDirection() {
        return direction;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        this.isConnected = connected;
    }

    /**
     * Calculates the absolute X coordinate of this port based on its parent size and position.
     * @return double X coordinate
     */
    public double getX() {
        if (parent == null) return 0.0;
        switch (direction) {
            case LEFT:
                return parent.getX();
            case RIGHT:
                return parent.getX() + parent.getWidth();
            case TOP:
            case BOTTOM:
            default:
                return parent.getX() + parent.getWidth() / 2.0;
        }
    }

    /**
     * Calculates the absolute Y coordinate of this port based on its parent size and position.
     * @return double Y coordinate
     */
    public double getY() {
        if (parent == null) return 0.0;
        switch (direction) {
            case TOP:
                return parent.getY();
            case BOTTOM:
                return parent.getY() + parent.getHeight();
            case LEFT:
            case RIGHT:
            default:
                return parent.getY() + parent.getHeight() / 2.0;
        }
    }
}
