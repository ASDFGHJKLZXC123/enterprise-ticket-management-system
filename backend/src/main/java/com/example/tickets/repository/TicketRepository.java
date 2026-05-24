package com.example.tickets.repository;

import com.example.tickets.entity.Ticket;
import com.example.tickets.entity.TicketPriority;
import com.example.tickets.entity.TicketStatus;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface TicketRepository extends JpaRepository<Ticket, Long>, JpaSpecificationExecutor<Ticket> {
    long countByStatus(TicketStatus status);

    long countByPriorityIn(List<TicketPriority> priorities);

    long countByDueAtBeforeAndStatusNotIn(Instant dueAt, List<TicketStatus> statuses);

    @Query("""
        select coalesce(a.fullName, 'Unassigned') as key, count(t) as count
        from Ticket t left join t.assignee a
        group by a.fullName
        order by count(t) desc
        """)
    List<KeyCountProjection> countByAssigneeGroup();

    interface KeyCountProjection {
        String getKey();

        long getCount();
    }
}
