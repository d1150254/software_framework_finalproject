package org.example.state;

import javafx.scene.input.MouseEvent;
import org.example.canvas.UMLCanvas;
import org.example.core.BasicObject;
import org.example.core.Port;
import org.example.core.lines.LineType;
import org.example.core.lines.RelationshipLine;

public class CreateConnectionMode implements ToolState {
    private UMLCanvas canvas;
    private LineType lineType;
    private Port startPort;
    private double currentMouseX, currentMouseY; // For drawing temp line during drag
    private BasicObject nearestObject;

    public CreateConnectionMode(UMLCanvas canvas, LineType lineType) {
        this.canvas = canvas;
        this.lineType = lineType;
    }

    @Override
    public void onMousePress(MouseEvent e) {
        startPort = findHoveredPort(e.getX(), e.getY());
        if (startPort != null) {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
        }
    }

    @Override
    public void onMouseDrag(MouseEvent e) {
        if (startPort != null) {
            currentMouseX = e.getX();
            currentMouseY = e.getY();
            nearestObject = findNearestObject(currentMouseX, currentMouseY);
        }
    }

    @Override
    public void onMouseRelease(MouseEvent e) {
        if (startPort != null) {
            Port endPort = findHoveredPort(e.getX(), e.getY());
            // Prevent self-connection and valid target
            if (endPort != null && endPort.getParent() != startPort.getParent()) {
                RelationshipLine line = new RelationshipLine(startPort, endPort, lineType);
                canvas.addLine(line);
                canvas.triggerActionCompleted();
            }
            startPort = null;
            canvas.repaint();
        }
    }

    @Override
    public void onMouseMove(MouseEvent e) {
        currentMouseX = e.getX();
        currentMouseY = e.getY();
        nearestObject = findNearestObject(currentMouseX, currentMouseY);
    }
    
    @Override
    public void drawPreview(javafx.scene.canvas.GraphicsContext gc) {
        if (nearestObject != null) {
            gc.save();
            gc.setFill(javafx.scene.paint.Color.BLACK);
            for (Port p : nearestObject.getPorts()) {
                gc.fillRect(p.getX() - 3, p.getY() - 3, 6, 6);
            }
            gc.restore();
        }

        if (startPort != null) {
            gc.save();
            gc.setGlobalAlpha(0.5);
            gc.setLineDashes(5.0);
            gc.setStroke(javafx.scene.paint.Color.GRAY);
            gc.setLineWidth(2.0);
            gc.strokeLine(startPort.getX(), startPort.getY(), currentMouseX, currentMouseY);
            gc.restore();
        }
    }

    private BasicObject findNearestObject(double mx, double my) {
        BasicObject nearest = null;
        double minDist = Double.MAX_VALUE;
        for (BasicObject obj : canvas.getObjects()) {
            double cx = obj.getX() + obj.getWidth() / 2.0;
            double cy = obj.getY() + obj.getHeight() / 2.0;
            double dist = Math.hypot(mx - cx, my - cy);
            if (dist < minDist) {
                minDist = dist;
                nearest = obj;
            }
        }
        return nearest;
    }

    private Port findHoveredPort(double mx, double my) {
        for (BasicObject obj : canvas.getObjects()) {
            for (Port p : obj.getPorts()) {
                double px = p.getX();
                double py = p.getY();
                // Simple distance check using Math.hypot instead of Point2D static method
                if (Math.hypot(mx - px, my - py) < 15) {
                    return p;
                }
            }
        }
        return null;
    }
}
