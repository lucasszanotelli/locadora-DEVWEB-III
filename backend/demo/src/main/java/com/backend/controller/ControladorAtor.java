package com.backend.controller;

import java.net.URI;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.backend.model.domain.Ator;
import com.backend.model.service.ServAtor;

@RestController
@RequestMapping("/atores")
public class ControladorAtor {

    private final ServAtor servAtor;

    public ControladorAtor(ServAtor servAtor) {
        this.servAtor = servAtor;
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> buscarPorId(@PathVariable Integer id) {
        Optional<Ator> ator = servAtor.buscarPorId(id);

        if (ator.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("não encontrado");
        }

        return ResponseEntity.ok(ator.get());
    }

    @PostMapping
    public ResponseEntity<Ator> inserir(@RequestBody Ator ator) {
        Ator atorSalvo = servAtor.salvar(ator);
        URI localizacao = URI.create("/atores/" + atorSalvo.getId());

        return ResponseEntity.created(localizacao).body(atorSalvo);
    }
}
