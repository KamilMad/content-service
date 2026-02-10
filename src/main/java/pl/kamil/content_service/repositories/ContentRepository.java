package pl.kamil.content_service.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamil.content_service.models.Content;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {
}
