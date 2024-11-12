package org.example.paperless_components.RestAPI.service.mapper;

import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.springframework.stereotype.Component;

@Component
public class DocumentMapper extends AbstractMapper<DocumentEntity, DocumentDto> {
    @Override
    public DocumentDto mapToDto(DocumentEntity source) {
        if (source == null) {
            return null;
        }
        return DocumentDto.builder()
                .id(source.getId())
                .name(source.getName())
                .path(source.getFilepath())
                .dateupload(source.getDateupload())
                .build();
    }

    public DocumentEntity mapToEntity(DocumentDto source) {
        if (source == null) {
            return null;
        }
        return DocumentEntity.builder()
                .id(source.getId())
                .name(source.getName())
                .filepath(source.getPath())
                .dateupload(source.getDateupload())
                .build();
    }
}
