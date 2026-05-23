package org.example.ui;

import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.GridPane;
import org.example.core.BasicObject;
import org.example.core.UMLAttribute;
import org.example.core.UMLClass;
import org.example.core.UMLInterface;
import org.example.core.UMLMethod;

import java.util.Optional;

public class ObjectEditorDialog {

    public static void showEditDialog(BasicObject obj, Runnable onUpdate) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle("Edit Object");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);

        TextField nameField = new TextField();
        TextField attrField = new TextField();
        TextField methodField = new TextField();

        grid.add(new Label("Object Name:"), 0, 0);
        grid.add(nameField, 1, 0);

        if (obj instanceof UMLClass) {
            UMLClass c = (UMLClass) obj;
            nameField.setText(c.getName());
            
            grid.add(new Label("Add Attribute (Optional):"), 0, 1);
            grid.add(attrField, 1, 1);
            
            grid.add(new Label("Add Method (Optional):"), 0, 2);
            grid.add(methodField, 1, 2);
            
        } else if (obj instanceof UMLInterface) {
            UMLInterface i = (UMLInterface) obj;
            nameField.setText(i.getName());

            grid.add(new Label("Add Method (Optional):"), 0, 1);
            grid.add(methodField, 1, 1);
        }

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        Optional<ButtonType> result = dialog.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (obj instanceof UMLClass) {
                UMLClass c = (UMLClass) obj;
                if (!nameField.getText().trim().isEmpty()) {
                    c.setName(nameField.getText().trim());
                }
                if (!attrField.getText().trim().isEmpty()) {
                    c.addAttribute(new UMLAttribute(attrField.getText().trim()));
                }
                if (!methodField.getText().trim().isEmpty()) {
                    c.addMethod(new UMLMethod(methodField.getText().trim()));
                }
                c.adjustHeight();
            } else if (obj instanceof UMLInterface) {
                UMLInterface i = (UMLInterface) obj;
                if (!nameField.getText().trim().isEmpty()) {
                    i.setName(nameField.getText().trim());
                }
                if (!methodField.getText().trim().isEmpty()) {
                    i.addMethod(new UMLMethod(methodField.getText().trim()));
                }
                i.adjustHeight();
            }
            onUpdate.run();
        }
    }
}
