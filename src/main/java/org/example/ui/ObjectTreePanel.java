package org.example.ui;

import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTreeCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.example.canvas.UMLCanvas;
import org.example.core.*;

public class ObjectTreePanel extends VBox {
    private static final String CAT_ATTRIBUTES = "Attributes";
    private static final String CAT_METHODS = "Methods";
    private static final String DEFAULT_ATTR_TEMPLATE = "+ newAttr: String";
    private static final String DEFAULT_METHOD_TEMPLATE = "+ newMethod(): void";
    private static final String DEFAULT_NEW_ITEM = "New Item";
    private static final String NODE_ROOT = "Root";
    private static final String MENU_ADD_ITEM = "Add Item";
    private static final String MENU_DELETE = "Delete";
    private static final String ERR_TITLE = "輸入格式錯誤";
    private static final int INDEX_NAME = 0;
    private static final int INDEX_ATTRIBUTES = 1;
    private static final int INDEX_METHODS_CLASS = 2;
    private static final int INDEX_METHODS_INTERFACE = 1;

    private final TreeView<String> treeView;
    private BasicObject currentObject;
    private final UMLCanvas canvas;

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
        
        TreeItem<String> root = new TreeItem<>(NODE_ROOT);
        
        if (obj instanceof UMLClass umlClass) {
            bindClassModel(umlClass, root);
        } else if (obj instanceof UMLInterface umlInterface) {
            bindInterfaceModel(umlInterface, root);
        }
        
