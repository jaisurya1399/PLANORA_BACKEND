package com.projectmanagement.app.ticket;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.projectmanagement.app.user.User;
import com.projectmanagement.app.user.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TicketActivityService {

        private final TicketActivityRepository repository;
        private final TicketRepository ticketRepository;
        private final TicketStatusRepository ticketStatusRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<TicketActivityResponse> getAll() {
                return repository.findAll()
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TicketActivityResponse getById(Long id) {
                TicketActivity activity = repository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket activity not found: " + id));

                return toResponse(activity);
        }

        @Transactional(readOnly = true)
        public List<TicketActivityResponse> getByTicket(Long ticketId) {

                if (!ticketRepository.existsById(ticketId)) {
                        throw new RuntimeException(
                                        "Ticket not found: " + ticketId);
                }

                return repository
                                .findByTicketIdOrderByCreatedAtDesc(ticketId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TicketActivityResponse> getByUser(Long userId) {

                if (!userRepository.existsById(userId)) {
                        throw new RuntimeException(
                                        "User not found: " + userId);
                }

                return repository
                                .findByUserIdOrderByCreatedAtDesc(userId)
                                .stream()
                                .map(this::toResponse)
                                .toList();
        }

        public TicketActivityResponse create(
                        TicketActivityRequest request) {

                Ticket ticket = ticketRepository.findById(request.getTicketId())
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket not found: "
                                                                + request.getTicketId()));

                User user = userRepository.findById(request.getUserId())
                                .orElseThrow(() -> new RuntimeException(
                                                "User not found: "
                                                                + request.getUserId()));

                TicketStatus oldStatus = null;
                TicketStatus newStatus = null;

                if (request.getOldStatusId() != null) {
                        oldStatus = ticketStatusRepository
                                        .findById(request.getOldStatusId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Old ticket status not found: "
                                                                        + request.getOldStatusId()));
                }

                if (request.getNewStatusId() != null) {
                        newStatus = ticketStatusRepository
                                        .findById(request.getNewStatusId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "New ticket status not found: "
                                                                        + request.getNewStatusId()));
                }

                TicketActivity activity = TicketActivity.builder()
                                .ticket(ticket)
                                .oldStatus(oldStatus)
                                .newStatus(newStatus)
                                .user(user)
                                .build();

                return toResponse(repository.save(activity));
        }

        public void delete(Long id) {

                if (!repository.existsById(id)) {
                        throw new RuntimeException(
                                        "Ticket activity not found: " + id);
                }

                repository.deleteById(id);
        }

        @Transactional(readOnly = true)
        public long countByTicket(Long ticketId) {

                if (!ticketRepository.existsById(ticketId)) {
                        throw new RuntimeException(
                                        "Ticket not found: " + ticketId);
                }

                return repository.countByTicketId(ticketId);
        }

        private TicketActivityResponse toResponse(
                        TicketActivity activity) {
                return TicketActivityResponse.builder()
                                .id(activity.getId())

                                .ticketId(activity.getTicket().getId())
                                .ticketName(activity.getTicket().getName())

                                .oldStatusId(
                                                activity.getOldStatus() != null
                                                                ? activity.getOldStatus().getId()
                                                                : null)
                                .oldStatusName(
                                                activity.getOldStatus() != null
                                                                ? activity.getOldStatus().getName()
                                                                : null)

                                .newStatusId(
                                                activity.getNewStatus() != null
                                                                ? activity.getNewStatus().getId()
                                                                : null)
                                .newStatusName(
                                                activity.getNewStatus() != null
                                                                ? activity.getNewStatus().getName()
                                                                : null)

                                .userId(activity.getUser().getId())
                                .userName(activity.getUser().getName())

                                .createdAt(activity.getCreatedAt())
                                .updatedAt(activity.getUpdatedAt())
                                .build();
        }
}