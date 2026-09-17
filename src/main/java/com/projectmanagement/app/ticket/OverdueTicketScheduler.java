package com.projectmanagement.app.ticket;

import org.springframework.stereotype.Component;

import com.projectmanagement.app.notification.TicketNotificationService;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class OverdueTicketScheduler {

    private final TicketRepository ticketRepository;
    private final TicketNotificationService ticketNotificationService;

    /**
     * Checks every 5 minutes. Each overdue ticket is notified only once
     * until its due date is changed, completed, or otherwise resolved.
     */
    // @Scheduled(fixedDelayString =
    // "${app.scheduler.overdue-ticket.fixed-delay-ms:300000}")
    // @Transactional
    // public void notifyOverdueTickets() {
    // for (Ticket ticket :
    // ticketRepository.findOverdueNotYetNotified(LocalDateTime.now())) {
    // ticketNotificationService.notifyProjectAdminsOfOverdue(ticket);
    // ticket.setOverdueNotifiedAt(LocalDateTime.now());
    // ticketRepository.save(ticket);
    // }
    // }
}
