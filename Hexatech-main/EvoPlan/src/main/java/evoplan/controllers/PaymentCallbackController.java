package evoplan.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PaymentCallbackController {

    @GetMapping("/success")
    public ResponseEntity<String> paymentSuccess(@RequestParam(required = false) String trackingId) {
        return ResponseEntity.ok("✅ Paiement réussi ! Tracking ID : " + (trackingId != null ? trackingId : "Aucun"));
    }

    @GetMapping("/fail")
    public ResponseEntity<String> paymentFail(@RequestParam(required = false) String trackingId) {
        return ResponseEntity.ok("❌ Échec du paiement. Tracking ID : " + (trackingId != null ? trackingId : "Aucun"));
    }

}
