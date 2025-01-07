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

    @Column(name = "filepath")
    private String filepath;

    @Column(name = "extracted_text", columnDefinition = "TEXT")
    private String extractedText;
}