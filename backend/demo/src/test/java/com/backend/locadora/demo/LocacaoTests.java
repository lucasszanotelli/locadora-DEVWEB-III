package com.backend.locadora.demo;

import com.backend.DemoApplication;
import com.backend.model.domain.*;
import com.backend.model.repository.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:sqlite::memory:")
class LocacaoTests {
    @LocalServerPort int port;
    @Autowired RepoCliente clientes;
    @Autowired RepoItem itens;
    @Autowired RepoTitulo titulos;
    @Autowired RepoClasse classes;
    @Autowired RepoLocacao locacoes;
    private final HttpClient http = HttpClient.newHttpClient();
    private final ObjectMapper mapper = new ObjectMapper();
    private Cliente cliente;
    private Item item;
    private LocalDate hoje() { return LocalDate.now(ZoneId.of("America/Sao_Paulo")); }

    @BeforeEach void preparar() {
        String nome = UUID.randomUUID().toString();
        Classe classe = new Classe(); classe.setNome(nome); classe.setPrazo(3); classe.setValor(new BigDecimal("12.50")); classes.save(classe);
        Titulo titulo = new Titulo(); titulo.setNome(nome); titulo.setClasse(nome); titulos.save(titulo);
        cliente = new Cliente(); cliente.setInscricao(nome); cliente.setNome("Cliente teste"); cliente.setTipo("Sócio");
        cliente.setNascimento("2000-01-01"); cliente.setSexo("Outro"); clientes.save(cliente);
        item = novoItem(nome);
    }
    private Item novoItem(String titulo) {
        Item i = new Item(); i.setTitulo(titulo); i.setNumeroSerie(UUID.randomUUID().toString()); i.setTipoItem("DVD");
        i.setDataAquisicao("2020-01-01"); return itens.save(i);
    }
    private HttpResponse<String> request(String metodo, String path, String body) throws Exception {
        return http.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json").method(metodo, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }
    private String dados(Item i, boolean paga, String extras) {
        return "{\"clienteId\":" + cliente.getId() + ",\"itemId\":" + i.getSerial() + ",\"paga\":" + paga + extras + "}";
    }
    private JsonNode criar(boolean paga) throws Exception {
        var r = request("POST", "/locacoes", dados(item, paga, ""));
        assertEquals(201, r.statusCode(), r.body()); return mapper.readTree(r.body());
    }

    @Test void calculaClassePersisteEDevolveSemAtraso() throws Exception {
        var l = criar(false); int id = l.path("id").asInt();
        assertEquals(12.5, l.path("valor").asDouble());
        assertEquals(hoje().toString(), l.path("data").asText());
        assertEquals(hoje().plusDays(3).toString(), l.path("prevista").asText());
        assertEquals("Locado", itens.findById(item.getSerial()).orElseThrow().getStatus());
        assertEquals(409, request("POST", "/locacoes", dados(item, false, "")).statusCode());
        assertEquals(200, request("GET", "/locacoes/vigente?serie=" + item.getNumeroSerie().toUpperCase(), null).statusCode());
        assertEquals(409, request("DELETE", "/itens/" + item.getSerial(), null).statusCode());
        assertEquals(400, request("DELETE", "/clientes/" + cliente.getId(), null).statusCode());
        var retorno = request("POST", "/locacoes/" + id + "/devolucao", "{}");
        assertEquals(200, retorno.statusCode(), retorno.body());
        var devolvida = mapper.readTree(retorno.body());
        assertEquals("Devolvida", devolvida.path("status").asText()); assertTrue(devolvida.path("paga").asBoolean());
        assertEquals(hoje().toString(), devolvida.path("devolucao").asText()); assertEquals(0, devolvida.path("multa").asDouble());
        assertEquals("Disponível", itens.findById(item.getSerial()).orElseThrow().getStatus());
        assertEquals(409, request("POST", "/locacoes/" + id + "/devolucao", "{}").statusCode());
        assertEquals(404, request("GET", "/locacoes/vigente?serie=" + item.getNumeroSerie(), null).statusCode());
        assertEquals(409, request("PUT", "/locacoes/" + id, dados(item, true, "")).statusCode());
    }

    @Test void multaFixaBloqueioPorAtrasoESomaSomenteValorPendente() throws Exception {
        for (boolean paga : new boolean[] { false, true }) {
            var criado = criar(paga); int id = criado.path("id").asInt();
            Locacao l = locacoes.findById(id).orElseThrow(); l.setData(hoje().minusDays(5)); l.setPrevista(hoje().minusDays(2)); locacoes.save(l);
            var resumo = mapper.readTree(request("GET", "/locacoes/" + id, null).body());
            assertEquals(4, resumo.path("multa").asDouble()); assertEquals(2, resumo.path("diasAtraso").asInt());
            assertEquals(paga ? 4 : 16.5, resumo.path("total").asDouble());
            Item outro = novoItem(item.getTitulo());
            var bloqueada = request("POST", "/locacoes", dados(outro, false, ""));
            assertEquals(409, bloqueada.statusCode()); assertTrue(bloqueada.body().contains("atraso"));
            assertEquals(200, request("POST", "/locacoes/" + id + "/devolucao", "{}").statusCode());
            assertEquals(0, new BigDecimal("4.00").compareTo(locacoes.findById(id).orElseThrow().getMulta()));
        }
    }

    @Test void alteraTrocaItemECancelaLiberandoAcervo() throws Exception {
        var l = criar(false); int id = l.path("id").asInt(); Item outro = novoItem(item.getTitulo());
        String ajustes = ",\"valor\":9.75,\"prevista\":\"" + hoje().plusDays(3) + "\"";
        var alterada = request("PUT", "/locacoes/" + id, dados(outro, false, ajustes));
        assertEquals(200, alterada.statusCode(), alterada.body());
        assertEquals(9.75, mapper.readTree(alterada.body()).path("valor").asDouble());
        assertEquals("Disponível", itens.findById(item.getSerial()).orElseThrow().getStatus());
        assertEquals("Locado", itens.findById(outro.getSerial()).orElseThrow().getStatus());
        assertEquals(204, request("DELETE", "/locacoes/" + id, null).statusCode());
        assertFalse(locacoes.existsById(id)); assertEquals("Disponível", itens.findById(outro.getSerial()).orElseThrow().getStatus());
        int paga = criar(true).path("id").asInt();
        assertEquals(409, request("DELETE", "/locacoes/" + paga, null).statusCode());
        assertEquals(409, request("PUT", "/locacoes/" + paga, dados(item, false, "")).statusCode());
    }

    @Test void impedeAlterarPrazoDaClasseEPreservaPrazoContratado() throws Exception {
        String prazoLivre = ",\"valor\":9.75,\"prevista\":\"" + hoje().plusDays(7) + "\"";
        assertEquals(400, request("POST", "/locacoes", dados(item, false, prazoLivre)).statusCode());
        assertEquals("Disponível", itens.findById(item.getSerial()).orElseThrow().getStatus());
        var criada = criar(false); int id = criada.path("id").asInt();
        assertEquals(400, request("PUT", "/locacoes/" + id, dados(item, false, prazoLivre)).statusCode());
        assertEquals(hoje().plusDays(3), locacoes.findById(id).orElseThrow().getPrevista());
        Classe classe = classes.findByNomeIgnoreCase(item.getTitulo()).orElseThrow();
        classe.setPrazo(10); classes.save(classe);
        var editada = request("PUT", "/locacoes/" + id, dados(item, false, ""));
        assertEquals(200, editada.statusCode(), editada.body());
        assertEquals(hoje().plusDays(3).toString(), mapper.readTree(editada.body()).path("prevista").asText());
        Item outro = novoItem(item.getTitulo());
        var trocada = request("PUT", "/locacoes/" + id, dados(outro, false, ""));
        assertEquals(200, trocada.statusCode(), trocada.body());
        assertEquals(hoje().plusDays(10).toString(), mapper.readTree(trocada.body()).path("prevista").asText());
    }

    @Test void devolucaoNoPrazoNaoCobraMulta() throws Exception {
        int id = criar(false).path("id").asInt();
        Locacao l = locacoes.findById(id).orElseThrow();
        l.setData(hoje().minusDays(3)); l.setPrevista(hoje()); locacoes.save(l);
        var resposta = request("POST", "/locacoes/" + id + "/devolucao", "{}");
        assertEquals(200, resposta.statusCode(), resposta.body());
        assertEquals(0, mapper.readTree(resposta.body()).path("multa").asDouble());
    }

    @Test void rejeitaDadosInvalidosEClienteInativo() throws Exception {
        assertEquals(400, request("POST", "/locacoes", "{}").statusCode());
        assertEquals(400, request("POST", "/locacoes", dados(item, false, ",\"valor\":-1")).statusCode());
        assertEquals(400, request("POST", "/locacoes", dados(item, false, ",\"prevista\":\"" + hoje() + "\"")).statusCode());
        assertEquals("Disponível", itens.findById(item.getSerial()).orElseThrow().getStatus());
        cliente.setAtivo(false); clientes.save(cliente);
        assertEquals(400, request("POST", "/locacoes", dados(item, false, "")).statusCode());
        assertEquals(404, request("GET", "/locacoes/999999", null).statusCode());
    }
}
