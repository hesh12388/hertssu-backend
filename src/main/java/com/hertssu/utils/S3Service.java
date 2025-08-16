package com.hertssu.utils;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

@Service
@Slf4j
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    //Upload a file to S3 and return the S3 key
    public String uploadFile(MultipartFile file, String keyPrefix) {
        try {
            // Generate unique S3 key
            String fileName = file.getOriginalFilename();
            String s3Key = keyPrefix + System.currentTimeMillis() + "-" + fileName;

            // Create upload request
            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .build();

            // Upload the file
            s3Client.putObject(putRequest, 
                    RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            log.info("Successfully uploaded file to S3: {}", s3Key);
            return s3Key;

        } catch (Exception e) {
            log.error("Failed to upload file to S3: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to upload file to S3", e);
        }
    }

    
     // Generate a presigned URL for downloading a file
    public String generatePresignedDownloadUrl(String s3Key, int durationMinutes) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(Duration.ofMinutes(durationMinutes))
                    .getObjectRequest(getRequest)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);
            
            log.info("Generated presigned URL for S3 key: {}", s3Key);
            return presignedRequest.url().toString();

        } catch (Exception e) {
            log.error("Failed to generate presigned URL for S3 key {}: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("Failed to generate presigned URL", e);
        }
    }

    
     // Delete a file from S3
    
    public void deleteFile(String s3Key) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.deleteObject(deleteRequest);
            log.info("Successfully deleted file from S3: {}", s3Key);

        } catch (Exception e) {
            log.error("Failed to delete file from S3 {}: {}", s3Key, e.getMessage(), e);
            throw new RuntimeException("Failed to delete file from S3", e);
        }
    }

     // Check if a file exists in S3
    public boolean fileExists(String s3Key) {
        try {
            HeadObjectRequest headRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(headRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;
        } catch (Exception e) {
            log.error("Error checking if file exists in S3 {}: {}", s3Key, e.getMessage(), e);
            return false;
        }
    }


     // Get the bucket name (useful for storing in database)
    public String getBucketName() {
        return bucketName;
    }
    
    
}