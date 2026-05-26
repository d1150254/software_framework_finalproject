package org.example;

import javafx.embed.swing.SwingFXUtils;
import javafx.geometry.BoundingBox;
import javafx.scene.SnapshotParameters;
import javafx.scene.control.Alert;
import javafx.scene.image.WritableImage;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.example.canvas.UMLCanvas;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class ExportManager {

    public void exportCanvas(Stage owner, UMLCanvas canvas) {
        FileChooser fileChooser = createExportFileChooser();
        File targetFile = fileChooser.showSaveDialog(owner);
        if (targetFile == null) {
            return;
        }

        WritableImage snapshot = captureCanvasSnapshot(canvas);

        try {
            processExportAction(snapshot, targetFile);
        } catch (IOException ex) {
            showExportError(ex);
        }
    }

    private FileChooser createExportFileChooser() {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Export Diagram");
        chooser.setInitialFileName("diagram");
        chooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("PNG Image", "*.png"),
                new FileChooser.ExtensionFilter("PDF Document", "*.pdf")
        );
        return chooser;
    }

    private WritableImage captureCanvasSnapshot(UMLCanvas canvas) {
        canvas.clearSelection();
        canvas.ensureContentVisible();
        canvas.repaint();

        BoundingBox bounds = canvas.getContentBounds();
        SnapshotParameters params = new SnapshotParameters();
        params.setViewport(new javafx.geometry.Rectangle2D(
                bounds.getMinX(), bounds.getMinY(), bounds.getWidth(), bounds.getHeight()
        ));

        WritableImage targetImage = new WritableImage(
                (int) Math.ceil(bounds.getWidth()),
                (int) Math.ceil(bounds.getHeight())
        );
        return canvas.snapshot(params, targetImage);
    }

    private void processExportAction(WritableImage snapshot, File targetFile) throws IOException {
        String fileName = targetFile.getName().toLowerCase();
        if (fileName.endsWith(".pdf")) {
            exportAsPdf(snapshot, targetFile);
        } else {
            exportAsPng(snapshot, targetFile);
        }
    }

    private void exportAsPng(WritableImage image, File targetFile) throws IOException {
        BufferedImage bufferedImage = convertToBufferedImage(image);
        ImageIO.write(bufferedImage, "png", targetFile);
    }

    private void exportAsPdf(WritableImage image, File targetFile) throws IOException {
        BufferedImage bufferedImage = convertToBufferedImage(image);

        try (PDDocument document = new PDDocument()) {
            PDImageXObject pdImage = LosslessFactory.createFromImage(document, bufferedImage);
            
            float width = pdImage.getWidth();
            float height = pdImage.getHeight();
            PDPage page = new PDPage(new PDRectangle(width, height));
            document.addPage(page);

            try (PDPageContentStream contents = new PDPageContentStream(document, page)) {
                contents.drawImage(pdImage, 0, 0, width, height);
            }
            
            document.save(targetFile);
        }
    }

    private BufferedImage convertToBufferedImage(WritableImage image) throws IOException {
        BufferedImage bufferedImage = SwingFXUtils.fromFXImage(image, null);
        if (bufferedImage == null) {
            throw new IOException("Failed to convert image for export.");
        }
        return bufferedImage;
    }

    private void showExportError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Export Failed");
        alert.setHeaderText("An error occurred while exporting the diagram");
        alert.setContentText(ex.getMessage());
        alert.showAndWait();
    }
}
