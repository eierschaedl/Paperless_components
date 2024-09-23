package org.example.paperless_components.RestAPI.service.dtos;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DocumentDto {
    private String id;
    private String title;

    public DocumentDto(String id, String title) {
        this.id = id;
        this.title = title;
    }

}
