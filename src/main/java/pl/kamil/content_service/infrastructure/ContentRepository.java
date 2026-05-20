package pl.kamil.content_service.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.kamil.content_service.domain.Content;

import java.util.UUID;

public interface ContentRepository extends JpaRepository<Content, UUID> {

}
