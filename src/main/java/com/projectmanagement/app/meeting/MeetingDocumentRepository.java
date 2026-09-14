package com.projectmanagement.app.meeting;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface MeetingDocumentRepository extends JpaRepository<MeetingDocument, Long> {
    List<MeetingDocument> findByMeetingId(Long meetingId);

    Optional<MeetingDocument> findByMeetingIdAndDocumentId(Long meetingId, Long documentId);
}
