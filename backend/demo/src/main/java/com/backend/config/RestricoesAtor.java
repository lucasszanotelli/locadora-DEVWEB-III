package com.backend.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/** Protege também tabelas SQLite antigas, que ddl-auto=update não torna NOT NULL. */
@Component
public class RestricoesAtor implements ApplicationRunner {
    private final JdbcTemplate jdbc;
    public RestricoesAtor(JdbcTemplate jdbc) { this.jdbc = jdbc; }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        for (String operacao : new String[] { "INSERT", "UPDATE" }) {
            jdbc.execute("""
                CREATE TRIGGER IF NOT EXISTS atores_campos_obrigatorios_%s
                BEFORE %s ON atores
                FOR EACH ROW
                WHEN NEW.nome IS NULL OR trim(NEW.nome) = ''
                  OR NEW.nacionalidade IS NULL OR trim(NEW.nacionalidade) = ''
                  OR NEW.data_nascimento IS NULL OR trim(NEW.data_nascimento) = ''
                BEGIN
                  SELECT RAISE(ABORT, 'Todos os campos do ator sao obrigatorios');
                END
                """.formatted(operacao.toLowerCase(java.util.Locale.ROOT), operacao));
        }
    }
}
