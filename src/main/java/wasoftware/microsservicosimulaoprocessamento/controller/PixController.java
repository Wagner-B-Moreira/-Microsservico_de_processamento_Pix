package wasoftware.microsservicosimulaoprocessamento.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import wasoftware.microsservicosimulaoprocessamento.dto.PixRequestDTO;
import wasoftware.microsservicosimulaoprocessamento.entity.PixTransaction;
import wasoftware.microsservicosimulaoprocessamento.service.PixService;

@RestController
@RequestMapping("/api/v1/pix")
public class PixController {
    private final PixService pixService;

    public PixController(PixService pixService) {
        this.pixService = pixService;
    }

    @PostMapping
    public ResponseEntity<PixTransaction> createPix(@Valid @RequestBody PixRequestDTO requestDTO) {
        PixTransaction createdTransaction = pixService.createPixCharge(requestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdTransaction);
    }
}
