package org.example.core;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class UMLClass extends BasicObject {
    private String name;
    private List<UMLAttribute> attributes;
    private List<UMLMethod> methods;

    public UMLClass(double x, double y) {
        super(x, y, 100, 120);
        this.name = "Class";
        this.attributes = new ArrayList<>();
        this.methods = new ArrayList<>();
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return name; }
    
    public void addAttribute(UMLAttribute attr) { attributes.add(attr); }
    public void addMethod(UMLMethod method) { methods.add(method); }

    public List<UMLAttribute> getAttributes() { return attributes; }
    public List<UMLMethod> getMethods() { return methods; }
    
    public void removeAttribute(UMLAttribute attr) { attributes.remove(attr); }
    public void removeMethod(UMLMethod method) { methods.remove(method); }

    /**
     * Adjusts the sizes dynamically to ensure text fits inside.
     */
    public void adjustSize() {
        int maxLines = Math.max(attributes.size(), methods.size());
        double requiredLayerHeight = maxLines * 15 + 20;
        double totalRequiredHeight = requiredLayerHeight * 3;
        
        if (this.height < totalRequiredHeight) {
            this.height = totalRequiredHeight;
        }

        double maxTextWidth = 100.0;
        javafx.scene.text.Text textNode = new javafx.scene.text.Text();
        
        textNode.setText(name);
        maxTextWidth = Math.max(maxTextWidth, textNode.getLayoutBounds().getWidth() + 20);

        for (UMLAttribute attr : attributes) {
            textNode.setText(attr.getDisplayText());
            maxTextWidth = Math.max(maxTextWidth, textNode.getLayoutBounds().getWidth() + 20);
        }
        for (UMLMethod method : methods) {
            textNode.setText(method.getDisplayText());
            maxTextWidth = Math.max(maxTextWidth, textNode.getLayoutBounds().getWidth() + 20);
        }
        
        if (maxTextWidth > 300) maxTextWidth = 300;
        if (this.width < maxTextWidth) {
            this.width = maxTextWidth;
        }
    }

    @Override
    public void draw(GraphicsContext gc) {
        gc.setLineWidth(1.5);
        gc.setFill(Color.WHITE);
        gc.fillRect(x, y, width, height);

        gc.setStroke(isSelected ? Color.BLUE : Color.BLACK);
        gc.strokeRect(x, y, width, height);

        double layerHeight = height / 3.0;
        
        // Lines
        gc.strokeLine(x, y + layerHeight, x + width, y + layerHeight);
        gc.strokeLine(x, y + 2 * layerHeight, x + width, y + 2 * layerHeight);
        
        gc.setFill(Color.BLACK);
        
        // Name
        gc.fillText(name, x + 5, y + 15, width - 10);
        
        // Attributes
        double currentY = y + layerHeight + 15;
        for (UMLAttribute attr : attributes) {
            gc.fillText(attr.getDisplayText(), x + 5, currentY, width - 10);
            currentY += 15;
            if (currentY > y + 2 * layerHeight) break;
        }

        // Methods
        currentY = y + 2 * layerHeight + 15;
        for (UMLMethod method : methods) {
            gc.fillText(method.getDisplayText(), x + 5, currentY, width - 10);
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
