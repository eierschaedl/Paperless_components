package org.example.paperless_components.RestAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.mapper.DocumentMapper;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Slf4j
@Service
public class DocumentServiceImpl {

    private final DocumentMapper documentMapper;
    private final DocumentRepo documentRepository;
    private final RabbitMQService rabbitMQService;

    public DocumentServiceImpl(DocumentMapper documentMapper, DocumentRepo documentRepository, RabbitMQService rabbitMQService) {
        this.documentMapper = documentMapper;
        this.documentRepository = documentRepository;
        this.rabbitMQService = rabbitMQService;
    }

    public DocumentDto uploadDocument(DocumentDto documentDto) {
        DocumentEntity documentEntity = documentMapper.mapToEntity(documentDto);
        DocumentEntity savedEntity = documentRepository.save(documentEntity);

        log.info("createdDocument: {}", savedEntity);

        // Send message to RabbitMQ
        rabbitMQService.sendMessageToQueue("Document uploaded: " + savedEntity.getId());
        return documentMapper.mapToDto(savedEntity);
    }
}