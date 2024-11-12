package org.example.paperless_components.RestAPI.service;

import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;

public interface DocumentService {

    DocumentDto uploadDocument(DocumentDto documentDto);

}
