package com.backend.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.backend.model.domain.Diretor;
import com.backend.model.service.ServDiretor;

@RestController
@RequestMapping("/diretores")
public class ControladorDiretor {
    private final ServDiretor servDiretor;

    public ControladorDiretor(ServDiretor servDiretor) {
        this.servDiretor = servDiretor;
    }

    @GetMapping
    public List<Diretor> listar() {
        return servDiretor.listarTodos();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Diretor> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.of(servDiretor.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Diretor> inserir(@RequestBody Diretor diretor) {
        diretor.setId(null);
        Diretor salvo = servDiretor.salvar(diretor);
        return ResponseEntity.created(URI.create("/diretores/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Diretor> atualizar(@PathVariable Integer id, @RequestBody Diretor diretor) {
        return ResponseEntity.of(servDiretor.atualizar(id, diretor));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return servDiretor.excluir(id) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
