package org.example.paperless_components.service;

import lombok.extern.slf4j.Slf4j;
import org.example.paperless_components.service.dtos.DocumentDto;
import org.example.paperless_components.service.mapper.DocumentService;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DocumentServiceImpl implements DocumentService {

    public DocumentServiceImpl() {}

    @Override
    public DocumentDto uploadDocument(String document) {
        DocumentDto documentDto = new DocumentDto("1","great success");
        return documentDto;
    }
}
