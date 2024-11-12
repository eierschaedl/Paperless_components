package org.example.paperless_components.RestAPI.service;

import lombok.extern.slf4j.Slf4j;
import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.mapper.DocumentMapper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentServiceImpl {

    private final DocumentMapper documentMapper;
    private final DocumentRepo documentRepository;

    public DocumentServiceImpl(DocumentMapper documentMapper, DocumentRepo documentRepository) {
        this.documentMapper = documentMapper;
        this.documentRepository = documentRepository;
    }

    public DocumentDto uploadDocument(DocumentDto documentDto) {
        DocumentEntity documentEntity = documentMapper.mapToEntity(documentDto);
        DocumentEntity savedEntity = documentRepository.save(documentEntity);

        log.info("createdDocument: {}", savedEntity);

        return documentMapper.mapToDto(savedEntity);
    }
}
