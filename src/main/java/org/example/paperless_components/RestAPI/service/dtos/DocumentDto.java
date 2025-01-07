package org.example.paperless_components.RestAPI.service.dtos;

import lombok.*;

import java.sql.Timestamp;


@Data
@Setter
@Getter
@AllArgsConstructor
@Builder

public class DocumentDto {

    private Long id;
    private String name;
    private String path;
    private String extractedText;
    private Timestamp dateupload;

}