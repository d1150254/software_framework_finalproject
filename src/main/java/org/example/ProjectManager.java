package org.example;

import javafx.scene.control.Alert;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.example.canvas.UMLCanvas;
import org.example.persistence.UMLProjectStore;

import java.io.File;
import java.io.IOException;

public class ProjectManager {

    private File currentProjectFile = null;
    private final UMLProjectStore projectStore;

    public ProjectManager(UMLProjectStore projectStore) {
        this.projectStore = projectStore;
    }

    public void openProject(Stage owner, UMLCanvas canvas) {
        FileChooser fileChooser = createProjectFileChooser("Open UML Project");
        File file = fileChooser.showOpenDialog(owner);
        if (file == null) {
            return;
        }

        try {
            projectStore.load(canvas, file.toPath());
            currentProjectFile = file;
        } catch (IOException | IllegalArgumentException ex) {
            showError("Open Failed", "Unable to open the selected UML project file.", ex);
        }
    }

    public void saveProject(Stage owner, UMLCanvas canvas) {
        File file;
        if (currentProjectFile != null) {
            file = currentProjectFile;
        } else {
            FileChooser fileChooser = createProjectFileChooser("Save UML Project");
            fileChooser.setInitialFileName("uml-project.json");
            file = fileChooser.showSaveDialog(owner);
            if (file == null) {
                return;
            }
        }

        try {
            projectStore.save(canvas, file.toPath());
            currentProjectFile = file;
        } catch (IOException | IllegalArgumentException ex) {
            showError("Save Failed", "Unable to save the UML project file.", ex);
        }
    }

    private FileChooser createProjectFileChooser(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("UML Project JSON", "*.json")
        );
        return fileChooser;
    }

    private void showError(String title, String message, Exception exception) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(message);
        alert.setContentText(exception.getMessage());
        alert.showAndWait();
    }
}

