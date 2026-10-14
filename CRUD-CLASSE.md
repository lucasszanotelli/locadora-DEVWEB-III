# Cadastro de classes

A página `/classes` permite listar, buscar por nome, cadastrar, editar e excluir classes. Os campos são nome, valor da locação e prazo de devolução em dias.

Fluxo: `Classes → ClasseService (HTTP) → ControladorClasse → ServClasse → RepoClasse → SQLite`.

Os registros ficam em `backend/demo/data/locadora.db`, conforme `application.properties`, e continuam disponíveis após recarregar a página. O combobox de títulos consulta as classes no backend.

## Executar no PowerShell

Na raiz do projeto, inicie o backend:

```powershell
cd backend\demo
.\mvnw.cmd spring-boot:run
```

Em outro terminal, a partir da raiz:

```powershell
cd frontend-angular
npm start
```

Acesse `http://localhost:4200/classes`. O proxy encaminha `/api/classes` do Angular para `/classes` no backend.

## API

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/classes` | Listar |
| GET | `/classes/{id}` | Consultar |
| POST | `/classes` | Inserir (201) |
| PUT | `/classes/{id}` | Editar (200) |
| DELETE | `/classes/{id}` | Excluir (204) |

Corpo de inserção e edição:

```json
{ "nome": "Lançamento", "valor": 12.50, "prazo": 2 }
```

Nome, valor positivo e prazo de pelo menos um dia são obrigatórios. Nomes duplicados, desconsiderando maiúsculas e minúsculas, retornam 409. Registros inexistentes retornam 404; dados inválidos retornam 400.

Uma classe associada a títulos não pode ser excluída (409). Como os títulos armazenam o nome da classe, a edição do nome atualiza os títulos associados na mesma transação.

## Verificação

Em `backend/demo`, execute `.\mvnw.cmd test`. Os testes usam SQLite em memória, sem alterar o banco da aplicação, e verificam CRUD, validações, duplicidade, alteração de nome com títulos associados e bloqueio de exclusão.

Em `frontend-angular`, execute `npm run build`.