        expandAllNodes(root);
        treeView.setRoot(root);
    }

    private void bindClassModel(UMLClass umlClass, TreeItem<String> root) {
        TreeItem<String> nameItem = new TreeItem<>(umlClass.getName());
        
        TreeItem<String> attrRoot = new TreeItem<>(CAT_ATTRIBUTES);
        umlClass.getAttributes().forEach(attr -> attrRoot.getChildren().add(new TreeItem<>(attr.getDisplayText())));
        
        TreeItem<String> methodRoot = new TreeItem<>(CAT_METHODS);
        umlClass.getMethods().forEach(method -> methodRoot.getChildren().add(new TreeItem<>(method.getDisplayText())));
        
        root.getChildren().add(nameItem);
        root.getChildren().add(attrRoot);
        root.getChildren().add(methodRoot);
    }

    private void bindInterfaceModel(UMLInterface umlInterface, TreeItem<String> root) {
        TreeItem<String> nameItem = new TreeItem<>(umlInterface.getName());
        
        TreeItem<String> methodRoot = new TreeItem<>(CAT_METHODS);
        umlInterface.getMethods().forEach(method -> methodRoot.getChildren().add(new TreeItem<>(method.getDisplayText())));
        
        root.getChildren().add(nameItem);
        root.getChildren().add(methodRoot);
    }

    private void expandAllNodes(TreeItem<String> root) {
        root.setExpanded(true);
        root.getChildren().forEach(child -> child.setExpanded(true));
    }
    
    private void updateModelAndCanvas() {
        if (currentObject == null || treeView.getRoot() == null) return;
        
        TreeItem<String> root = treeView.getRoot();
        
        if (currentObject instanceof UMLClass umlClass) {
            updateClassFromTree(umlClass, root);
        } else if (currentObject instanceof UMLInterface umlInterface) {
            updateInterfaceFromTree(umlInterface, root);
        }
        
        canvas.repaint();
    }

    private void updateClassFromTree(UMLClass umlClass, TreeItem<String> root) {
        umlClass.setName(root.getChildren().get(INDEX_NAME).getValue());
        
        umlClass.getAttributes().clear();
        TreeItem<String> attrRoot = root.getChildren().get(INDEX_ATTRIBUTES);
        attrRoot.getChildren().forEach(item -> umlClass.addAttribute(UMLAttribute.parse(item.getValue())));
        
        umlClass.getMethods().clear();
        TreeItem<String> methodRoot = root.getChildren().get(INDEX_METHODS_CLASS);
        methodRoot.getChildren().forEach(item -> umlClass.addMethod(UMLMethod.parse(item.getValue())));

        umlClass.adjustSize();
    }

    private void updateInterfaceFromTree(UMLInterface umlInterface, TreeItem<String> root) {
        umlInterface.setName(root.getChildren().get(INDEX_NAME).getValue());
        
        umlInterface.getMethods().clear();
        TreeItem<String> methodRoot = root.getChildren().get(INDEX_METHODS_INTERFACE);
        methodRoot.getChildren().forEach(item -> umlInterface.addMethod(UMLMethod.parse(item.getValue())));
        
        umlInterface.adjustSize();
    }

    private class PropertyTreeCell extends TextFieldTreeCell<String> {
        private final ContextMenu cellContextMenu = new ContextMenu();
        
        public PropertyTreeCell() {
            super(new StringConverter<>() {
                @Override public String toString(String object) { return object; }
                @Override public String fromString(String string) { return string; }
            });
        }
        
        @Override
        public void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            
            if (empty || item == null) {
                clearCell();
                return;
            }
            setupCellMenuAndEditability();
        }

        private void clearCell() {
            setText(null);
            setContextMenu(null);
        }

        private void setupCellMenuAndEditability() {
            TreeItem<String> treeItem = getTreeItem();
            TreeItem<String> root = getTreeView().getRoot();
            
            if (isCategoryNode(treeItem, root)) {
                setupCategoryNode(treeItem);
            } else {
                setupLeafNode(treeItem, root);
            }
        }

        private boolean isCategoryNode(TreeItem<String> item, TreeItem<String> root) {
            return item.getParent() == root && !root.getChildren().isEmpty() && item != root.getChildren().get(INDEX_NAME);
        }

        private void setupCategoryNode(TreeItem<String> treeItem) {
            MenuItem addItem = new MenuItem(MENU_ADD_ITEM);
            addItem.setOnAction(e -> handleAddItemAction(treeItem));
            cellContextMenu.getItems().setAll(addItem);
            setContextMenu(cellContextMenu);
            setEditable(false);
        }

        private void handleAddItemAction(TreeItem<String> treeItem) {
            String category = treeItem.getValue();
            String template = getTemplateForCategory(category);
            treeItem.getChildren().add(new TreeItem<>(template));
            treeItem.setExpanded(true);
            updateModelAndCanvas();
        }

        private String getTemplateForCategory(String category) {
            if (CAT_ATTRIBUTES.equals(category)) return DEFAULT_ATTR_TEMPLATE;
            if (CAT_METHODS.equals(category)) return DEFAULT_METHOD_TEMPLATE;
            return DEFAULT_NEW_ITEM;
        }

        private void setupLeafNode(TreeItem<String> treeItem, TreeItem<String> root) {
            boolean isNameNode = isNodeNameProperty(treeItem, root);
            
            if (isNameNode) {
                setContextMenu(null);
            } else {
                MenuItem deleteItem = new MenuItem(MENU_DELETE);
                deleteItem.setOnAction(e -> handleDeleteItemAction(treeItem));
                cellContextMenu.getItems().setAll(deleteItem);
                setContextMenu(cellContextMenu);
            }
            setEditable(true);
        }

        private boolean isNodeNameProperty(TreeItem<String> treeItem, TreeItem<String> root) {
            return treeItem.getParent() == root && !root.getChildren().isEmpty() && treeItem == root.getChildren().get(INDEX_NAME);
        }

        private void handleDeleteItemAction(TreeItem<String> treeItem) {
            treeItem.getParent().getChildren().remove(treeItem);
            updateModelAndCanvas();
        }
        
        @Override
        public void commitEdit(String newValue) {
            if (isInvalidInput(newValue)) {
                cancelEdit();
                return;
            }
            String trimmed = newValue.trim();
            
            if (!validateInput(trimmed)) {
                return;
            }
            
            getTreeItem().setValue(trimmed);
            super.commitEdit(trimmed);
            updateModelAndCanvas();
        }

        private boolean isInvalidInput(String newValue) {
            return newValue == null || newValue.trim().isEmpty();
        }

        private boolean validateInput(String trimmed) {
            try {
                parseInputByParentCategory(trimmed);
                return true;
            } catch (IllegalArgumentException ex) {
                handleValidationError(ex);
                return false;
            }
        }

        private void parseInputByParentCategory(String trimmed) {
            TreeItem<String> treeItem = getTreeItem();
            if (treeItem.getParent() == null) return;
            
            String parentValue = treeItem.getParent().getValue();
            if (CAT_ATTRIBUTES.equals(parentValue)) {
                UMLAttribute.parse(trimmed);
            } else if (CAT_METHODS.equals(parentValue)) {
                UMLMethod.parse(trimmed);
            }
        }

        private void handleValidationError(IllegalArgumentException ex) {
            cancelEdit();
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(ERR_TITLE);
            alert.setHeaderText(null);
            alert.setContentText(ex.getMessage());
            alert.showAndWait();
        }
    }
}
