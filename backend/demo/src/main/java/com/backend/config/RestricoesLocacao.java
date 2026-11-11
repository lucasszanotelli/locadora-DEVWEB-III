package com.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class RestricoesLocacao implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public RestricoesLocacao(JdbcTemplate jdbc) { this.jdbc = jdbc; }
    @Override public void run(ApplicationArguments args) {
        jdbc.execute("CREATE UNIQUE INDEX IF NOT EXISTS locacoes_item_vigente ON locacoes(item_id) WHERE devolucao IS NULL");
    }
}
