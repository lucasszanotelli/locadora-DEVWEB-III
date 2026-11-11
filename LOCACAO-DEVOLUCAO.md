# Locação e devolução

As telas **Locações** e **Devoluções** usam a API Spring Boot e persistem as operações no SQLite. O backend cria a tabela `locacoes` e o índice de locações vigentes ao iniciar.

## Executar

Em terminais separados:

```sh
cd backend/demo
./mvnw spring-boot:run
```

```sh
cd frontend-angular
npm start
```

Acesse `http://localhost:4200`. Cadastre classe (valor e prazo), título associado à classe, item disponível e cliente ativo antes de locar.

## Operações

- Criar, consultar e alterar locações. O valor é sugerido pela classe e pode ser ajustado. A devolução prevista é obrigatoriamente a data da locação mais o prazo em dias da classe, sem ajuste manual. O prazo registrado é preservado quando a mesma locação é editada, mesmo que a classe tenha sido alterada posteriormente. Ao trocar o item, o prazo é recalculado pela classe do novo item. A data da locação é a data corrente em America/Sao_Paulo.
- Clientes com locações vigentes atrasadas ficam impedidos de realizar novas locações; a mensagem identifica as locações em atraso. Uma locação ainda dentro do prazo pode ser paga na devolução.
- Cancelar uma locação vigente não paga exclui o registro e libera o item.
- A aba Devoluções exibe todas as locações em aberto (incluindo atrasadas), com ação para selecionar e devolver. A busca centralizada por número de série prioriza a locação vigente; se o item já foi devolvido e não possui locação vigente, exibe a última locação com status Devolvida, data efetiva e sem permitir nova devolução.
- Multa fixa de **R$ 2,00 por dia de atraso**, inclusive para locações pagas antecipadamente. A configuração está em `locadora.multa.valor-diario`, no `application.properties`.
- O total da devolução soma a multa ao valor da locação quando ainda não estiver pago. Confirmar registra o pagamento, a multa e a data corrente, liberando o item na mesma transação.
- Locações devolvidas ficam no histórico. Clientes, dependentes e itens com histórico não podem ser excluídos. O pagamento de uma locação paga não pode ser desfeito pela edição.

## API

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/locacoes` | Listar histórico e valores atuais |
| GET | `/locacoes/{id}` | Consultar locação |
| GET | `/locacoes/vigente?serie=...` | Buscar locação vigente pela série |
| POST | `/locacoes` | Criar locação |
| PUT | `/locacoes/{id}` | Alterar locação vigente |
| DELETE | `/locacoes/{id}` | Cancelar locação não paga |
| POST | `/locacoes/{id}/devolucao` | Confirmar devolução e pagamento |

Criação e alteração recebem `clienteId`, `itemId`, `paga` e, opcionalmente, `valor`. O prazo é calculado pelo servidor a partir da classe. O campo `prevista` é aceito apenas se coincidir com a data definida pelo servidor; uma tentativa de alterar o prazo retorna HTTP 400. Datas, nomes, status e multa são definidos pelo servidor.

## Validação

- Backend: `cd backend/demo && ./mvnw test` (banco SQLite em memória).
- Frontend: `cd frontend-angular && npm run build`.
