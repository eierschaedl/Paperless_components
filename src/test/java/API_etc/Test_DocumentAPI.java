package API_etc;

import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.api.DocumentAPI;
import org.example.paperless_components.RestAPI.service.DocumentServiceImpl;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class Test_DocumentAPI {

    @Mock
    private DocumentServiceImpl documentService;

    @Mock
    private RabbitMQService rabbitMQService;

    @Mock
    private DocumentRepo documentRepo;

    @InjectMocks
    private DocumentAPI documentAPI;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void postDocument_shouldReturnSavedDocumentDto() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getOriginalFilename()).thenReturn("test.txt");
        when(documentService.saveFile(mockFile)).thenReturn("/path/to/test.txt");

        DocumentDto mockDocumentDto = DocumentDto.builder()
                .name("test.txt")
                .path("/path/to/test.txt")
                .build();
        when(documentService.saveDocumentData(any(DocumentDto.class))).thenReturn(mockDocumentDto);

        ResponseEntity<DocumentDto> response = documentAPI.postDocument(mockFile);

        assertEquals(200, response.getStatusCodeValue());
        assertNotNull(response.getBody());
        assertEquals("test.txt", response.getBody().getName());
        verify(rabbitMQService).sendMessageToQueue("/path/to/test.txt");
    }

    @Test
    void postDocument_shouldReturn500Exception() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(documentService.saveFile(mockFile)).thenThrow(new IOException("File save error"));

        ResponseEntity<DocumentDto> response = documentAPI.postDocument(mockFile);

        assertEquals(500, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void updateText_shouldUpdateDocumentExtractedText() {
        String filepath = "/path/to/document.txt";
        String extractedText = "Extracted text";
        DocumentEntity mockEntity = new DocumentEntity();
        mockEntity.setExtractedText(null);

        when(documentRepo.findByFilepath(filepath)).thenReturn(mockEntity);

        documentAPI.updateText(filepath, extractedText);

        assertEquals(extractedText, mockEntity.getExtractedText());
        verify(documentRepo).save(mockEntity);
    }

    @Test
    void updateText_shouldThrowExceptionIfDocumentNotFound() {
        String filepath = "/invalid/path";
        when(documentRepo.findByFilepath(filepath)).thenReturn(null);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            documentAPI.updateText(filepath, "Extracted text");
        });
        assertEquals("Document with filepath /invalid/path not found", exception.getMessage());
    }
}
