package com.backend.locadora.demo;

import com.backend.DemoApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
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
