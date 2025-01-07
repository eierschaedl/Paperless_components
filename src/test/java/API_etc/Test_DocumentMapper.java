package API_etc;

import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.example.paperless_components.RestAPI.service.dtos.DocumentDto;
import org.example.paperless_components.RestAPI.service.mapper.DocumentMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Test_DocumentMapper {

    private DocumentMapper documentMapper;

    @BeforeEach
    void setUp() {
        documentMapper = new DocumentMapper();
    }

    @Test
    void mapToDto_shouldReturnDtoWithCorrectValues() {
        DocumentEntity entity = DocumentEntity.builder()
                .id(1L)
                .name("Document Name")
                .filepath("/path/to/document")
                .extractedText("Sample extracted text")
                .build();

        DocumentDto dto = documentMapper.mapToDto(entity);

        assertNotNull(dto);
        assertEquals(entity.getId(), dto.getId());
        assertEquals(entity.getName(), dto.getName());
        assertEquals(entity.getFilepath(), dto.getPath());
        assertEquals(entity.getExtractedText(), dto.getExtractedText());
    }

    @Test
    void mapToDto_shouldReturnNullWhenSourceIsNull() {
        DocumentDto dto = documentMapper.mapToDto((DocumentEntity) null);

        assertNull(dto);
    }

    @Test
    void mapToEntity_shouldReturnEntityWithCorrectValues() {
        DocumentDto dto = DocumentDto.builder()
                .id(1L)
                .name("Document Name")
                .path("/path/to/document")
                .extractedText("Sample extracted text")
                .build();

        DocumentEntity entity = documentMapper.mapToEntity(dto);

        assertNotNull(entity);
        assertEquals(dto.getId(), entity.getId());
        assertEquals(dto.getName(), entity.getName());
        assertEquals(dto.getPath(), entity.getFilepath());
        assertEquals(dto.getExtractedText(), entity.getExtractedText());
    }

    @Test
    void mapToEntity_shouldReturnNullWhenSourceIsNull() {
        DocumentEntity entity = documentMapper.mapToEntity(null);

        assertNull(entity);
    }
}