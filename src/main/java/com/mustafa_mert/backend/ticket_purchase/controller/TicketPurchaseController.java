package com.mustafa_mert.backend.ticket_purchase.controller;

import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface TicketPurchaseController {
    ResponseEntity<RootEntity<TicketPurchaseResponse>> purchaseTicket(PurchaseTicketRequest purchaseTicketRequest);
    ResponseEntity<RootEntity<?>> cancelTicket(Long id);
    ResponseEntity<RootEntity<List<TicketPurchaseResponse>>> getAllPurchasedTickets();
}
