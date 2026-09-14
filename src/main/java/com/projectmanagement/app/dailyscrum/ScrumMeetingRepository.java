package com.projectmanagement.app.dailyscrum;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScrumMeetingRepository extends JpaRepository<ScrumMeeting, Long> {
    Optional<ScrumMeeting> findByProjectId(Long projectId);
}
