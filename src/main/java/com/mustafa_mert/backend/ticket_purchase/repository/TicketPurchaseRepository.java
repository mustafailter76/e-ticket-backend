package com.mustafa_mert.backend.ticket_purchase.repository;

import com.mustafa_mert.backend.ticket_purchase.entity.TicketPurchase;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TicketPurchaseRepository extends JpaRepository<TicketPurchase, Long> {
    List<TicketPurchase> findByUserId(Long userId);
}
