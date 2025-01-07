package API_etc;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.Persistance.repos.DocumentRepo;
import org.example.paperless_components.RestAPI.service.DocumentServiceImpl;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.mapper.DocumentMapper;
import org.example.paperless_components.RestAPI.service.rabbitmq.RabbitMQService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class Test_DocumentServiceImpl {
    private DocumentServiceImpl documentService;
    private DocumentMapper documentMapper;
    private DocumentRepo documentRepository;
    private RabbitMQService rabbitMQService;
    private MinioClient minioClient;

    @BeforeEach
    void setUp() {
        documentMapper = mock(DocumentMapper.class);
        documentRepository = mock(DocumentRepo.class);
        rabbitMQService = mock(RabbitMQService.class);
        minioClient = mock(MinioClient.class);

        documentService = new DocumentServiceImpl(documentMapper, documentRepository, rabbitMQService, minioClient);
    }

    @Test
    void saveFile_shouldUploadFileAndReturnUniqueFileName() throws Exception {
        MultipartFile file = mock(MultipartFile.class);
        String fileName = "testFile.txt";
        InputStream fileContent = new ByteArrayInputStream("test content".getBytes());

        when(file.getOriginalFilename()).thenReturn(fileName);
        when(file.getInputStream()).thenReturn(fileContent);
        when(file.getSize()).thenReturn((long) fileContent.available());
        when(file.getContentType()).thenReturn("text/plain");
        when(minioClient.bucketExists(any(BucketExistsArgs.class))).thenReturn(true);

        String uniqueFileName = documentService.saveFile(file);

        assertNotNull(uniqueFileName);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void saveDocumentData_shouldSaveAndReturnMappedDto() {
        DocumentDto inputDto = DocumentDto.builder()
                .id(null)
                .name("Document Name")
                .path("/path/to/document")
                .build();

        DocumentEntity entity = DocumentEntity.builder()
                .id(1L)
                .name("Document Name")
                .filepath("/path/to/document")
                .build();

        when(documentMapper.mapToEntity(inputDto)).thenReturn(entity);
        when(documentRepository.save(entity)).thenReturn(entity);
        when(documentMapper.mapToDto(entity)).thenReturn(inputDto);

        DocumentDto resultDto = documentService.saveDocumentData(inputDto);

        assertNotNull(resultDto);
        assertEquals(inputDto, resultDto);
        verify(documentRepository, times(1)).save(entity);
    }

    @Test
    void uploadDocument_shouldSaveDocumentAndSendMessageToQueue() {
        DocumentDto inputDto = DocumentDto.builder()
                .id(null)
                .name("Document Name")
                .path("/path/to/document")
                .build();

        DocumentEntity entity = DocumentEntity.builder()
                .id(1L)
                .name("Document Name")
                .filepath("/path/to/document")
                .build();

        when(documentMapper.mapToEntity(inputDto)).thenReturn(entity);
        when(documentRepository.save(entity)).thenReturn(entity);
        when(documentMapper.mapToDto(entity)).thenReturn(inputDto);

        DocumentDto resultDto = documentService.uploadDocument(inputDto);

        assertNotNull(resultDto);
        verify(documentRepository, times(1)).save(entity);
        verify(rabbitMQService, times(1)).sendMessageToQueue("Document uploaded: " + entity.getId());
    }
}
