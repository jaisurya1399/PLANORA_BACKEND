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
public class TicketHourService {

    private final TicketHourRepository ticketHourRepository;
    private final TicketRepository ticketRepository;
    private final UserRepository userRepository;
    private final TicketActivityRepository ticketActivityRepository;

    @Transactional(readOnly = true)
    public List<TicketHourResponse> getAll() {

        return ticketHourRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public TicketHourResponse getById(Long id) {

        TicketHour hour = ticketHourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket hour not found with id: " + id));

        return mapToResponse(hour);
    }

    @Transactional(readOnly = true)
    public List<TicketHourResponse> getByTicket(Long ticketId) {

        validateTicket(ticketId);

        return ticketHourRepository
                .findByTicketIdOrderByCreatedAtDesc(ticketId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHourResponse> getByUser(Long userId) {

        validateUser(userId);

        return ticketHourRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHourResponse> getByActivity(Long activityId) {

        validateActivity(activityId);

        return ticketHourRepository
                .findByActivityId(activityId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TicketHourResponse> getByTicketAndUser(
            Long ticketId,
            Long userId) {

        validateTicket(ticketId);
        validateUser(userId);

        return ticketHourRepository
                .findByTicketIdAndUserId(ticketId, userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    public TicketHourResponse create(TicketHourRequest request) {

        Ticket ticket = ticketRepository
                .findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found with id: "
                                + request.getTicketId()));

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: "
                                + request.getUserId()));

        TicketActivity activity = null;

        if (request.getActivityId() != null) {

            activity = ticketActivityRepository
                    .findById(request.getActivityId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ticket activity not found with id: "
                                    + request.getActivityId()));

            if (!activity.getTicket().getId()
                    .equals(request.getTicketId())) {

                throw new RuntimeException(
                        "Activity does not belong to the specified ticket");
            }
        }

        TicketHour hour = TicketHour.builder()
                .ticket(ticket)
                .user(user)
                .value(request.getValue())
                .comment(request.getComment())
                .activity(activity)
                .build();

        return mapToResponse(
                ticketHourRepository.save(hour));
    }

    public TicketHourResponse update(
            Long id,
            TicketHourRequest request) {

        TicketHour hour = ticketHourRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Ticket hour not found with id: " + id));

        Ticket ticket = ticketRepository
                .findById(request.getTicketId())
                .orElseThrow(() -> new RuntimeException(
                        "Ticket not found with id: "
                                + request.getTicketId()));

        User user = userRepository
                .findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException(
                        "User not found with id: "
                                + request.getUserId()));

        TicketActivity activity = null;

        if (request.getActivityId() != null) {

            activity = ticketActivityRepository
                    .findById(request.getActivityId())
                    .orElseThrow(() -> new RuntimeException(
                            "Ticket activity not found with id: "
                                    + request.getActivityId()));

            if (!activity.getTicket().getId()
                    .equals(request.getTicketId())) {

                throw new RuntimeException(
                        "Activity does not belong to the specified ticket");
            }
        }

        hour.setTicket(ticket);
        hour.setUser(user);
        hour.setValue(request.getValue());
        hour.setComment(request.getComment());
        hour.setActivity(activity);

        return mapToResponse(
                ticketHourRepository.save(hour));
    }

    public void delete(Long id) {

        if (!ticketHourRepository.existsById(id)) {
            throw new RuntimeException(
                    "Ticket hour not found with id: " + id);
        }

        ticketHourRepository.deleteById(id);
    }

    public void deleteByTicket(Long ticketId) {

        validateTicket(ticketId);

        ticketHourRepository.deleteByTicketId(ticketId);
    }

    public void deleteByUser(Long userId) {

        validateUser(userId);

        ticketHourRepository.deleteByUserId(userId);
    }

    public void deleteByActivity(Long activityId) {

        validateActivity(activityId);

        ticketHourRepository.deleteByActivityId(activityId);
    }

    @Transactional(readOnly = true)
    public long countByTicket(Long ticketId) {

        validateTicket(ticketId);

        return ticketHourRepository.countByTicketId(ticketId);
    }

    @Transactional(readOnly = true)
    public long countByUser(Long userId) {

        validateUser(userId);

        return ticketHourRepository.countByUserId(userId);
    }

    @Transactional(readOnly = true)
    public long countByActivity(Long activityId) {

        validateActivity(activityId);

        return ticketHourRepository.countByActivityId(activityId);
    }

    private void validateTicket(Long ticketId) {

        if (!ticketRepository.existsById(ticketId)) {
            throw new RuntimeException(
                    "Ticket not found with id: " + ticketId);
        }
    }

    private void validateUser(Long userId) {

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException(
                    "User not found with id: " + userId);
        }
    }

    private void validateActivity(Long activityId) {

        if (!ticketActivityRepository.existsById(activityId)) {
            throw new RuntimeException(
                    "Ticket activity not found with id: " + activityId);
        }
    }

    private TicketHourResponse mapToResponse(
            TicketHour hour) {

        Ticket ticket = hour.getTicket();
        User user = hour.getUser();
        TicketActivity activity = hour.getActivity();

        TicketHourResponse.TicketHourResponseBuilder builder = TicketHourResponse.builder()
                .id(hour.getId())

                .ticketId(ticket.getId())
                .ticketName(ticket.getName())
                .ticketCode(ticket.getCode())

                .userId(user.getId())
                .userName(user.getName())
                .userEmail(user.getEmail())

                .value(hour.getValue())
                .comment(hour.getComment())

                .createdAt(hour.getCreatedAt())
                .updatedAt(hour.getUpdatedAt());

        if (activity != null) {
            builder.activityId(activity.getId());

            if (activity.getOldStatus() != null) {
                builder.activityOldStatusId(
                        activity.getOldStatus().getId());
            }

            if (activity.getNewStatus() != null) {
                builder.activityNewStatusId(
                        activity.getNewStatus().getId());
            }
        }

        return builder.build();
    }
}