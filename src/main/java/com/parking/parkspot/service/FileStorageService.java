package com.parking.parkspot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path fileStorageLocation;

    public FileStorageService(@Value("${app.file.upload-dir:uploads/reportes}") String uploadDir) {
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("No se pudo crear el directorio donde se almacenarán los archivos.", ex);
        }
    }

    public String storeFile(MultipartFile file) {
        // Verificar si el archivo no está vacío
        String originalFileName = file.getOriginalFilename();
        if (file.isEmpty() || originalFileName == null || originalFileName.trim().isEmpty()) {
            throw new RuntimeException("No se puede almacenar un archivo vacío");
        }
        
        // Normalizar nombre del archivo
        String fileName = StringUtils.cleanPath(originalFileName);

        try {
            // Verificar si el nombre contiene secuencias inválidas
            if (fileName.contains("..")) {
                throw new RuntimeException("El nombre del archivo contiene una secuencia de ruta inválida " + fileName);
            }

            // Generar un nombre único para evitar conflictos
            String fileExtension = "";
            if (fileName.contains(".")) {
                fileExtension = fileName.substring(fileName.lastIndexOf("."));
            }
            
            String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

            // Copiar archivo al directorio destino
            Path targetLocation = this.fileStorageLocation.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            return uniqueFileName;
        } catch (IOException ex) {
            throw new RuntimeException("No se pudo almacenar el archivo " + fileName + ". Inténtalo de nuevo.", ex);
        }
    }

    public Path loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            if (Files.exists(filePath)) {
                return filePath;
            } else {
                throw new RuntimeException("Archivo no encontrado " + fileName);
            }
        } catch (Exception ex) {
            throw new RuntimeException("Archivo no encontrado " + fileName, ex);
        }
    }

    public boolean deleteFile(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            return Files.deleteIfExists(filePath);
        } catch (IOException ex) {
            return false;
        }
    }

    public String getFileStorageLocation() {
        return fileStorageLocation.toString();
    }
}
