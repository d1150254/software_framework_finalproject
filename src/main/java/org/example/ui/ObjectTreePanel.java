package org.example.ui;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.canvas.UMLCanvas;
import org.example.core.*;

public class ObjectTreePanel extends VBox {
    private TreeView<String> treeView;
    private BasicObject currentObject;
    private UMLCanvas canvas;

    public ObjectTreePanel(UMLCanvas canvas) {
        this.canvas = canvas;
        setPrefWidth(200);
        setStyle("-fx-background-color: #ffffff; -fx-border-color: #cccccc; -fx-border-width: 0 0 0 1;");
        
        Label title = new Label("Properties");
        title.setStyle("-fx-font-weight: bold; -fx-padding: 5;");
        
        treeView = new TreeView<>();
        treeView.setEditable(true);
        treeView.setShowRoot(false);
        
        treeView.setCellFactory(tv -> new PropertyTreeCell());
        
        getChildren().addAll(title, treeView);
    }

    public void bindObject(BasicObject obj) {
        this.currentObject = obj;
        if (obj == null) {
            treeView.setRoot(null);
            return;
        }
        
        TreeItem<String> root = new TreeItem<>("Root");
        
        if (obj instanceof UMLClass) {
            UMLClass c = (UMLClass) obj;
            
            TreeItem<String> nameItem = new TreeItem<>(c.getName());
            
            TreeItem<String> attrRoot = new TreeItem<>("Attributes");
            for (UMLAttribute attr : c.getAttributes()) {
                attrRoot.getChildren().add(new TreeItem<>(attr.getName()));
            }
            
            TreeItem<String> methodRoot = new TreeItem<>("Methods");
            for (UMLMethod method : c.getMethods()) {
                methodRoot.getChildren().add(new TreeItem<>(method.getName()));
            }
            
            root.getChildren().addAll(nameItem, attrRoot, methodRoot);
        } else if (obj instanceof UMLInterface) {
            UMLInterface i = (UMLInterface) obj;
            
            TreeItem<String> nameItem = new TreeItem<>(i.getName());
            
            TreeItem<String> methodRoot = new TreeItem<>("Methods");
            for (UMLMethod method : i.getMethods()) {
                methodRoot.getChildren().add(new TreeItem<>(method.getName()));
            }
            
            root.getChildren().addAll(nameItem, methodRoot);
        }
        
        root.setExpanded(true);
        for (TreeItem<String> child : root.getChildren()) {
            child.setExpanded(true);
        }
        
        treeView.setRoot(root);
    }
    
    private void updateModelAndCanvas() {
        if (currentObject == null) return;
        
        TreeItem<String> root = treeView.getRoot();
        if (root == null) return;
        
        if (currentObject instanceof UMLClass) {
            UMLClass c = (UMLClass) currentObject;
            
            String newName = root.getChildren().get(0).getValue();
            c.setName(newName);
            
            c.getAttributes().clear();
            TreeItem<String> attrRoot = root.getChildren().get(1);
            for (TreeItem<String> attrItem : attrRoot.getChildren()) {
                c.addAttribute(new UMLAttribute(attrItem.getValue()));
            }
            
            c.getMethods().clear();
            TreeItem<String> methodRoot = root.getChildren().get(2);
            for (TreeItem<String> methodItem : methodRoot.getChildren()) {
                c.addMethod(new UMLMethod(methodItem.getValue()));
            }
            
            c.adjustHeight();
        } else if (currentObject instanceof UMLInterface) {
            UMLInterface i = (UMLInterface) currentObject;
            
            String newName = root.getChildren().get(0).getValue();
            i.setName(newName);
            
            i.getMethods().clear();
            TreeItem<String> methodRoot = root.getChildren().get(1);
            for (TreeItem<String> methodItem : methodRoot.getChildren()) {
                i.addMethod(new UMLMethod(methodItem.getValue()));
            }
            
            i.adjustHeight();
        }
        
        canvas.repaint();
    }

    private class PropertyTreeCell extends TextFieldTreeCell<String> {
        private ContextMenu contextMenu = new ContextMenu();
        
        public PropertyTreeCell() {
            super(new StringConverter<String>() {
                @Override public String toString(String object) { return object; }
                @Override public String fromString(String string) { return string; }
            });
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                setText(null);
                setContextMenu(null);
            } else {
                TreeItem<String> treeItem = getTreeItem();
                TreeItem<String> root = getTreeView().getRoot();
                
                // Root's child 0 is Name. Category roots are the other children of Root.
                boolean isCategory = (treeItem.getParent() == root && !root.getChildren().isEmpty() && treeItem != root.getChildren().get(0));
                
                if (isCategory) {
                    MenuItem addItem = new MenuItem("Add Item");
                    addItem.setOnAction(e -> {
                        treeItem.getChildren().add(new TreeItem<>("New Item"));
                        treeItem.setExpanded(true);
                        updateModelAndCanvas();
                    });
                    contextMenu.getItems().setAll(addItem);
                    setContextMenu(contextMenu);
                    setEditable(false);
                } else {
                    MenuItem deleteItem = new MenuItem("Delete");
                    deleteItem.setOnAction(e -> {
                        treeItem.getParent().getChildren().remove(treeItem);
                        updateModelAndCanvas();
                    });
                    
                    boolean isName = (treeItem.getParent() == root && !root.getChildren().isEmpty() && treeItem == root.getChildren().get(0));
                    
                    if (isName) {
                        setContextMenu(null);
                    } else {
                        contextMenu.getItems().setAll(deleteItem);
                        setContextMenu(contextMenu);
                    }
                    setEditable(true);
                }
            }
        }
        
        @Override
        public void commitEdit(String newValue) {
            if (newValue == null || newValue.trim().isEmpty()) {
                cancelEdit();
                return;
            }
            String trimmed = newValue.trim();
            
            // Force the underlying TreeItem to reflect the new value immediately 
            // before the tree model is parsed for synchronization.
            getTreeItem().setValue(trimmed);
            super.commitEdit(trimmed);
            
            updateModelAndCanvas();
        }
    }
}
