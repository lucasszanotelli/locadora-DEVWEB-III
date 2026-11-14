package com.backend.model.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.backend.model.domain.Cliente;
import com.backend.model.domain.Item;
import com.backend.model.domain.Locacao;
import com.backend.model.repository.RepoClasse;
import com.backend.model.repository.RepoCliente;
import com.backend.model.repository.RepoItem;
import com.backend.model.repository.RepoLocacao;
import com.backend.model.repository.RepoTitulo;

@Service
@Transactional
public class ServLocacao {

    private final RepoLocacao repo;
    private final RepoCliente clientes;
    private final RepoItem itens;
    private final RepoTitulo titulos;
    private final RepoClasse classes;
    private final BigDecimal valorDiarioMulta;

    public ServLocacao(
            RepoLocacao repo,
            RepoCliente clientes,
            RepoItem itens,
            RepoTitulo titulos,
            RepoClasse classes,
            @Value("${locadora.multa.valor-diario:2.00}") BigDecimal valorDiarioMulta) {
        this.repo = repo;
        this.clientes = clientes;
        this.itens = itens;
        this.titulos = titulos;
        this.classes = classes;
        this.valorDiarioMulta = valorDiarioMulta;
    }

    public record Dados(
            Integer clienteId,
            Integer itemId,
            LocalDate prevista,
            BigDecimal valor,
            boolean paga) {

    }

    public record Resumo(
            Integer id,
            Integer clienteId,
            Integer itemId,
            String cliente,
            String item,
            String serie,
            LocalDate data,
            LocalDate prevista,
            LocalDate devolucao,
            BigDecimal valor,
            BigDecimal multa,
            boolean paga,
            String status,
            long diasAtraso,
            BigDecimal total) {

    }

    private LocalDate hoje() {
        return LocalDate.now(ZoneId.of("America/Sao_Paulo"));
    }

    private ResponseStatusException erro(HttpStatus status, String mensagem) {
        return new ResponseStatusException(status, mensagem);
    }

    private Locacao obter(Integer id) {
        return repo.findById(id)
                .orElseThrow(() -> erro(HttpStatus.NOT_FOUND, "Locação não encontrada."));
    }

    @Transactional(readOnly = true)
    public List<Resumo> listar() {
        return repo.findAllByOrderByIdDesc()
                .stream()
                .map(this::resumo)
                .toList();
    }

    @Transactional(readOnly = true)
    public Resumo buscar(Integer id) {
        return resumo(obter(id));
    }

    @Transactional(readOnly = true)
    public Resumo buscarVigente(String serie) {
        if (serie == null || serie.isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe o número de série.");
        }

        var vigentes = repo.findBySerieIgnoreCaseAndDevolucaoIsNull(serie.trim());

        if (vigentes.isEmpty()) {
            throw erro(HttpStatus.NOT_FOUND, "O item informado não possui uma locação vigente.");
        }

        if (vigentes.size() > 1) {
            throw erro(HttpStatus.CONFLICT, "Número de série ambíguo. Confira o cadastro dos itens.");
        }

        return resumo(vigentes.getFirst());
    }

    public Resumo salvar(Dados dados) {
        return gravar(new Locacao(), dados);
    }

    public Resumo atualizar(Integer id, Dados dados) {
        Locacao l = obter(id);

        if (l.getDevolucao() != null) {
            throw erro(HttpStatus.CONFLICT, "Uma locação devolvida não pode ser alterada.");
        }

        if (l.isPaga() && (!dados.paga()
                || !l.getClienteId().equals(dados.clienteId())
                || !l.getItemId().equals(dados.itemId())
                || (dados.valor() != null && l.getValor().compareTo(dados.valor()) != 0))) {
            throw erro(HttpStatus.CONFLICT, "Preserve o pagamento, o cliente, o item e o valor da locação já paga.");
        }

        return gravar(l, dados);
    }

    private Resumo gravar(Locacao l, Dados dados) {
        if (dados.clienteId() == null || dados.itemId() == null) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione um cliente e um item.");
        }

        Cliente cliente = clientes.findById(dados.clienteId())
                .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Cliente não encontrado."));
        if (!cliente.isAtivo()) {
            throw erro(HttpStatus.BAD_REQUEST, "Selecione um cliente ativo.");
        }

        if ("Dependente".equals(cliente.getTipo()) && (cliente.getSocioId() == null
                || clientes.findById(cliente.getSocioId()).filter(Cliente::isAtivo).isEmpty())) {
            throw erro(HttpStatus.BAD_REQUEST, "O sócio responsável deve estar ativo.");
        }

        Integer id = l.getId() == null ? 0 : l.getId();
        var atrasadas = repo.findAllByOrderByIdDesc()
                .stream()
                .filter(x -> x.getClienteId().equals(cliente.getId())
                        && !x.getId().equals(id)
                        && x.getDevolucao() == null
                        && x.getPrevista().isBefore(hoje()))
                .toList();
        if (!atrasadas.isEmpty()) {
            throw erro(
                    HttpStatus.CONFLICT,
                    "Cliente em débito. Locações em atraso: "
                    + atrasadas.stream()
                            .map(x -> "#" + x.getId() + " — " + x.getItem() + " (" + x.getPrevista() + ")")
                            .toList());
        }

