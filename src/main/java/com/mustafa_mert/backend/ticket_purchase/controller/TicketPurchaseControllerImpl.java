package com.mustafa_mert.backend.ticket_purchase.controller;

import com.mustafa_mert.backend.common.response.RestBaseController;
import com.mustafa_mert.backend.common.response.RootEntity;
import com.mustafa_mert.backend.ticket_purchase.dto.PurchaseTicketRequest;
import com.mustafa_mert.backend.ticket_purchase.dto.TicketPurchaseResponse;
import com.mustafa_mert.backend.ticket_purchase.service.TicketPurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequestMapping("/api/v1/ticket-purchase")
@RequiredArgsConstructor
@RestController
public class TicketPurchaseControllerImpl extends RestBaseController implements TicketPurchaseController {

    private final TicketPurchaseService ticketPurchaseService;

    @Operation(
            summary = "Bilet satın al",
            description = "Kullanıcı seçilen etkinlik için 1 ile 3 arasında bilet satın alır.",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Bilet başarıyla satın alındı"),
            @ApiResponse(responseCode = "404", description = "Etkinlik bulunamadı"),
            @ApiResponse(responseCode = "400", description = "Yetersiz bilet stoğu"),

    })
    @PostMapping("/purchase")
    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<RootEntity<TicketPurchaseResponse>> purchaseTicket(@Valid @RequestBody PurchaseTicketRequest purchaseTicketRequest) {
        return ok(ticketPurchaseService.purchaseTicket(purchaseTicketRequest));
    }

    @DeleteMapping("/cancel/{id}")
    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<RootEntity<?>> cancelTicket(@PathVariable Long id) {
        ticketPurchaseService.cancelTicket(id);
        return ResponseEntity.ok(RootEntity.ok());
    }

    @GetMapping("/all")
    @PreAuthorize("hasRole('USER')")
    @Override
    public ResponseEntity<RootEntity<List<TicketPurchaseResponse>>> getAllPurchasedTickets() {
        return ok(ticketPurchaseService.getAllPurchasedTickets());
    }
}
