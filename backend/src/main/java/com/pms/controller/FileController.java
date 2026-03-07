package com.pms.controller;

import com.pms.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.UUID;
import java.util.Locale;

/**
 * 交付物文档上传与下载。
 * 上传目录默认为运行目录下的 uploads/deliverables。
 */
@RestController
@RequestMapping("/file")
public class FileController {

    private static final String SUBDIR = "deliverables";

    @Value("${pms.upload-dir:}")
    private String uploadDirOverride;

    private Path getUploadRoot() {
        if (uploadDirOverride != null && !uploadDirOverride.isBlank()) {
            return Paths.get(uploadDirOverride);
        }
        return Paths.get(System.getProperty("user.dir", "."), "uploads");
    }

    /** 上传文档，返回存储相对路径与原始文件名 */
    @PostMapping("/upload")
    public Result<Map<String, String>> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.fail("请选择文件");
        }
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            originalName = "file";
        }
        Path root = getUploadRoot().resolve(SUBDIR);
        try {
            Files.createDirectories(root);
        } catch (IOException e) {
            return Result.fail("创建上传目录失败");
        }
        String ext = "";
        int dot = originalName.lastIndexOf('.');
        if (dot > 0) {
            ext = originalName.substring(dot);
        }
        String savedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path target = root.resolve(savedName);
        try {
            file.transferTo(target.toFile());
        } catch (IOException e) {
            return Result.fail("保存文件失败: " + e.getMessage());
        }
        String relativePath = SUBDIR + "/" + savedName;
        return Result.ok(Map.of("path", relativePath, "name", originalName));
    }

    /** 下载文档，path 为上传接口返回的 path（仅允许 deliverables/ 下） */
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam("path") String path) {
        ResourceAndMeta meta = resolveFile(path);
        if (meta == null) return ResponseEntity.badRequest().build();
        if (meta.resource == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok()
            .contentType(MediaType.APPLICATION_OCTET_STREAM)
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + meta.filename + "\"")
            .body(meta.resource);
    }

    /** 在线预览文档（inline + 按扩展名设置 Content-Type，便于浏览器直接展示） */
    @GetMapping("/preview")
    public ResponseEntity<Resource> preview(@RequestParam("path") String path) {
        ResourceAndMeta meta = resolveFile(path);
        if (meta == null) return ResponseEntity.badRequest().build();
        if (meta.resource == null) return ResponseEntity.notFound().build();
        MediaType contentType = mediaTypeFromFilename(meta.filename);
        return ResponseEntity.ok()
            .contentType(contentType)
            .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + meta.filename + "\"")
            .body(meta.resource);
    }

    private ResourceAndMeta resolveFile(String path) {
        if (path == null || path.isBlank() || path.contains("..")) return null;
        if (!path.startsWith(SUBDIR + "/")) return null;
        Path root = getUploadRoot();
        Path file = root.resolve(path).normalize();
        if (!file.startsWith(root)) return null;
        if (!Files.isRegularFile(file)) return new ResourceAndMeta(null, path.substring(path.lastIndexOf('/') + 1));
        String filename = file.getFileName().toString();
        return new ResourceAndMeta(new FileSystemResource(file), filename);
    }

    private static MediaType mediaTypeFromFilename(String filename) {
        if (filename == null) return MediaType.APPLICATION_OCTET_STREAM;
        String ext = filename.contains(".") ? filename.substring(filename.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT) : "";
        switch (ext) {
            case "pdf": return MediaType.APPLICATION_PDF;
            case "jpg":
            case "jpeg": return MediaType.IMAGE_JPEG;
            case "png": return MediaType.IMAGE_PNG;
            case "gif": return MediaType.IMAGE_GIF;
            case "webp": return MediaType.parseMediaType("image/webp");
            case "svg": return MediaType.parseMediaType("image/svg+xml");
            case "bmp": return MediaType.parseMediaType("image/bmp");
            case "txt": return MediaType.TEXT_PLAIN;
            case "md": return MediaType.parseMediaType("text/markdown");
            case "html":
            case "htm": return MediaType.TEXT_HTML;
            case "xml": return MediaType.APPLICATION_XML;
            case "json": return MediaType.APPLICATION_JSON;
            default: return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private static class ResourceAndMeta {
        final Resource resource;
        final String filename;
        ResourceAndMeta(Resource resource, String filename) {
            this.resource = resource;
            this.filename = filename;
        }
    }
}
