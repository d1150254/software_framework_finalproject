package org.example.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class UMLInterface extends BasicObject {
    private String name;
    private List<UMLMethod> methods;

    public UMLInterface(double x, double y) {
        super(x, y, 100, 100);
        this.name = "Interface";
        this.methods = new ArrayList<>();
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    
    public void addMethod(UMLMethod method) { methods.add(method); }

    /**
     * Adjusts the height of the interface dynamically to ensure text fits inside.
     */
    public void adjustHeight() {
        int maxLines = methods.size() + 1; // including <<interface>> string and name
        double requiredLayerHeight = maxLines * 15 + 20;
        double totalRequiredHeight = requiredLayerHeight * 2;
        
        if (this.height < totalRequiredHeight) {
            this.height = totalRequiredHeight;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(1.5);
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);

        gc.setStroke(isSelected ? Color.BLUE : Color.BLACK);
        gc.strokeRect(x, y, width, height);

        double layerHeight = height / 2.0; // Only two layers
        
        gc.strokeLine(x, y + layerHeight, x + width, y + layerHeight);
        
        gc.setFill(Color.BLACK);
        
        // Name
        gc.fillText("<<interface>>", x + 5, y + 15);
        gc.fillText(name, x + 5, y + 30);
        
        // Methods
        double currentY = y + layerHeight + 15;
        for (UMLMethod method : methods) {
            gc.fillText(method.getName(), x + 5, currentY);
            currentY += 15;
            if (currentY > y + height) break;
        }

        // Draw ports if selected
        if (isSelected) {
            gc.setFill(Color.BLACK);
            for (Port p : ports) {
                gc.fillRect(p.getX() - 3, p.getY() - 3, 6, 6);
            }
        }
    }
}
