package org.example.core.lines;

import javafx.geometry.BoundingBox;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import org.example.core.Direction;
import org.example.core.Drawable;
import org.example.core.Port;

import java.util.ArrayList;
import java.util.List;

public class RelationshipLine implements Drawable {
    private static final double EXTENSION_LENGTH = 20.0;
    private static final double ARROW_LENGTH = 15.0;
    private static final double VISUAL_PADDING = 6.0;

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
    public LineType getLineType() { return lineType; }
    
    public List<Point2D> getRoutePoints() {
        return calculateOrthogonalRoute();
    }

    public BoundingBox getVisualBounds() {
        List<Point2D> points = calculateOrthogonalRoute();
        if (points.isEmpty()) {
            return new BoundingBox(0, 0, 0, 0);
        }

        List<Point2D> visualPoints = new ArrayList<>(points);
        if (points.size() >= 2 && lineType != LineType.ASSOCIATION) {
            visualPoints.addAll(calculateArrowheadPoints(
                    points.get(points.size() - 2),
                    points.get(points.size() - 1)
            ));
        }

        double minX = Double.MAX_VALUE;
        double minY = Double.MAX_VALUE;
        double maxX = -Double.MAX_VALUE;
        double maxY = -Double.MAX_VALUE;

        for (Point2D point : visualPoints) {
            minX = Math.min(minX, point.getX());
            minY = Math.min(minY, point.getY());
            maxX = Math.max(maxX, point.getX());
            maxY = Math.max(maxY, point.getY());
        }

        minX -= VISUAL_PADDING;
        minY -= VISUAL_PADDING;
        maxX += VISUAL_PADDING;
        maxY += VISUAL_PADDING;

        return new BoundingBox(minX, minY, maxX - minX, maxY - minY);
    }
    
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
        Point2D p1 = extend(sx, sy, startPort.getDirection(), EXTENSION_LENGTH);
        pts.add(p1);

        // Extended end point
        Point2D p2 = extend(ex, ey, endPort.getDirection(), EXTENSION_LENGTH);

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
        List<Point2D> points = calculateArrowheadPoints(p1, p2);
        if (points.isEmpty()) {
            return;
        }

        double[] px = points.stream().mapToDouble(Point2D::getX).toArray();
        double[] py = points.stream().mapToDouble(Point2D::getY).toArray();

        if (lineType == LineType.COMPOSITION) {
            gc.setFill(Color.BLACK);
        } else {
            gc.setFill(Color.WHITE);
        }

        gc.fillPolygon(px, py, points.size());
        gc.strokePolygon(px, py, points.size());
    }

    private List<Point2D> calculateArrowheadPoints(Point2D p1, Point2D p2) {
        List<Point2D> points = new ArrayList<>();
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        double angle = Math.atan2(dy, dx);
        
        double endX = p2.getX();
        double endY = p2.getY();

        if (lineType == LineType.INHERITANCE || lineType == LineType.IMPLEMENTATION) {
            // Triangle
            double x1 = endX - ARROW_LENGTH * Math.cos(angle - Math.PI / 6);
            double y1 = endY - ARROW_LENGTH * Math.sin(angle - Math.PI / 6);
            double x2 = endX - ARROW_LENGTH * Math.cos(angle + Math.PI / 6);
            double y2 = endY - ARROW_LENGTH * Math.sin(angle + Math.PI / 6);

            points.add(new Point2D(endX, endY));
            points.add(new Point2D(x1, y1));
            points.add(new Point2D(x2, y2));
        } else if (lineType == LineType.AGGREGATION || lineType == LineType.COMPOSITION) {
            // Diamond
            double x1 = endX - ARROW_LENGTH * Math.cos(angle - Math.PI / 8);
            double y1 = endY - ARROW_LENGTH * Math.sin(angle - Math.PI / 8);
            double x2 = endX - ARROW_LENGTH * Math.cos(angle + Math.PI / 8);
            double y2 = endY - ARROW_LENGTH * Math.sin(angle + Math.PI / 8);
            
            double tipX = endX - 2 * ARROW_LENGTH * Math.cos(angle) * 0.8;
            double tipY = endY - 2 * ARROW_LENGTH * Math.sin(angle) * 0.8;

            points.add(new Point2D(endX, endY));
            points.add(new Point2D(x1, y1));
            points.add(new Point2D(tipX, tipY));
            points.add(new Point2D(x2, y2));
        }
        return points;
    }
}
