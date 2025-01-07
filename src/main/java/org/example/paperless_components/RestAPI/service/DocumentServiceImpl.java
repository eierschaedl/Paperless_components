package org.example.paperless_components.RestAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.mapper.DocumentMapper;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;

import java.io.IOException;

@Slf4j
@Service
public class DocumentServiceImpl {

    private final DocumentMapper documentMapper;
    private final DocumentRepo documentRepository;
    private final RabbitMQService rabbitMQService;
    private final MinioClient minioClient;
    private final String bucketName = "documents";

    public DocumentServiceImpl(DocumentMapper documentMapper, DocumentRepo documentRepository, RabbitMQService rabbitMQService, MinioClient minioClient) {
        this.documentMapper = documentMapper;
        this.documentRepository = documentRepository;
        this.rabbitMQService = rabbitMQService;
        this.minioClient = minioClient;
    }

    public String saveFile(MultipartFile file) throws Exception {
        ensureBucketExists();

        String uniqueFileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .stream(file.getInputStream(), file.getSize(), -1)
                        .contentType(file.getContentType())
                        .build()
        );

        return uniqueFileName;
    }

    public DocumentDto saveDocumentData(DocumentDto documentDto) {
        DocumentEntity documentEntity = documentMapper.mapToEntity(documentDto);
        documentEntity = documentRepository.save(documentEntity);
        //DB created id, so now retrieved document has unique id
        return documentMapper.mapToDto(documentEntity);
    }

    public DocumentDto uploadDocument(DocumentDto documentDto) {
        DocumentEntity documentEntity = documentMapper.mapToEntity(documentDto);
        DocumentEntity savedEntity = documentRepository.save(documentEntity);

        log.info("createdDocument: {}", savedEntity);

        // Send message to RabbitMQ
        rabbitMQService.sendMessageToQueue("Document uploaded: " + savedEntity.getId());
        return documentMapper.mapToDto(savedEntity);
    }

    private void ensureBucketExists() throws Exception {
        boolean bucketExists = minioClient.bucketExists(
                io.minio.BucketExistsArgs.builder().bucket(bucketName).build()
        );

        if (!bucketExists) {
            minioClient.makeBucket(
                    io.minio.MakeBucketArgs.builder().bucket(bucketName).build()
            );
        }
    }

}