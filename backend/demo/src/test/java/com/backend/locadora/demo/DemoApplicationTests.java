package com.backend.locadora.demo;

import com.backend.DemoApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;
import org.springframework.dao.DataAccessException;
import com.backend.config.RestricoesAtor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = DemoApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "spring.datasource.url=jdbc:sqlite::memory:")
class DemoApplicationTests {
    @LocalServerPort int port;
    private final HttpClient client = HttpClient.newHttpClient();

    private HttpResponse<String> request(String method, String path, String body) throws Exception {
        return client.send(HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
            .header("Content-Type", "application/json")
            .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body))
            .build(), HttpResponse.BodyHandlers.ofString());
    }

    @Test
    void disponibilizaDocumentacaoESwaggerUi() throws Exception {
        var documentacao = request("GET", "/v3/api-docs", null);
        assertEquals(200, documentacao.statusCode());
        var json = new tools.jackson.databind.ObjectMapper().readTree(documentacao.body());
        assertEquals("API da Locadora", json.path("info").path("title").asText());
        for (String recurso : new String[] { "/atores", "/diretores", "/classes", "/titulos", "/itens" }) {
            var rota = json.path("paths").path(recurso);
            assertTrue(rota.has("get"), recurso);
            assertTrue(rota.has("post"), recurso);
        }
        var pagina = request("GET", "/swagger-ui/index.html", null);
        assertEquals(200, pagina.statusCode());
        assertTrue(pagina.body().contains("swagger-ui-bundle.js"));
        var configuracao = request("GET", "/v3/api-docs/swagger-config", null);
        assertEquals(200, configuracao.statusCode());
        assertTrue(configuracao.body().contains("/v3/api-docs"));
    }

    @Test
    void validaCamposDoAtorNaInsercaoEEdicao() throws Exception {
        String valido = "{\"nome\":\"Ator validacao\",\"nacionalidade\":\" Brasil \",\"dataNascimento\":\"2000-02-29\"}";
        var criado = request("POST", "/atores", valido);
        assertEquals(201, criado.statusCode());
        String local = criado.headers().firstValue("Location").orElseThrow();
        for (String campo : new String[] { "nome", "nacionalidade", "dataNascimento" }) {
            String original = campo.equals("nome") ? "\"Ator validacao\"" : campo.equals("nacionalidade") ? "\" Brasil \"" : "\"2000-02-29\"";
            for (String vazio : new String[] { "null", "\"\"", "\"   \"" }) {
                String invalido = valido.replace("\"" + campo + "\":" + original, "\"" + campo + "\":" + vazio);
                assertEquals(400, request("POST", "/atores", invalido).statusCode(), campo + "=" + vazio);
                assertEquals(400, request("PUT", local, invalido).statusCode(), campo + "=" + vazio);
            }
            String ausente = valido.replace("\"" + campo + "\":" + original, "").replace("{,", "{").replace(",}", "}").replace(",,", ",");
            assertEquals(400, request("POST", "/atores", ausente).statusCode(), campo + " ausente");
            assertEquals(400, request("PUT", local, ausente).statusCode(), campo + " ausente");
        }
        for (String data : new String[] { "2023-02-29", "2000-02-30", "2000-13-01", "0000-01-01", "29/02/2000", "2000-2-29", LocalDate.now().plusDays(1).toString() }) {
            String invalido = valido.replace("2000-02-29", data);
            assertEquals(400, request("POST", "/atores", invalido).statusCode(), data);
            assertEquals(400, request("PUT", local, invalido).statusCode(), data);
        }
        assertTrue(request("GET", local, null).body().contains("2000-02-29"));
        assertTrue(request("GET", local, null).body().contains("\"nacionalidade\":\"Brasil\""));
        assertEquals(200, request("PUT", local, valido.replace("2000-02-29", LocalDate.now().toString())).statusCode());
        assertEquals(204, request("DELETE", local, null).statusCode());
    }

    @Test
    void protegeBancoAntigoContraCamposNulosEVazios() {
        // Simula a tabela anterior, sem NOT NULL, em um banco separado.
        var fonte = new SingleConnectionDataSource("jdbc:sqlite::memory:", true);
        try {
            var jdbc = new JdbcTemplate(fonte);
            jdbc.execute("CREATE TABLE atores (id INTEGER PRIMARY KEY, nome TEXT, nacionalidade TEXT, data_nascimento TEXT)");
            jdbc.update("INSERT INTO atores VALUES (1, 'Ator', 'Brasil', '2000-02-29')");
            var restricoes = new RestricoesAtor(jdbc);
            restricoes.run(null);
            restricoes.run(null); // Reiniciar a aplicação não duplica as restrições.
            for (String coluna : new String[] { "nome", "nacionalidade", "data_nascimento" }) {
                for (String valor : new String[] { null, "", "   " }) {
                    assertThrows(DataAccessException.class, () -> jdbc.update("UPDATE atores SET " + coluna + " = ? WHERE id = 1", valor));
                    String nome = coluna.equals("nome") ? valor : "Ator";
                    String nacionalidade = coluna.equals("nacionalidade") ? valor : "Brasil";
                    String nascimento = coluna.equals("data_nascimento") ? valor : "2000-02-29";
                    assertThrows(DataAccessException.class, () -> jdbc.update("INSERT INTO atores (nome, nacionalidade, data_nascimento) VALUES (?, ?, ?)", nome, nacionalidade, nascimento));
                }
            }
            assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM atores", Integer.class));
            jdbc.update("UPDATE atores SET nome = 'Nome editado' WHERE id = 1");
        } finally {
            fonte.destroy();
        }
    }

    @Test
    void crudDeClasseComVinculos() throws Exception {
        var criado = request("POST", "/classes", "{\"id\":999,\"nome\":\" Lancamento \",\"valor\":12.50,\"prazo\":2}");
        assertEquals(201, criado.statusCode());
        String local = criado.headers().firstValue("Location").orElseThrow();
        assertNotEquals("/classes/999", local);
        assertTrue(request("GET", "/classes", null).body().contains("Lancamento"));
        assertEquals(200, request("GET", local, null).statusCode());
        assertEquals(409, request("POST", "/classes", "{\"nome\":\"lancamento\",\"valor\":10,\"prazo\":1}").statusCode());
        assertEquals(400, request("POST", "/classes", "{\"nome\":\" \",\"valor\":10,\"prazo\":1}").statusCode());
        assertEquals(400, request("POST", "/classes", "{\"nome\":\"Invalida\",\"valor\":0,\"prazo\":1}").statusCode());
        assertEquals(400, request("POST", "/classes", "{\"nome\":\"Invalida\",\"valor\":10,\"prazo\":0}").statusCode());
        assertEquals(400, request("POST", "/classes", "{\"nome\":\"Invalida\"}").statusCode());

        var titulo = request("POST", "/titulos", "{\"nome\":\"Filme de teste\",\"categoria\":\"Drama\",\"classe\":\"Lancamento\",\"ano\":2025}");
        assertEquals(201, titulo.statusCode());
        String tituloLocal = titulo.headers().firstValue("Location").orElseThrow();
        assertEquals(409, request("DELETE", local, null).statusCode());
        var editado = request("PUT", local, "{\"nome\":\"Premium\",\"valor\":15.75,\"prazo\":3}");
        assertEquals(200, editado.statusCode());
        assertTrue(editado.body().contains("Premium"));
        assertTrue(request("GET", local, null).body().contains("15.75"));
        assertTrue(request("GET", tituloLocal, null).body().contains("Premium"));
        assertEquals(409, request("DELETE", local, null).statusCode());
        assertEquals(400, request("PUT", local, "{\"nome\":\"Premium\",\"valor\":-1,\"prazo\":3}").statusCode());
        assertEquals(204, request("DELETE", tituloLocal, null).statusCode());
        assertEquals(204, request("DELETE", local, null).statusCode());
        assertEquals(404, request("GET", local, null).statusCode());
        assertEquals(404, request("PUT", local, "{\"nome\":\"Premium\",\"valor\":10,\"prazo\":1}").statusCode());
        assertEquals(404, request("DELETE", local, null).statusCode());
    }

    @Test
    void crudDeAtor() throws Exception {
        var criado = request("POST", "/atores", "{\"id\":999,\"nome\":\" Ator teste \",\"nacionalidade\":\"Brasil\",\"dataNascimento\":\"1990-01-15\"}");
        assertEquals(201, criado.statusCode());
        String local = criado.headers().firstValue("Location").orElseThrow();
        assertNotEquals("/atores/999", local);
        assertTrue(request("GET", "/atores", null).body().contains("Ator teste"));
        assertEquals(200, request("GET", local, null).statusCode());
        var editado = request("PUT", local, "{\"nome\":\"Ator editado\",\"nacionalidade\":\"Portugal\",\"dataNascimento\":\"1991-02-16\"}");
        assertEquals(200, editado.statusCode());
        assertTrue(editado.body().contains("Ator editado"));
        assertTrue(request("GET", local, null).body().contains("Portugal"));
        assertEquals(400, request("POST", "/atores", "{\"nome\":\" \"}").statusCode());
        assertEquals(400, request("PUT", local, "{\"nome\":\"Teste\",\"dataNascimento\":\"invalida\"}").statusCode());
        assertEquals(204, request("DELETE", local, null).statusCode());
        assertEquals(404, request("GET", local, null).statusCode());
        assertEquals(404, request("PUT", local, "{\"nome\":\"Teste\"}").statusCode());
        assertEquals(404, request("DELETE", local, null).statusCode());
    }
}
