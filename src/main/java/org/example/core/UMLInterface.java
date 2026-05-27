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

    public List<UMLMethod> getMethods() { return methods; }
    
    public void removeMethod(UMLMethod method) { methods.remove(method); }

    /**
     * Adjusts the sizes dynamically to ensure text fits inside.
     */
    public void adjustSize() {
        int maxLines = methods.size() + 1; // including <<interface>> string and name
        double requiredLayerHeight = maxLines * 15 + 20;
        double totalRequiredHeight = requiredLayerHeight * 2;
        
        if (this.height < totalRequiredHeight) {
            this.height = totalRequiredHeight;
        }

        double maxTextWidth = 100.0;
        javafx.scene.text.Text textNode = new javafx.scene.text.Text();
        
        textNode.setText("<<interface>>");
        maxTextWidth = Math.max(maxTextWidth, textNode.getLayoutBounds().getWidth() + 20);
        
        textNode.setText(name);
        maxTextWidth = Math.max(maxTextWidth, textNode.getLayoutBounds().getWidth() + 20);

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

        double layerHeight = height / 2.0; // Only two layers
        
        gc.strokeLine(x, y + layerHeight, x + width, y + layerHeight);
        
        gc.setFill(Color.BLACK);
        
        // Name
        gc.fillText("<<interface>>", x + 5, y + 15, width - 10);
        gc.fillText(name, x + 5, y + 30, width - 10);
        
        // Methods
        double currentY = y + layerHeight + 15;
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
