package com.backend.excecoes;

import java.time.Instant;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converte erros de domínio de ator em respostas HTTP consistentes. */
@RestControllerAdvice(assignableTypes = com.backend.controller.ControladorAtor.class)
public class TratadorExcecaoAtor {

    @ExceptionHandler(ExcecaoAtor.class)
    public ResponseEntity<RespostaErro> tratar(ExcecaoAtor excecao, HttpServletRequest requisicao) {
        var status = excecao.getStatus();
        var corpo = new RespostaErro(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                excecao.getMessage(),
                requisicao.getRequestURI());
        return ResponseEntity.status(status).body(corpo);
    }

    public record RespostaErro(Instant timestamp, int status, String error, String message, String path) {}
}
