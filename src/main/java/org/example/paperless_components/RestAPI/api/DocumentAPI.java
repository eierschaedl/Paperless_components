package org.example.paperless_components.RestAPI.api;
import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.service.DocumentService;
import org.example.paperless_components.RestAPI.service.DocumentServiceImpl;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping(path = "document")
public class DocumentAPI {
    private final DocumentServiceImpl documentService;
    private final RabbitMQService rabbitMQService;
    private final DocumentRepo documentRepo;

    @Autowired
    public DocumentAPI(DocumentServiceImpl documentService, RabbitMQService rabbitMQService, DocumentRepo documentRepo) {
        this.documentService = documentService;
        this.rabbitMQService = rabbitMQService;
        this.documentRepo = documentRepo;
    }

    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> postDocument(@RequestParam("file") MultipartFile file){
        try {
            String filePath = documentService.saveFile(file);

            DocumentDto documentDto = DocumentDto.builder()
                    .id(null)
                    .name(file.getOriginalFilename())
                    .path(filePath)
                    .build();

            DocumentDto savedDocument = documentService.saveDocumentData(documentDto);

            rabbitMQService.sendMessageToQueue(filePath);
            return ResponseEntity.ok(savedDocument);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(null);
        }
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<List<DocumentDto>> getDocumentByText(@RequestParam String text) { return null; }

    @GetMapping("/{documentId}/metadata")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<DocumentDto> getDocumentMetadata(@PathVariable String documentId) {
        return null;
    }

    @PutMapping("/{documentId}/metadata")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<Void> updateDocumentMetadata(@PathVariable String documentId) { return null; }

    @DeleteMapping("/{documentId}")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<Void> deleteDocument(@PathVariable String documentId) {
        return null;
    }

    @GetMapping("/{documentId}/download")
    @ResponseStatus(HttpStatus.NOT_IMPLEMENTED)
    public ResponseEntity<DocumentDto> getDocumentDownload(@PathVariable String documentId) {
        return null;
    }

    public void updateText(String filepath, String extractedText) {
        DocumentEntity document = documentRepo.findByFilepath(filepath);

        if(document == null){
            throw new IllegalArgumentException("Document with filepath " + filepath + " not found");
        }
        document.setExtractedText(extractedText);
        System.out.println(document.getExtractedText());
        documentRepo.save(document);
    }
}