        Item item = itens.findById(dados.itemId())
                .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Item não encontrado."));
        boolean mesmoItem = item.getSerial().equals(l.getItemId());
        if ((!mesmoItem && !"Disponível".equals(item.getStatus()))
                || repo.existsByItemIdAndDevolucaoIsNullAndIdNot(item.getSerial(), id)) {
            throw erro(HttpStatus.CONFLICT, "O item não está disponível para locação.");
        }

        if (item.getNumeroSerie() == null || item.getNumeroSerie().isBlank()) {
            throw erro(HttpStatus.BAD_REQUEST, "O item precisa ter número de série.");
        }

        boolean novo = l.getId() == null;
        if (novo) {
            l.setData(hoje());
            l.setMulta(BigDecimal.ZERO);
        }

        BigDecimal valor = dados.valor();
        LocalDate prevista = novo || !mesmoItem ? null : l.getPrevista();
        if (valor == null || prevista == null) {
            var titulo = titulos.findByNomeIgnoreCase(item.getTitulo())
                    .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Título do item não encontrado."));
            var classe = classes.findByNomeIgnoreCase(titulo.getClasse())
                    .orElseThrow(() -> erro(HttpStatus.BAD_REQUEST, "Classe do título não encontrada."));
            if (classe.getValor() == null || classe.getPrazo() == null || classe.getPrazo() <= 0) {
                throw erro(HttpStatus.BAD_REQUEST, "Configure valor e prazo válidos na classe.");
            }

            if (valor == null) {
                valor = novo || !mesmoItem ? classe.getValor() : l.getValor();
            }

            if (prevista == null) {
                prevista = novo || !mesmoItem ? l.getData().plusDays(classe.getPrazo()) : l.getPrevista();
            }
        }

        if (dados.prevista() != null && !dados.prevista().equals(prevista)) {
            throw erro(
                    HttpStatus.BAD_REQUEST,
                    "A data prevista de devolução é definida pelo prazo da classe e não pode ser alterada.");
        }

        if (valor.signum() <= 0 || valor.scale() > 2) {
            throw erro(HttpStatus.BAD_REQUEST, "Informe um valor positivo com até duas casas decimais.");
        }

        if (!prevista.isAfter(l.getData())) {
            throw erro(HttpStatus.BAD_REQUEST, "A devolução prevista deve ser posterior à data da locação.");
        }

        if (!novo && !mesmoItem) {
            liberar(l.getItemId());
        }

        l.setClienteId(cliente.getId());
        l.setCliente(cliente.getNome());
        l.setItemId(item.getSerial());
        l.setItem(item.getTitulo());
        l.setSerie(item.getNumeroSerie());
        l.setValor(valor);
        l.setPrevista(prevista);
        l.setPaga(dados.paga());

        item.setStatus("Locado");
        itens.save(item);

        return resumo(repo.saveAndFlush(l));
    }

    public void cancelar(Integer id) {
        Locacao l = obter(id);

        if (l.isPaga() || l.getDevolucao() != null) {
            throw erro(HttpStatus.CONFLICT, "Apenas locações não pagas e vigentes podem ser canceladas.");
        }

        liberar(l.getItemId());
        repo.delete(l);
    }

    public Resumo devolver(Integer id) {
        Locacao l = obter(id);

        if (l.getDevolucao() != null) {
            throw erro(HttpStatus.CONFLICT, "Esta locação já foi devolvida.");
        }

        if (hoje().isBefore(l.getData())) {
            throw erro(HttpStatus.BAD_REQUEST, "A devolução não pode ser anterior à locação.");
        }

        l.setMulta(resumo(l).multa());
        l.setDevolucao(hoje());
        l.setPaga(true);
        liberar(l.getItemId());

        return resumo(repo.saveAndFlush(l));
    }

    private void liberar(Integer id) {
        Item item = itens.findById(id)
                .orElseThrow(() -> erro(HttpStatus.CONFLICT, "Item da locação não encontrado."));
        item.setStatus("Disponível");
        itens.save(item);
    }

    private Resumo resumo(Locacao l) {
        long atraso = Math.max(
                0,
                ChronoUnit.DAYS.between(
                        l.getPrevista(),
                        l.getDevolucao() == null ? hoje() : l.getDevolucao()));

        BigDecimal multa = l.getDevolucao() == null
                ? valorDiarioMulta.multiply(BigDecimal.valueOf(atraso)).setScale(2, RoundingMode.HALF_UP)
                : l.getMulta();

        String status = l.getDevolucao() != null
                ? "Devolvida"
                : atraso > 0 ? "Atrasada" : "Em aberto";

        BigDecimal total = l.getDevolucao() != null
                ? BigDecimal.ZERO
                : (l.isPaga() ? BigDecimal.ZERO : l.getValor()).add(multa);

        return new Resumo(
                l.getId(),
                l.getClienteId(),
                l.getItemId(),
                l.getCliente(),
                l.getItem(),
                l.getSerie(),
                l.getData(),
                l.getPrevista(),
                l.getDevolucao(),
                l.getValor(),
                multa,
                l.isPaga(),
                status,
                atraso,
                total);
    }
}
