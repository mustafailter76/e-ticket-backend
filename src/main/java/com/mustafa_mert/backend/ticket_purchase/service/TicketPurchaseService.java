package com.mustafa_mert.backend.ticket_purchase.service;

import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;

import java.util.List;

public interface TicketPurchaseService {
    TicketPurchaseResponse purchaseTicket(PurchaseTicketRequest purchaseTicketRequest);
    void cancelTicket(Long id);
    List<TicketPurchaseResponse> getAllPurchasedTickets();
}
