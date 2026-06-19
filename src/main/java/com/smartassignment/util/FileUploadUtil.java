package com.smartassignment.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.UUID;
import java.util.Base64;

/**
 * Utility class for handling file uploads, validation, and storage.
 * Enforces business rules: Max Size 10MB, allowed types: PDF, DOCX, JPG, JPEG, PNG.
 * Rewritten to be Servlet-independent by using Base64 decoded payloads.
 */
public class FileUploadUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUploadUtil.class);

    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB
    private static final String[] ALLOWED_EXTENSIONS = {"pdf", "docx", "jpg", "jpeg", "png"};

    /**
     * Validates an uploaded file size and extension.
     * 
     * @param originalName original file name.
     * @param sizeInBytes file size in bytes.
     * @return true if valid, false otherwise.
     */
    public static boolean isValidFile(String originalName, long sizeInBytes) {
        // Validate File Size
        if (sizeInBytes > MAX_FILE_SIZE_BYTES) {
            logger.warn("Validation failed: File size {} exceeds limit {}", sizeInBytes, MAX_FILE_SIZE_BYTES);
            return false;
        }

        // Validate File Extension
        if (originalName == null || originalName.trim().isEmpty()) {
            logger.warn("Validation failed: Original filename is empty");
            return false;
        }

        String extension = getFileExtension(originalName);
        if (extension == null) {
            logger.warn("Validation failed: No file extension found for {}", originalName);
            return false;
        }

        boolean isAllowed = Arrays.stream(ALLOWED_EXTENSIONS)
                .anyMatch(ext -> ext.equalsIgnoreCase(extension));

        if (!isAllowed) {
            logger.warn("Validation failed: File extension '{}' is not allowed", extension);
        }

        return isAllowed;
    }

    /**
     * Extracts the extension of a file.
     * 
     * @param fileName the name of the file.
     * @return the lowercase extension (without dot), or null if none exists.
     */
    public static String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) {
            return null;
        }
        return fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
    }

    /**
     * Saves a Base64-encoded file payload to the server.
     * Generates a unique name to prevent collisions.
     * 
     * @param originalName original name of file.
     * @param base64Content the Base64 content of the file.
     * @param uploadBasePath absolute server path to the uploads directory.
     * @param subFolder subfolder (e.g. "assignments" or "submissions").
     * @return relative database path or null if failure.
     * @throws IOException on write errors.
     */
    public static String saveFile(String originalName, String base64Content, String uploadBasePath, String subFolder) throws IOException {
        if (base64Content == null || base64Content.trim().isEmpty()) {
            logger.warn("Save failed: base64 content is empty");
            return null;
        }

        byte[] fileBytes;
        try {
            // Strip data URL prefixes if present (e.g., data:application/pdf;base64,)
            if (base64Content.contains(",")) {
                base64Content = base64Content.substring(base64Content.indexOf(",") + 1);
            }
            fileBytes = Base64.getDecoder().decode(base64Content.trim());
        } catch (IllegalArgumentException e) {
            logger.error("Failed to decode Base64 file content", e);
            return null;
        }

        if (!isValidFile(originalName, fileBytes.length)) {
            return null;
        }

        String extension = getFileExtension(originalName);
        
        // Generate Unique Filename to avoid collision
        String uniqueFileName = UUID.randomUUID().toString() + "_" + System.currentTimeMillis() + "." + extension;
        
        // Target Directory Path
        String targetDirPath = uploadBasePath + File.separator + subFolder;
        File targetDir = new File(targetDirPath);
        if (!targetDir.exists()) {
            boolean created = targetDir.mkdirs();
            if (created) {
                logger.info("Created upload directory: {}", targetDirPath);
            }
        }

        // Physical File Location
        String filePath = targetDirPath + File.separator + uniqueFileName;
        
        // Save the file
        try (FileOutputStream fos = new FileOutputStream(filePath)) {
            fos.write(fileBytes);
        }
        logger.info("File saved successfully to: {}", filePath);

        // Return relative path for database storage (with standard web slashes)
        return "uploads/" + subFolder + "/" + uniqueFileName;
    }

    /**
     * Deletes a file from the server.
     * 
     * @param uploadBasePath the absolute/context path of the upload folder.
     * @param relativePath the relative path stored in the database.
     * @return true if deleted successfully, false otherwise.
     */
    public static boolean deleteFile(String uploadBasePath, String relativePath) {
        if (relativePath == null || relativePath.trim().isEmpty()) {
            return false;
        }
        
        String cleanedPath = relativePath;
        if (cleanedPath.startsWith("uploads/")) {
            cleanedPath = cleanedPath.substring("uploads/".length());
        } else if (cleanedPath.startsWith("uploads\\")) {
            cleanedPath = cleanedPath.substring("uploads\\".length());
        }

        String fullPath = uploadBasePath + File.separator + cleanedPath.replace("/", File.separator).replace("\\", File.separator);
        File file = new File(fullPath);
        
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                logger.info("Deleted file: {}", fullPath);
            } else {
                logger.warn("Failed to delete file: {}", fullPath);
            }
            return deleted;
        }
        
        logger.warn("File to delete not found: {}", fullPath);
        return false;
    }
}
