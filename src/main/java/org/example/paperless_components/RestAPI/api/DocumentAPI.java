package org.example.paperless_components.RestAPI.api;
import org.example.paperless_components.RestAPI.service.DocumentService;
import org.example.paperless_components.RestAPI.service.DocumentServiceImpl;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping(path = "document")
public class DocumentAPI {
    private final DocumentServiceImpl documentService;
    private final RabbitMQService rabbitMQService;

    @Autowired
    public DocumentAPI(DocumentServiceImpl documentService, RabbitMQService rabbitMQService) {
        this.documentService = documentService;
        this.rabbitMQService = rabbitMQService;
    }
    @PostMapping("/upload")
    public ResponseEntity<DocumentDto> postDocument(@RequestBody DocumentDto documentDto) {
        DocumentDto uploadedDocument = documentService.uploadDocument(documentDto);
        System.out.println("uploadedDocument: " + uploadedDocument.getPath());
        rabbitMQService.sendMessageToQueue(uploadedDocument.getPath());
        return ResponseEntity.ok(uploadedDocument);
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
    public ResponseEntity<Void> updateDocumentMetadata(@PathVariable String documentId) {
        return null;
    }

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


}
