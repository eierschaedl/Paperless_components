package org.example.paperless_components.Persistance.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Timestamp;

@Entity
@Table(name = "documents")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor

public class DocumentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(name = "documentname")
    private String name;

    private String filepath;

    private String extractedText;

    private Timestamp dateupload;

}