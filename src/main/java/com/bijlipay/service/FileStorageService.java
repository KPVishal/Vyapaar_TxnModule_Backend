package com.bijlipay.service;

import com.bijlipay.exception.FileStorageException;
import com.bijlipay.exception.MyFileNotFoundException;
import com.bijlipay.properties.FileStorageProperties;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.NoSuchBucketException;

@Service
public class FileStorageService {
    private final Path fileStorageLocation;
    Logger logger = LoggerFactory.getLogger(FileStorageService.class);
    @Value("file.upload-dir")
    private String fileUploadDir;
    @Autowired
    private S3Client s3Client;
    @Value("${amazon.bucket.name}")
    private String amazonBucketName;
    @Value("${file.download.url}")
    private String fileDownloadUrl;

    @Autowired
    public FileStorageService(FileStorageProperties fileStorageProperties) {
        this.fileStorageLocation = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();

        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new FileStorageException("Could not create the directory where the uploaded files will be stored.", ex);
        }
    }

    public String storeFileReturnFileName(MultipartFile file) {
        try {
            String fileNameOriginal = StringUtils.cleanPath(file.getOriginalFilename());
            if (fileNameOriginal.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileNameOriginal);
            } else {
                UUID uuid = UUID.randomUUID();
                String randomUUIDString = uuid.toString();
                String fileExtension = "";
                String mimeType = file.getContentType();
                System.out.println("File mimeType new  is: " + mimeType);
                if (fileNameOriginal.contains(".") && fileNameOriginal.lastIndexOf(".") != 0) {
                    fileExtension = fileNameOriginal.substring(fileNameOriginal.lastIndexOf("."));
                }

                System.out.println("File extension new  is: " + fileExtension);
                String fileNameNew = randomUUIDString + fileExtension;
                String fileName = StringUtils.cleanPath(fileNameNew);
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                return fileName;
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file  Please try again!", ex);
        }
    }

    public Map<String, String> storeFile(MultipartFile file, String fileNameReq) {
        try {
            String fileNameOriginal = StringUtils.cleanPath(file.getOriginalFilename());
            if (fileNameOriginal.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileNameOriginal);
            } else {
                String randomUUIDString = String.valueOf(System.currentTimeMillis());
                if (fileNameReq != null) {
                    randomUUIDString = fileNameReq + "_" + randomUUIDString;
                }

                randomUUIDString = randomUUIDString.replaceAll("[^a-zA-Z0-9_]", "");
                String fileExtension = "";
                String mimeType = file.getContentType();
                if (fileNameOriginal.contains(".") && fileNameOriginal.lastIndexOf(".") != 0) {
                    fileExtension = fileNameOriginal.substring(fileNameOriginal.lastIndexOf("."));
                }

                String fileNameNew = randomUUIDString + fileExtension;
                String fileName = StringUtils.cleanPath(fileNameNew);
                Path targetLocation = this.fileStorageLocation.resolve(fileName);
                Files.copy(file.getInputStream(), targetLocation, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                HashMap<String, String> response = new HashMap();
                response.put("fileName", fileName);
                response.put("mimeType", mimeType);
                response.put("fileNameOriginal", fileNameOriginal);
                return response;
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file  Please try again!", ex);
        }
    }

    public String storeS3File(ResponseBytes<GetObjectResponse> file, String fileNameReq) {
        try {
            String fileNameOriginal = StringUtils.cleanPath(fileNameReq);
            if (fileNameOriginal.contains("..")) {
                throw new FileStorageException("Sorry! Filename contains invalid path sequence " + fileNameOriginal);
            } else {
                String fileExtension = "";
                if (fileNameOriginal.contains(".") && fileNameOriginal.lastIndexOf(".") != 0) {
                    fileExtension = fileNameOriginal.substring(fileNameOriginal.lastIndexOf("."));
                }

                Path targetLocation = this.fileStorageLocation.resolve(fileNameReq);
                Files.copy(file.asInputStream(), targetLocation, new CopyOption[]{StandardCopyOption.REPLACE_EXISTING});
                return fileNameReq;
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not store file  Please try again!", ex);
        }
    }

    public String convertUrlToPdf(String settlementFileUrl) throws IOException {
        String[] parts = settlementFileUrl.split("/");
        String key = parts[parts.length - 1];
        this.logger.info("file key ---------------: {}", key);
        GetObjectRequest getObjectRequest = (GetObjectRequest)GetObjectRequest.builder().bucket(this.amazonBucketName).key(key).build();

        try {
            ResponseBytes<GetObjectResponse> objectBytes = this.s3Client.getObjectAsBytes(getObjectRequest);
            this.storeS3File(objectBytes, key);
            return this.fileDownloadUrl + key;
        } catch (NoSuchBucketException e) {
            throw new RuntimeException("The specified bucket does not exist: " + e.getMessage(), e);
        }
    }

    public Resource loadFileAsResource(String fileName) {
        try {
            Path filePath = this.fileStorageLocation.resolve(fileName).normalize();
            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists()) {
                return resource;
            } else {
                throw new MyFileNotFoundException("File not found " + fileName);
            }
        } catch (MalformedURLException ex) {
            throw new MyFileNotFoundException("File not found " + fileName, ex);
        }
    }
}