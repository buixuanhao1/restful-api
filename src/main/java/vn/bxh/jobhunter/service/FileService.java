package vn.bxh.jobhunter.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileService {

    @Value("${hao.upload-file.base-uri}")
    private String baseUri;

    // Convert "file:///D:/..." URI string to a plain filesystem Path
    private Path toPath(String uriString) {
        // Strip the "file:///" prefix if present, then build a Path directly
        String normalized = uriString;
        if (normalized.startsWith("file:///")) {
            normalized = normalized.substring(8); // remove "file:///"
        } else if (normalized.startsWith("file://")) {
            normalized = normalized.substring(7);
        } else if (normalized.startsWith("file:/")) {
            normalized = normalized.substring(6);
        }
        return Paths.get(normalized);
    }

    // Sanitize filename: replace spaces and special URI-unsafe chars
    private String sanitizeFilename(String name) {
        if (name == null) return "file";
        // Replace spaces and parentheses with underscores
        return name.replaceAll("[\\s()\\[\\]{}#%&]", "_");
    }

    public void createDirector(String folder) throws URISyntaxException {
        Path path = toPath(folder);
        File tmpDir = new File(path.toString());
        if (!tmpDir.isDirectory()) {
            try {
                Files.createDirectories(tmpDir.toPath());
                System.out.println(">>> CREATE NEW DIRECTORY SUCCESSFUL, PATH = " + tmpDir.toPath());
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println(">>> SKIP MAKING DIRECTORY, ALREADY EXISTS");
        }
    }

    public String store(MultipartFile file, String folder) throws URISyntaxException, IOException {
        String safeName = sanitizeFilename(file.getOriginalFilename());
        String finalName = System.currentTimeMillis() + "-" + safeName;
        Path path = toPath(baseUri + folder + "/" + finalName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
        }
        return finalName;
    }

    public long getFileLength(String fileName, String folder) throws URISyntaxException {
        Path path = toPath(baseUri + folder + "/" + fileName);
        File tmpDir = path.toFile();
        if (!tmpDir.exists() || tmpDir.isDirectory()) return 0;
        return tmpDir.length();
    }

    public InputStreamResource getResource(String fileName, String folder)
            throws URISyntaxException, FileNotFoundException {
        Path path = toPath(baseUri + folder + "/" + fileName);
        return new InputStreamResource(new FileInputStream(path.toFile()));
    }
}
