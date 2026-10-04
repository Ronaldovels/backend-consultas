# Backend - Sistema de Consultas

API REST em Spring Boot para marcação de consultas médicas.
Projeto acadêmico - FIAP Engenharia da Computação, turma 3ECR.

Ronaldo Veloso Filho - RM 556445

---

## Stack

| Item | Versão |
|---|---|
| Java | 17 |
| Spring Boot | 4.0.3 |
| Banco | H2 (arquivo em `./data/consultas`) |
| Build | Maven Wrapper (`./mvnw`) |

---

## Rodando localmente

```bash
./mvnw spring-boot:run
```

Sobe em `http://localhost:8080`. O `DataLoader` semeia os dados de exemplo
automaticamente no primeiro start.

---

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| GET | `/health` | `{"status":"UP"}` - usado pelo app para detectar backend offline |
| GET | `/especialidades` | lista de especialidades |
| GET | `/medicos` | lista de médicos |
| GET | `/medicos/crm/{crm}` | login do médico |
| GET | `/pacientes` | lista de pacientes |
| GET | `/pacientes/cpf/{cpf}` | login do paciente |
| GET | `/consultas` | lista de consultas |
| GET | `/consultas/medico/{id}` | consultas de um médico |
| GET | `/consultas/paciente/{id}` | consultas de um paciente |
| POST | `/consultas` | agendar consulta |
| PUT | `/consultas/{id}` | atualizar consulta |
| DELETE | `/consultas/{id}` | cancelar/remover consulta |

`especialidades`, `medicos` e `pacientes` também aceitam POST, PUT e DELETE.

---

## Deploy

### Backend

Hospedado no Render: `https://SEU-SERVICO.onrender.com` *(substitua pela sua URL)*

- `GET /health` → `{"status":"UP"}`
- `GET /medicos` → lista de médicos
- `GET /pacientes` → lista de pacientes

> O serviço dorme após 15 min de inatividade (free tier).
> A primeira requisição pode levar até 60 segundos (cold start).

**Configuração do serviço no Render:**

| Campo | Valor |
|---|---|
| Runtime | Docker |
| Branch | `main` |
| Region | Ohio (US East) |
| Instance Type | Free |
| Variáveis de ambiente | nenhuma necessária |

O Render detecta o `Dockerfile` na raiz do projeto. O build é feito em dois
estágios: o primeiro compila com JDK 17 + Maven, o segundo mantém apenas o JRE
e o JAR, reduzindo a imagem final de ~500 MB para ~200 MB.

### Persistência na nuvem

O sistema de arquivos do free tier do Render é **efêmero**: o arquivo do H2 é
descartado a cada reinicialização do serviço. Por isso o `DataLoader` semeia
tudo - especialidades, médicos, pacientes e consultas - sempre que o banco
estiver vazio. Toda vez que o backend acorda, os dados de exemplo voltam.

Cada seção tem a própria guarda `if (count() == 0)`, então reiniciar o
servidor localmente não duplica registros.

> Em produção real o H2 seria substituído por um banco externo
> (PostgreSQL, MySQL) com persistência garantida.

---

## Credenciais de teste

| Perfil | Campo | Valor |
|---|---|---|
| Médico | CRM | `789456` (Dr. Roberto Silva - Cardiologia) |
| Médico | CRM | `123789` (Dra. Ana Ferreira - Dermatologia) |
| Médico | CRM | `456123` (Dr. Carlos Mendes - Ortopedia) |
| Paciente | CPF | `12345678901` (Maria Silva) |
| Paciente | CPF | `98765432100` (João Santos) |
| Paciente | CPF | `11122233344` (Ana Costa) |

---

## Observações

- O console do H2 (`/h2-console`) está habilitado com usuário `sa` e senha
  vazia. É prático para a demonstração em aula, mas fica acessível
  publicamente junto com a URL do Render. Para desligá-lo basta trocar
  `spring.h2.console.enabled` para `false` em `application.properties`.
- O banco local (`data/`) não é versionado: o `DataLoader` recria os dados.
