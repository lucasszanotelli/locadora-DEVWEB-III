# Cadastro de atores

A página `/atores` do Angular usa `AtorService` para acessar o backend. O fluxo é:

`Tela Angular → AtorService (HTTP) → ControladorAtor → ServAtor → RepoAtor → SQLite`

O Angular representa a View. O controller expõe as rotas HTTP e o Model reúne a entidade, as regras do serviço e o repositório. Apenas atores foram conectados ao backend.

## Executar

Em um terminal, a partir da raiz do projeto:

```bash
cd backend/demo
bash mvnw spring-boot:run -Djava.version=21
```

O `pom.xml` define Java 25. O comando acima permite executar com o Java 21 disponível neste ambiente; com JDK 25, o parâmetro pode ser omitido.

Em outro terminal:

```bash
cd frontend-angular
npm ci
npm start
```

Acesse `http://localhost:4200/atores`. O proxy do servidor Angular encaminha `/api/atores` para `http://localhost:8080/atores`, evitando a necessidade de CORS no desenvolvimento. Em produção, configure o servidor para encaminhar essa mesma rota à API.

Os registros são persistidos no arquivo `backend/demo/locadora.db` quando o backend é iniciado nessa pasta. A lista usa os registros do banco; os atores simulados foram removidos.

## API

| Método | Rota | Operação |
| --- | --- | --- |
| GET | `/atores` | Listar |
| GET | `/atores/{id}` | Consultar |
| POST | `/atores` | Inserir, retorna 201 |
| PUT | `/atores/{id}` | Editar, retorna 200 |
| DELETE | `/atores/{id}` | Excluir, retorna 204 |

Corpo para inserção e edição:

```json
{
  "nome": "Fernanda Montenegro",
  "nacionalidade": "Brasileira",
  "dataNascimento": "1929-10-16"
}
```

Nome é obrigatório. Nacionalidade e nascimento são opcionais. Quando informada, a data deve seguir `AAAA-MM-DD`. O ID é gerado no banco. Registros inexistentes retornam 404 e dados inválidos retornam 400.

## Verificação

```bash
cd backend/demo
bash mvnw test -Djava.version=21
```

O teste usa SQLite em memória e verifica inserção, listagem, consulta, edição, exclusão, validação e respostas 404, sem alterar o banco da aplicação.

```bash
cd frontend-angular
npm run build
```
