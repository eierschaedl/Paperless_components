package org.example.paperless_components.Persistance.repos;

import org.example.paperless_components.Persistance.entities.DocumentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentRepo extends JpaRepository<DocumentEntity, Long>{
}