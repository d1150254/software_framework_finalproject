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
            canvas.repaint(); // In a real app we'd draw temp line in canvas repaint logic
            // For simplicity, we just rely on the final release for now.
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
            }
            startPort = null;
            canvas.repaint();
        }
    }

    @Override
    public void onMouseMove(MouseEvent e) { }

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
