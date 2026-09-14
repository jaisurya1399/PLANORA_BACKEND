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
public class TicketSubscriberService {

        private final TicketSubscriberRepository ticketSubscriberRepository;
        private final TicketRepository ticketRepository;
        private final UserRepository userRepository;

        @Transactional(readOnly = true)
        public List<TicketSubscriberResponse> getAll() {

                return ticketSubscriberRepository.findAll()
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TicketSubscriberResponse getById(Long id) {

                TicketSubscriber subscriber = ticketSubscriberRepository.findById(id)
                                .orElseThrow(() -> new RuntimeException(
                                                "Ticket subscriber not found with id: " + id));

                return mapToResponse(subscriber);
        }

        @Transactional(readOnly = true)
        public List<TicketSubscriberResponse> getByTicket(Long ticketId) {

                validateTicket(ticketId);

                return ticketSubscriberRepository
                                .findByTicketId(ticketId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public List<TicketSubscriberResponse> getByUser(Long userId) {

                validateUser(userId);

                return ticketSubscriberRepository
                                .findByUserId(userId)
                                .stream()
                                .map(this::mapToResponse)
                                .toList();
        }

        @Transactional(readOnly = true)
        public TicketSubscriberResponse getByTicketAndUser(
                        Long ticketId,
                        Long userId) {

                validateTicket(ticketId);
                validateUser(userId);

                TicketSubscriber subscriber = ticketSubscriberRepository
                                .findByTicketIdAndUserId(ticketId, userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User is not subscribed to this ticket"));

                return mapToResponse(subscriber);
        }

        @Transactional(readOnly = true)
        public boolean exists(
                        Long ticketId,
                        Long userId) {

                return ticketSubscriberRepository
                                .existsByTicketIdAndUserId(ticketId, userId);
        }

        public TicketSubscriberResponse create(
                        TicketSubscriberRequest request) {

                validateTicket(request.getTicketId());
                validateUser(request.getUserId());

                if (ticketSubscriberRepository.existsByTicketIdAndUserId(
                                request.getTicketId(),
                                request.getUserId())) {
                        throw new RuntimeException(
                                        "User is already subscribed to this ticket");
                }

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

                TicketSubscriber subscriber = TicketSubscriber.builder()
                                .ticket(ticket)
                                .user(user)
                                .build();

                return mapToResponse(
                                ticketSubscriberRepository.save(subscriber));
        }

        public void delete(Long id) {

                if (!ticketSubscriberRepository.existsById(id)) {
                        throw new RuntimeException(
                                        "Ticket subscriber not found with id: " + id);
                }

                ticketSubscriberRepository.deleteById(id);
        }

        public void deleteByTicket(Long ticketId) {

                validateTicket(ticketId);

                ticketSubscriberRepository.deleteByTicketId(ticketId);
        }

        public void deleteByUser(Long userId) {

                validateUser(userId);

                ticketSubscriberRepository.deleteByUserId(userId);
        }

        public void unsubscribe(
                        Long ticketId,
                        Long userId) {

                TicketSubscriber subscriber = ticketSubscriberRepository
                                .findByTicketIdAndUserId(ticketId, userId)
                                .orElseThrow(() -> new RuntimeException(
                                                "User is not subscribed to this ticket"));

                ticketSubscriberRepository.delete(subscriber);
        }

        @Transactional(readOnly = true)
        public long countByTicket(Long ticketId) {

                validateTicket(ticketId);

                return ticketSubscriberRepository
                                .countByTicketId(ticketId);
        }

        @Transactional(readOnly = true)
        public long countByUser(Long userId) {

                validateUser(userId);

                return ticketSubscriberRepository
                                .countByUserId(userId);
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

        private TicketSubscriberResponse mapToResponse(
                        TicketSubscriber subscriber) {

                Ticket ticket = subscriber.getTicket();
                User user = subscriber.getUser();

                return TicketSubscriberResponse.builder()
                                .id(subscriber.getId())

                                .ticketId(ticket.getId())
                                .ticketName(ticket.getName())
                                .ticketCode(ticket.getCode())

                                .userId(user.getId())
                                .userName(user.getName())
                                .userEmail(user.getEmail())

                                .createdAt(subscriber.getCreatedAt())
                                .updatedAt(subscriber.getUpdatedAt())

                                .build();
        }
}