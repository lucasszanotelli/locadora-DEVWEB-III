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

import com.backend.model.domain.Item;
import com.backend.model.service.ServItem;

@RestController
@RequestMapping("/itens")
public class ControladorItem {
    private final ServItem servItem;

    public ControladorItem(ServItem servItem) {
        this.servItem = servItem;
    }

    @GetMapping
    public List<Item> listar() {
        return servItem.listarTodos();
    }

    @GetMapping("/{serial}")
    public ResponseEntity<Item> buscarPorId(@PathVariable Integer serial) {
        return ResponseEntity.of(servItem.buscarPorId(serial));
    }

    @PostMapping
    public ResponseEntity<Item> inserir(@RequestBody Item item) {
        item.setSerial(null);
        Item salvo = servItem.salvar(item);
        return ResponseEntity.created(URI.create("/itens/" + salvo.getSerial())).body(salvo);
    }

    @PutMapping("/{serial}")
    public ResponseEntity<Item> atualizar(@PathVariable Integer serial, @RequestBody Item item) {
        return ResponseEntity.of(servItem.atualizar(serial, item));
    }

    @DeleteMapping("/{serial}")
    public ResponseEntity<Void> excluir(@PathVariable Integer serial) {
        return servItem.excluir(serial) ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
