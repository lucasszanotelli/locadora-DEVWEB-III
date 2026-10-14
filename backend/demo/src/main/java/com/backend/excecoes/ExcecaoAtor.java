package com.backend.excecoes;

import org.springframework.http.HttpStatus;

/** Exceção de domínio para erros nas operações de ator. */
public class ExcecaoAtor extends RuntimeException {
    private final HttpStatus status;

    public ExcecaoAtor(HttpStatus status, String mensagem) {
        super(mensagem);
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
