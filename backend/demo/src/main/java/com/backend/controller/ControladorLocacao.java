package com.backend.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.backend.model.service.ServLocacao;
import com.backend.model.service.ServLocacao.Dados;
import com.backend.model.service.ServLocacao.Resumo;

@RestController
@RequestMapping("/locacoes")
public class ControladorLocacao {
    private final ServLocacao servico;
    public ControladorLocacao(ServLocacao servico) { this.servico = servico; }
    @GetMapping public List<Resumo> listar() { return servico.listar(); }
    @GetMapping("/vigente") public Resumo vigente(@RequestParam String serie) { return servico.buscarVigente(serie); }
    @GetMapping("/{id}") public Resumo buscar(@PathVariable Integer id) { return servico.buscar(id); }
    @PostMapping public ResponseEntity<Resumo> inserir(@RequestBody Dados dados) {
        Resumo salvo = servico.salvar(dados);
        return ResponseEntity.created(URI.create("/locacoes/" + salvo.id())).body(salvo);
    }
    @PutMapping("/{id}") public Resumo atualizar(@PathVariable Integer id, @RequestBody Dados dados) { return servico.atualizar(id, dados); }
    @DeleteMapping("/{id}") public ResponseEntity<Void> cancelar(@PathVariable Integer id) {
        servico.cancelar(id); return ResponseEntity.noContent().build();
    }
    @PostMapping("/{id}/devolucao") public Resumo devolver(@PathVariable Integer id) { return servico.devolver(id); }
}
