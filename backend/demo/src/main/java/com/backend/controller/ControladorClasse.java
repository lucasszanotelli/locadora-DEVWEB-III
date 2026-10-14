package com.backend.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.model.domain.Classe;
import com.backend.model.service.ServClasse;

@RestController
@RequestMapping("/classes")
public class ControladorClasse {

    private final ServClasse service;

    public ControladorClasse(ServClasse service) {
        this.service = service;
    }

    @GetMapping
    public List<Classe> listar() {
        return service.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Classe> buscar(@PathVariable Integer id) {
        return ResponseEntity.of(service.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Classe> inserir(@RequestBody Classe classe) {
        Classe salvo = service.salvar(classe);
        return ResponseEntity.created(URI.create("/classes/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Classe> atualizar(@PathVariable Integer id, @RequestBody Classe classe) {
        return ResponseEntity.of(service.atualizar(id, classe));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return service.excluir(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
