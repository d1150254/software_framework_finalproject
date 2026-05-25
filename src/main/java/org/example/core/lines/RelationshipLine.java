package org.example.core.lines;

import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.core.Direction;
import org.example.core.Drawable;
import org.example.core.Port;

import java.util.ArrayList;
import java.util.List;

public class RelationshipLine implements Drawable {
    private Port startPort;
    private Port endPort;
    private LineType lineType;

    public RelationshipLine(Port startPort, Port endPort, LineType lineType) {
        this.startPort = startPort;
        this.endPort = endPort;
        this.lineType = lineType;
        
        startPort.setConnected(true);
        endPort.setConnected(true);
    }

    public Port getStartPort() { return startPort; }
    public Port getEndPort() { return endPort; }
    
    private boolean isSelected = false;
    public void setSelected(boolean selected) { this.isSelected = selected; }
    public boolean isSelected() { return isSelected; }

    public boolean contains(double mx, double my) {
        List<Point2D> points = calculateOrthogonalRoute();
        if (points.size() < 2) return false;
        
        double tolerance = 5.0;
        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            
            double d1 = Math.hypot(mx - p1.getX(), my - p1.getY());
            double d2 = Math.hypot(mx - p2.getX(), my - p2.getY());
            double lineLen = Math.hypot(p2.getX() - p1.getX(), p2.getY() - p1.getY());
            
            if (d1 + d2 >= lineLen - tolerance && d1 + d2 <= lineLen + tolerance) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void draw(GraphicsContext gc) {
        List<Point2D> points = calculateOrthogonalRoute();
        
        if (points.size() < 2) return;

        gc.setLineWidth(1.5);
        if (lineType == LineType.IMPLEMENTATION) {
            gc.setLineDashes(5);
        } else {
            gc.setLineDashes(0);
        }
        gc.setStroke(isSelected ? Color.BLUE : Color.BLACK);
        gc.setFill(Color.WHITE); // For arrowheads

        for (int i = 0; i < points.size() - 1; i++) {
            Point2D p1 = points.get(i);
            Point2D p2 = points.get(i + 1);
            gc.strokeLine(p1.getX(), p1.getY(), p2.getX(), p2.getY());
        }
        
        // Reset dashes for arrowhead
        gc.setLineDashes(0);
        
        if (lineType != LineType.ASSOCIATION) {
            drawArrowhead(gc, points.get(points.size() - 2), points.get(points.size() - 1));
        }
    }

    /**
     * Manhattan routing strategy: simple orthogonal layout with extending segments.
     */
    private List<Point2D> calculateOrthogonalRoute() {
        List<Point2D> pts = new ArrayList<>();
        double sx = startPort.getX();
        double sy = startPort.getY();
        double ex = endPort.getX();
        double ey = endPort.getY();
        
        double midX = (sx + ex) / 2;
        double midY = (sy + ey) / 2;

        pts.add(new Point2D(sx, sy));
        
        // Extended start point based on direction
        double extScale = 20;
        Point2D p1 = extend(sx, sy, startPort.getDirection(), extScale);
        pts.add(p1);

        // Extended end point
        Point2D p2 = extend(ex, ey, endPort.getDirection(), extScale);

        // Middle points to form steps (simplified horizontal/vertical transition)
        // A more advanced routing would check crossings, but simple S-shape suffices here.
        if (startPort.getDirection() == Direction.LEFT || startPort.getDirection() == Direction.RIGHT) {
            pts.add(new Point2D(midX, p1.getY()));
            pts.add(new Point2D(midX, p2.getY()));
        } else {
            pts.add(new Point2D(p1.getX(), midY));
            pts.add(new Point2D(p2.getX(), midY));
        }

        pts.add(p2);
        pts.add(new Point2D(ex, ey));

        return pts;
    }

    private Point2D extend(double x, double y, Direction dir, double dist) {
        switch (dir) {
            case TOP: return new Point2D(x, y - dist);
            case BOTTOM: return new Point2D(x, y + dist);
            case LEFT: return new Point2D(x - dist, y);
            case RIGHT: return new Point2D(x + dist, y);
        }
        return new Point2D(x, y);
    }

    private void drawArrowhead(GraphicsContext gc, Point2D p1, Point2D p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double angle = Math.atan2(dy, dx);
        
        double length = 15;
        double width = 8;
        
        double endX = p2.getX();
        double endY = p2.getY();

        if (lineType == LineType.INHERITANCE || lineType == LineType.IMPLEMENTATION) {
            // Triangle
            double x1 = endX - length * Math.cos(angle - Math.PI / 6);
            double y1 = endY - length * Math.sin(angle - Math.PI / 6);
            double x2 = endX - length * Math.cos(angle + Math.PI / 6);
            double y2 = endY - length * Math.sin(angle + Math.PI / 6);
            
            gc.fillPolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
            gc.strokePolygon(new double[]{endX, x1, x2}, new double[]{endY, y1, y2}, 3);
            
        } else if (lineType == LineType.AGGREGATION || lineType == LineType.COMPOSITION) {
            // Diamond
            double cx = endX - (length / 2) * Math.cos(angle);
            double cy = endY - (length / 2) * Math.sin(angle);
            
            double x1 = endX - length * Math.cos(angle - Math.PI / 8);
            double y1 = endY - length * Math.sin(angle - Math.PI / 8);
            double x2 = endX - length * Math.cos(angle + Math.PI / 8);
            double y2 = endY - length * Math.sin(angle + Math.PI / 8);
            
            double tipX = endX - 2 * length * Math.cos(angle) * 0.8;
            double tipY = endY - 2 * length * Math.sin(angle) * 0.8;

            double[] px = {endX, x1, tipX, x2};
            double[] py = {endY, y1, tipY, y2};
            
            if (lineType == LineType.COMPOSITION) {
                gc.setFill(Color.BLACK);
            } else {
                gc.setFill(Color.WHITE);
            }
            
            gc.fillPolygon(px, py, 4);
            gc.strokePolygon(px, py, 4);
        }
    }
}
