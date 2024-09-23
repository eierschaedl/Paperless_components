package org.example.paperless_components.RestAPI.service.mapper;

import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;

public interface DocumentService {

    DocumentDto uploadDocument(String document);

}
