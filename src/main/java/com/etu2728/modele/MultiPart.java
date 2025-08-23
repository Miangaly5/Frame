package main.java.com.etu2728.modele;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class MultiPart {
    String fileName;
    long fileSize;
    String contentType;
    InputStream fileContent;

    public MultiPart() {
    }
    public MultiPart(String fileName, long fileSize, String contentType, InputStream fileContent) {
        this.fileName = fileName;
        this.fileSize = fileSize;
        this.contentType = contentType;
        this.fileContent = fileContent;
    }
    
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public long getFileSize() {
        return fileSize;
    }
    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }
    public String getContentType() {
        return contentType;
    }
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
    public InputStream getFileContent() {
        return fileContent;
    }
    public void setFileContent(InputStream fileContent) {
        this.fileContent = fileContent;
    }

    public void uploadToProjectDirectory(String projectPath) throws FileNotFoundException, IOException {
        File fileToSave = new File(projectPath, this.fileName);

        // Sauvegarde du fichier en lisant l'InputStream
        try (FileOutputStream outputStream = new FileOutputStream(fileToSave)) {

            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = this.fileContent.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

        }
    }

}
