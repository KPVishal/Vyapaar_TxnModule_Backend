package com.bijlipay.controller;

import com.bijlipay.service.FileStorageService;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;
import javax.servlet.http.HttpServletRequest;
import org.apache.tomcat.util.http.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/txn/file"})
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping({"/downloadFile/{fileName:.+}"})
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Start to Download File Api ---/downloadFile/{fileName:.+} --- {}", fileName);
        Resource resource = this.fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        String mimeType = null;

        try {
            log.info("DOWNLOAD FILE : resource file path : ---- : {}", resource.getFile().getAbsolutePath());
            InputStream is = new BufferedInputStream(Files.newInputStream(resource.getFile().toPath()));
            mimeType = URLConnection.guessContentTypeFromStream(is);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException var7) {
            log.info("DOWNLOAD FILE : Could not determine file type.");
        }

        if (fileName.endsWith(".xlsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileName.endsWith(".csv")) {
            contentType = "text/csv";
        } else if (fileName.endsWith(".pdf")) {
            contentType = "application/pdf";
        } else {
            contentType = contentType != null ? contentType : (mimeType != null ? mimeType : "application/pdf");
        }

        log.info("End for Download File Api ---/downloadFile/{fileName:.+}");
        return ((ResponseEntity.BodyBuilder)ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).header("Content-Disposition", new String[]{"attachment; filename=\"" + resource.getFilename() + "\""})).body(resource);
    }

    @GetMapping({"/tickets/get-attachments/{fileName}"})
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Start to Download Attachment Api ---/tickets/get-attachments/{fileName}");
        log.info("GET ATTACHMENTS : Processing request to view attachments : -------- : {}", fileName);
        Resource resource = this.fileStorageService.loadFileAsResource(fileName);
        String contentType = null;
        String mimeType = null;

        try {
            log.info("GET ATTACHMENTS : Found the file path name : ------------------- : {}", resource.getFile().getAbsolutePath());
            InputStream is = new BufferedInputStream(Files.newInputStream(resource.getFile().toPath()));
            mimeType = URLConnection.guessContentTypeFromStream(is);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException var7) {
            log.info("GET ATTACHMENTS : Could not determine file type. : -------------- : ");
        }

        log.info("GET ATTACHMENTS : Mapping content type of the file in response {}: ---- :", contentType);
        contentType = contentType != null ? contentType : (mimeType != null ? mimeType : "application/pdf");
        log.info("End For Download Attachment Api ---/tickets/get-attachments/{fileName}");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }
}