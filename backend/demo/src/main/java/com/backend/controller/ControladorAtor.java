package com.backend.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.model.domain.Ator;
import com.backend.model.service.ServAtor;

@RestController
@RequestMapping("/atores")
public class ControladorAtor {
    private final ServAtor servAtor;

    public ControladorAtor(ServAtor servAtor) {
        this.servAtor = servAtor;
    }

    @GetMapping
    public List<Ator> listar() {
        return servAtor.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ator> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.of(servAtor.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Ator> inserir(@RequestBody Ator ator) {
        ator.setId(null);
        Ator salvo = servAtor.salvar(ator);
        return ResponseEntity.created(URI.create("/atores/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ator> atualizar(@PathVariable Integer id, @RequestBody Ator ator) {
        return ResponseEntity.of(servAtor.atualizar(id, ator));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return servAtor.excluir(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
