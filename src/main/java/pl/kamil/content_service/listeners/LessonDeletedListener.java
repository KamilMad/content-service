package pl.kamil.content_service.listeners;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.kamil.content_service.events.LessonDeleteEvent;
import pl.kamil.content_service.exceptions.FileStorageException;
import pl.kamil.content_service.services.FileStorage;

@Slf4j
@Component
@RequiredArgsConstructor
public class LessonDeletedListener {
    private final FileStorage fileStorage;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLessonDeleted(LessonDeleteEvent event) {
        try {
            fileStorage.deleteFile(event.s3Key());
            log.info("File deleted successfully");
        } catch (FileStorageException e) {
            log.error("S3 deletion failed (expected)");
        } catch (Exception e) {
            log.error("S3 deletion failed (unexpected)");
        }
    }
}
