package com.backend.excecoes;

import java.util.Map;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import com.backend.controller.ControladorLocacao;

@RestControllerAdvice(assignableTypes = ControladorLocacao.class)
public class TratadorExcecaoLocacao {
    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> tratar(ResponseStatusException e) {
        return ResponseEntity.status(e.getStatusCode()).body(Map.of("message", e.getReason() == null ? "Operação inválida." : e.getReason()));
    }
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<?> conflito(DataIntegrityViolationException e) {
        return ResponseEntity.status(409).body(Map.of("message", "O item já possui uma locação vigente. Atualize a lista."));
    }
}
