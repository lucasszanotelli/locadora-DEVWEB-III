package com.backend.model.service;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.backend.model.domain.Cliente;
import com.backend.model.repository.RepoCliente;

@Service
public class ServCliente {
    private final RepoCliente repo;
    private final com.backend.model.repository.RepoLocacao locacoes;

    public ServCliente(RepoCliente repo, com.backend.model.repository.RepoLocacao locacoes) { this.repo = repo; this.locacoes = locacoes; }

    public List<Cliente> listar() { return repo.findAll(); }

    @Transactional
    public Cliente salvar(Cliente cliente) {
        validar(cliente, null);
        cliente.setId(null);
        cliente.setInscricao("PENDENTE-" + UUID.randomUUID());
        Cliente salvo = repo.saveAndFlush(cliente);
        salvo.setInscricao(("Sócio".equals(salvo.getTipo()) ? "SOC-" : "DEP-") + String.format("%04d", salvo.getId()));
        return repo.save(salvo);
    }

    @Transactional
    public Optional<Cliente> atualizar(Integer id, Cliente dados) {
        return repo.findById(id).map(cliente -> {
            validar(dados, id);
            cliente.setNome(dados.getNome().trim());
            cliente.setTipo(dados.getTipo());
            cliente.setCpf(limpar(dados.getCpf()));
            cliente.setTelefone(limpar(dados.getTelefone()));
            cliente.setEndereco(limpar(dados.getEndereco()));
            cliente.setSexo(dados.getSexo());
            cliente.setNascimento(dados.getNascimento());
            cliente.setAtivo(dados.isAtivo());
            cliente.setSocioId(dados.getSocioId());
            if (!cliente.getInscricao().startsWith("SOC-") && "Sócio".equals(cliente.getTipo()))
                cliente.setInscricao("SOC-" + String.format("%04d", id));
            else if (!cliente.getInscricao().startsWith("DEP-") && "Dependente".equals(cliente.getTipo()))
                cliente.setInscricao("DEP-" + String.format("%04d", id));
            return repo.save(cliente);
        });
    }

    @Transactional
    public Optional<Cliente> alterarStatus(Integer id) {
        return repo.findById(id).map(cliente -> {
            boolean ativo = !cliente.isAtivo();
            cliente.setAtivo(ativo);
            if ("Sócio".equals(cliente.getTipo()))
                repo.findBySocioId(id).forEach(dependente -> dependente.setAtivo(ativo));
            return repo.save(cliente);
        });
    }

    @Transactional
    public boolean excluir(Integer id) {
        if (!repo.existsById(id)) return false;
        if (locacoes.existsByClienteId(id) || repo.findBySocioId(id).stream().anyMatch(c -> locacoes.existsByClienteId(c.getId())))
            throw erro("O cliente ou seus dependentes possuem locações e não podem ser excluídos.");
        repo.deleteAll(repo.findBySocioId(id));
        repo.deleteById(id);
        return true;
    }

    private void validar(Cliente c, Integer id) {
        if (c.getNome() == null || c.getNome().isBlank()) throw erro("O nome é obrigatório.");
        c.setNome(c.getNome().trim());
        if (!"Sócio".equals(c.getTipo()) && !"Dependente".equals(c.getTipo())) throw erro("Tipo de cliente inválido.");
        if (c.getSexo() == null || c.getSexo().isBlank()) throw erro("O sexo é obrigatório.");
        try {
            if (c.getNascimento() == null || !c.getNascimento().matches("[0-9]{4}-[0-9]{2}-[0-9]{2}")) throw new DateTimeParseException("Formato inválido", "", 0);
            LocalDate data = LocalDate.parse(c.getNascimento());
            if (data.getYear() < 1 || data.isAfter(LocalDate.now())) throw erro("A data de nascimento deve ser válida e não pode estar no futuro.");
        } catch (DateTimeParseException ex) { throw erro("Data de nascimento inválida. Use AAAA-MM-DD."); }
        if ("Sócio".equals(c.getTipo())) {
            if (vazio(c.getCpf()) || vazio(c.getTelefone()) || vazio(c.getEndereco())) throw erro("CPF, telefone e endereço são obrigatórios para sócios.");
            c.setCpf(c.getCpf().trim());
            if (id == null ? repo.existsByCpf(c.getCpf()) : repo.existsByCpfAndIdNot(c.getCpf(), id)) throw erro("Já existe um cliente cadastrado com este CPF.");
            c.setSocioId(null);
        } else {
            c.setCpf(limpar(c.getCpf())); c.setTelefone(limpar(c.getTelefone())); c.setEndereco(limpar(c.getEndereco()));
            if (c.getSocioId() == null) throw erro("Selecione o sócio responsável.");
            Cliente socio = repo.findById(c.getSocioId()).orElseThrow(() -> erro("Sócio responsável não encontrado."));
            if (!"Sócio".equals(socio.getTipo()) || !socio.isAtivo()) throw erro("Selecione um sócio ativo.");
            if (c.isAtivo() && repo.countBySocioIdAndAtivoTrueAndIdNot(c.getSocioId(), id == null ? 0 : id) >= 3) throw erro("O sócio selecionado já possui três dependentes ativos.");
        }
    }

    private boolean vazio(String valor) { return valor == null || valor.isBlank(); }
    private String limpar(String valor) { return valor == null || valor.isBlank() ? null : valor.trim(); }
    private RuntimeException erro(String mensagem) { return new RespostaClienteInvalida(mensagem); }

    public static class RespostaClienteInvalida extends RuntimeException {
        public RespostaClienteInvalida(String mensagem) { super(mensagem); }
    }
}
