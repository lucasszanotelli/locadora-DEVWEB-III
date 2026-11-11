package com.backend.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.model.domain.Cliente;
import com.backend.model.service.ServCliente;

@RestController
@RequestMapping("/clientes")
public class ControladorCliente {
    private final ServCliente servico;
    public ControladorCliente(ServCliente servico) { this.servico = servico; }

    @GetMapping public List<Cliente> listar() { return servico.listar(); }
    @PostMapping public ResponseEntity<Cliente> inserir(@RequestBody Cliente cliente) {
        Cliente salvo = servico.salvar(cliente);
        return ResponseEntity.created(URI.create("/clientes/" + salvo.getId())).body(salvo);
    }
    @PutMapping("/{id}") public ResponseEntity<Cliente> atualizar(@PathVariable Integer id, @RequestBody Cliente cliente) {
        return ResponseEntity.of(servico.atualizar(id, cliente));
    }
    @PatchMapping("/{id}/status") public ResponseEntity<Cliente> alterarStatus(@PathVariable Integer id) {
        return ResponseEntity.of(servico.alterarStatus(id));
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> excluir(@PathVariable Integer id) {
        return servico.excluir(id) ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}
