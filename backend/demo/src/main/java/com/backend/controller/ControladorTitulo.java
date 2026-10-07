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

import com.backend.model.domain.Titulo;
import com.backend.model.service.ServTitulo;

@RestController
@RequestMapping("/titulos")
public class ControladorTitulo {
    private final ServTitulo servTitulo;

    public ControladorTitulo(ServTitulo servTitulo) { this.servTitulo = servTitulo; }

    @GetMapping
    public List<Titulo> listar() { return servTitulo.listarTodos(); }

    @GetMapping("/{id}")
    public ResponseEntity<Titulo> buscarPorId(@PathVariable Integer id) {
        return ResponseEntity.of(servTitulo.buscarPorId(id));
    }

    @PostMapping
    public ResponseEntity<Titulo> inserir(@RequestBody Titulo titulo) {
        titulo.setId(null);
        Titulo salvo = servTitulo.salvar(titulo);
        return ResponseEntity.created(URI.create("/titulos/" + salvo.getId())).body(salvo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Titulo> atualizar(@PathVariable Integer id, @RequestBody Titulo titulo) {
        return ResponseEntity.of(servTitulo.atualizar(id, titulo));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return servTitulo.excluir(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
