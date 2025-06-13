// Smjestite u: src/main/java/hr/srsveze/sredisnji_registar/controller/VezaController.java
package hr.srsveze.sredisnji_registar.controller;

import hr.srsveze.sredisnji_registar.service.VezaService; // Import vašeg servisa
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api")
public class VezaController {

    private final VezaService vezaService;

    @Autowired
    public VezaController(VezaService vezaService) {
        this.vezaService = vezaService;
    }

    @PostMapping("/search")
    public ResponseEntity<Map<String, Object>> searchOib(@RequestParam String oib) {
        if (oib == null || !oib.matches("\\d{11}")) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Neispravan format OIB-a. OIB mora imati 11 znamenki.");
            return ResponseEntity.badRequest().body(errorResponse);
        }
        try {
            Map<String, Object> result = vezaService.pronadjiSveVeze(oib);

            if (result.containsKey("error") && "OIB nije pronađen.".equals(result.get("error")) ) {
                 return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
            }
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Neočekivana greška u VezaController za OIB " + oib + ": " + e.getMessage());
            e.printStackTrace();
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("error", "Dogodila se interna greška na serveru.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}