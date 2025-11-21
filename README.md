# Agendador de Consultas (JavaFX + SQLite)

Aplicação JavaFX para cadastro de pacientes e médicos, agendamento de consultas com detecção de conflitos, lista de espera, edição, cancelamento e tema claro/escuro. Persistência via SQLite (arquivo `clinic.db`).

## Requisitos
- JDK 17 instalado (verifique com `java -version`)
- Maven 3.8+ (verifique com `mvn -v`)
- Internet (primeiro build baixa dependências)

## Tecnologias
- JavaFX (controles + FXML)
- SQLite (sqlite-jdbc)
- Maven (build e execução)

## Estrutura de Pastas
```
Project/
  pom.xml
  clinic.db                # Gerado após primeira execução
  src/
    main/
      java/br/usuario/clinica/...
      resources/br/usuario/clinica/
        hello-view.fxml
        styles.css
        styles-dark.css
```

## Como Executar (Linha de Comando)
```powershell
# Limpar e compilar
mvn clean compile

# Rodar aplicação JavaFX
mvn javafx:run
```
A janela abrirá com os botões de ações. O arquivo `clinic.db` será criado na raiz.

## Funcionalidades
- Cadastrar / editar paciente (nome, telefone)
- Cadastrar / editar médico (nome, especialidade)
- Agendar consulta (data e hora, seleção automática por especialidade se existir; adiciona em espera se conflito)
- Editar consulta existente
- Listar consultas
- Cancelar consulta por ID, paciente ou médico
- Ver lista de espera (médico específico e por especialidade futura - especialidade ainda em memória)
- Alternar tema claro/escuro (botão "Alternar Tema")

## Regras de Agendamento
- Conflito se paciente ou médico já ocupado no intervalo.
- Horários consecutivos permitidos (fim = início de outra).
- Se não houver médico da especialidade, paciente entra na fila de especialidade (em memória). Quando cadastra um médico dessa especialidade, tenta-se realocar.

## Persistência
Tabelas criadas automaticamente:
```
patients(id INTEGER PK, name TEXT, phone TEXT)
doctors(id INTEGER PK, name TEXT, specialty TEXT)
appointments(id INTEGER PK, patient_id INT FK, doctor_id INT FK, start TEXT, end TEXT)
```
Arquivo gerado: `clinic.db`. Pode inspeccionar com `sqlite3 clinic.db` ou ferramentas GUI.

## Importar no IntelliJ IDEA
1. File > New > Project from Existing Sources > selecione `pom.xml`.
2. Confirme import Maven.
3. Project SDK: JDK 17.
4. Rodar: janela Maven > `Plugins` > `javafx` > `javafx:run` ou Run Config apontando para `br.usuario.clinica.Launcher`.

## Scripts Úteis
```powershell
# Mostrar versão Java e Maven
java -version
mvn -v

# Limpar banco (apagar arquivo)
Remove-Item clinic.db
mvn javafx:run

# Rebuild completo
mvn clean package
```

## Personalização de Tema
- Claro padrão: `styles.css`
- Escuro: `styles-dark.css`
O botão "Alternar Tema" troca dinamicamente a folha de estilo.

## Próximos Passos (Sugestões)
- Persistir lista de espera por especialidade (criar tabela `specialty_waitlist`)
- Validação avançada de entrada (mask telefone, etc.)
- Testes automatizados (JUnit) para regras de conflito
- Internacionalização (i18n)

## Solução de Problemas
- "Cannot resolve symbol" no IDE: reimportar Maven / Invalidate Caches.
- Erros falsos de @FXML "unused": o FXML chama os métodos; pode ignorar.
- Janela não abre: confirme JavaFX versão e JDK 17.

## Licença
Uso livre para estudo.
