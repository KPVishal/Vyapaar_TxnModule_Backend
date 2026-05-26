package com.bijlipay.controller;

import com.bijlipay.service.FileStorageService;
import org.apache.tomcat.util.http.HeaderUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLConnection;
import java.nio.file.Files;

@RestController
@RequestMapping("/txn/file")
public class FileController {
    private static final Logger log = LoggerFactory.getLogger(HeaderUtil.class);
    @Autowired
    private FileStorageService fileStorageService;

    @GetMapping("/downloadFile/{fileName:.+}")
    public ResponseEntity<Resource> downloadFile(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Start to Download File Api ---/downloadFile/{fileName:.+} --- {}", fileName);
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        // Try to determine file's content type
        String contentType = null;
        String mimeType = null;
        try {
            log.info("DOWNLOAD FILE : resource file path : ---- : {}", resource.getFile().getAbsolutePath());
            //String mimeType= URLConnection.guessContentTypeFromName(resource.getFile().getAbsolutePath());
            InputStream is = new BufferedInputStream(Files.newInputStream(resource.getFile().toPath()));
            mimeType = URLConnection.guessContentTypeFromStream(is);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("DOWNLOAD FILE : Could not determine file type.");
        }
        // Fallback to the default content type if type could not be determined
        if (fileName.endsWith(".xlsx")) {
            contentType = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
        } else if (fileName.endsWith(".xls")) {
            contentType = "application/vnd.ms-excel";
        } else if (fileName.endsWith(".csv")) {
            contentType = "text/csv";
        }else if (fileName.endsWith(".pdf")){
            contentType = "application/pdf";
        }
        else {
//            contentType = (contentType != null ? contentType : (mimeType != null ? mimeType : "application/octet-stream"));
            contentType = (contentType != null ? contentType : (mimeType != null ? mimeType : "application/pdf")); //application/octet-stream
        }
        log.info("End for Download File Api ---/downloadFile/{fileName:.+}");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping("/tickets/get-attachments/{fileName}")
    public ResponseEntity<Resource> downloadAttachment(@PathVariable String fileName, HttpServletRequest request) {
        log.info("Start to Download Attachment Api ---/tickets/get-attachments/{fileName}");
        log.info("GET ATTACHMENTS : Processing request to view attachments : -------- : {}", fileName);
        // Load file as Resource
        Resource resource = fileStorageService.loadFileAsResource(fileName);
        // Try to determine file's content type
        String contentType = null;
        String mimeType = null;
        try {
//            log.info("GET ATTACHMENTS : Getting the file path with name : ------------ :");
            log.info("GET ATTACHMENTS : Found the file path name : ------------------- : {}", resource.getFile().getAbsolutePath());
            InputStream is = new BufferedInputStream(Files.newInputStream(resource.getFile().toPath()));
            mimeType = URLConnection.guessContentTypeFromStream(is);
            contentType = request.getServletContext().getMimeType(resource.getFile().getAbsolutePath());
        } catch (IOException ex) {
            log.info("GET ATTACHMENTS : Could not determine file type. : -------------- : ");
        }
        log.info("GET ATTACHMENTS : Mapping content type of the file in response {}: ---- :", contentType);
        // Fallback to the default content type if type could not be determined
        contentType = (contentType != null ? contentType : (mimeType != null ? mimeType : "application/pdf")); //application/octet-stream
        log.info("End For Download Attachment Api ---/tickets/get-attachments/{fileName}");
        return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(resource);
    }
}
