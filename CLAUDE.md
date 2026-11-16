# CLAUDE.md - Diretrizes e Regras de Trabalho (AP2 Mobile)

## 1. Como Você Deve Trabalhar Comigo (REGRA DE OURO)
Você é meu parceiro de desenvolvimento (Pair Programmer) e mentor técnico.
- **NUNCA saia criando código ou tomando decisões sozinho.**
- Quero construir o aplicativo passo a passo com você, entendendo cada escolha porque vou apresentar e explicar todo o código ao vivo para o professor.
- Antes de programar qualquer parte (seja o visual das telas, a lógica do quiz, o banco de dados ou a API), **converse comigo**: me apresente opções (ex: opções de paleta de cores, ideias de layout, estrutura das perguntas), explique o porquê de cada escolha e espere eu decidir.
- Só crie ou edite arquivos quando eu aprovar a ideia que discutimos.
- Trabalhamos **uma etapa por vez**. Ao final de cada etapa, explique de forma resumida o papel de cada arquivo criado.
- **Commits:** um commit ao final de cada etapa aprovada, com mensagem em português. **Nunca** adicionar `Co-Authored-By: Claude` nem qualquer linha de atribuição ao Claude/IA nas mensagens de commit ou PR.

---

## 2. O Tema Base
**SoundMatch:** Um aplicativo Android de recomendação musical e descoberta de perfil sonoro integrado a uma API REST. (Os detalhes das perguntas, o visual das telas e a lógica de recomendação serão decididos em conversa comigo).

---

## 3. Requisitos Obrigatórios do Professor (Não podemos esquecer nenhum)
**Trabalho individual e autoral.** Entrega e apresentação oral em **16/11**, em aula. Deve ser entregue o link de um repositório **público** no GitHub, e o app precisa estar **funcionando na apresentação**.

Nosso projeto precisa obrigatoriamente cumprir todos os itens abaixo:

1. **Estrutura e Navegação (Android Nativo em Kotlin com XML Views):**
   - Mínimo de **5 Activities distintas**.
   - Navegação entre as telas usando **Intents explícitas**.
   - Pelo menos **1 Intent implícita** (ex: abrir link web/YouTube/Spotify, compartilhar texto, enviar e-mail, etc.).
2. **Interface Gráfica (UX/UI) e Componentes:**
   - Visual organizado, boa legibilidade, consistência de cores e feedback visual para o usuário.
   - Usar no mínimo **6 tipos diferentes de Views** dentre: `TextView`, `EditText`, `Button`, `ImageView`, `CheckBox`, `RadioButton`, `Spinner`, `Switch`, `RecyclerView`, `ProgressBar`, `CardView`.
3. **API REST + Swagger:**
   - Backend (Python/FastAPI ou Node.js) com endpoints funcionais de cadastro (`POST`), consulta (`GET`), alteração (`PUT`) e remoção (`DELETE`) trafegando JSON.
   - Documentação **Swagger/OpenAPI** obrigatória.
4. **Banco de Dados Relacional:**
   - Persistência em banco relacional (ex: SQLite, PostgreSQL ou MySQL) através da API REST.
5. **Organização e GitHub:**
   - Separação clara de pacotes e classes, nomenclatura coerente, sem código redundante, boas práticas e commits frequentes.
   - `README.md` completo ao final do projeto, contendo:
     - título;
     - descrição da proposta;
     - tecnologias;
     - instruções de execução;
     - **prints/screenshots do app**;
     - descrição da API;
     - link do Swagger.

**Critérios de avaliação:**

| Critério | Pontos |
|---|---|
| Funcionamento geral | 1,0 |
| 5 Activities | 1,5 |
| Intents | 1,0 |
| UX/UI | 1,0 |
| API REST | 1,0 |
| Banco relacional | 1,0 |
| Organização do código | 1,0 |
| **Apresentação oral e domínio técnico** | **2,5** |

**Bônus de +0,5** por pelo menos um destes:
- app rodando em **celular físico** (o nosso caminho, via `adb reverse tcp:8000 tcp:8000`);
- publicação na Play Store (beta);
- deploy da API em nuvem.

Código copiado da internet ou de colegas leva nota zero, e eu preciso demonstrar que conheço o meu próprio código.

---

## 4. Trava de Segurança Técnica do Android
- Usar **XML Views com ViewBinding** (`buildFeatures { viewBinding = true }`), sem Jetpack Compose.
- Versões **validadas em build real** (`assembleDebug` passou), que não devem ser mudadas sem testar:
  - Android Gradle Plugin **8.7.3**, Kotlin **2.0.21**, Gradle Wrapper **8.9**, Java 17.
  - `compileSdk = 35`, `targetSdk = 35`, `minSdk = 24`.
  - **`androidx.core:core-ktx:1.15.0`**. A `1.17.0` foi testada e **quebra o build**, porque exige `compileSdk 36` e AGP 8.9.1. Nunca usar `1.16+`, `1.17.0` nem `1.19.+`. O edital do professor não exige versão nenhuma.
  - Demais dependências:
    - `appcompat:1.7.0`
    - `material:1.12.0`
    - `constraintlayout:2.1.4`
    - `recyclerview:1.3.2`
    - `cardview:1.0.0`
    - `lifecycle-runtime-ktx:2.8.7`
    - `retrofit:2.9.0` + `converter-gson:2.9.0`
    - `kotlinx-coroutines-android:1.7.3`
- Pacote base: `com.example.soundmatch`.

---

## 5. Decisões Já Aprovadas
### Backend (concluído e testado)
- **Stack:** FastAPI + SQLAlchemy 2.0 + SQLite (`soundmatch.db`), na pasta `/backend`.
- **Arquivos:**
  - `database.py`: conexão e sessão.
  - `models.py`: tabelas.
  - `schemas.py`: JSON e validação.
  - `main.py`: endpoints.
  - `requirements.txt`: dependências.
- **Tabelas:**
  - `users`: `id`, `name`, `favorite_artist`, `listening_mode`, `plays_instrument`, `created_at`.
  - `recommendations`: `id`, `user_id` (FK → `users.id`), `score` (10–100), `archetype`, `recommended_song`, `song_artist`, `subgenres`, `personal_note`, `created_at`.
  - Se existir um `soundmatch.db` antigo (com `recommended_album`), apague-o. A API recria o banco ao iniciar.
  - Relação 1 usuário → N recomendações.
- **Endpoints** (Swagger em `/docs`):
  - `POST /recommendations`: cria o usuário ou reaproveita um existente pelo nome.
  - `GET /recommendations`: lista com o usuário aninhado.
  - `GET /recommendations/{id}`
  - `PUT /recommendations/{id}`: altera a `personal_note`.
  - `DELETE /recommendations/{id}`
- **Rodar:** `uvicorn main:app --host 0.0.0.0 --port 8000`.
- **Endereço no Android:** `http://10.0.2.2:8000/` no emulador, ou `http://localhost:8000/` com `adb reverse` no celular físico.

### Identidade visual: "Neon Miami / synthwave anos 80" (tema escuro)
- Inspirada na estética de Hotline Miami. **Não usar** logo, personagens, máscaras ou arte do jogo. No README e na apresentação, chamar de "Neon Miami / synthwave anos 80".
- **Paleta:**
  - Fundo `#0D0221` e cards `#1A0B3B`
  - Magenta `#FF2A6D` como cor primária e ciano `#05D9E8` como secundária
  - Amarelo `#F9C80E` e laranja `#FF6C11`
- **Fonte retrô Monoton** (licença OFL), só nos títulos, com brilho neon.
- **Drawables vetoriais:** fundo synthwave (sol listrado + grade) e disco de vinil (substituiu o globo disco em 16/11).
- **Extras aprovados:**
  - o disco de vinil gira na MainActivity (3 s por volta);
  - o título fica inclinado e pulsa entre magenta e ciano;
  - **cada arquétipo tem uma cor**, usada no card do resultado e na faixa do histórico.
- Cores, estilos e textos centralizados em `colors.xml`, `themes.xml` e `strings.xml`. Não deixar cor nem texto fixo nos layouts.

### As 5 telas (Activities)
1. **MainActivity:** disco de vinil girando (`ImageView`), título neon (`TextView`), card "Como funciona" (`CardView`) e 2 botões ("Descobrir Meu Som" e "Minhas Descobertas"). Usa Intents explícitas para o Perfil e o Histórico.
2. **UserProfileActivity:** nome e artista favorito (`EditText`), modo de escuta (`Spinner`), "toco instrumento" (`Switch`), validação com `Toast` e Intent explícita com extras.
3. **SoundQuizActivity:** **tela única com rolagem**, com um card por pergunta (`RadioGroup` + `RadioButton`), subgêneros em `CheckBox` e uma `ProgressBar` que avança a cada resposta.
4. **ResultActivity:**
   - card na cor do arquétipo, com score, a música recomendada, **capa e nome do álbum** (iTunes Search API, com o vinil como reserva), a explicação do porquê e uma nota pessoal (`EditText`);
   - botão "Salvar nas Minhas Descobertas" (POST) com `ProgressBar`;
   - **Intents implícitas**: YouTube e Spotify (`ACTION_VIEW`; o Spotify tenta `spotify:search:` e cai no web) e Compartilhar (`ACTION_SEND`).
5. **HistoryActivity:** `RecyclerView` + `CardView`, botões Editar Nota (`AlertDialog` → PUT) e Excluir (DELETE), e mensagem de estado vazio.

Isso dá 11 tipos de Views, e o edital exige 6.

### Mockup visual (aguardando revisão do estudante)
- Canvas com as 5 telas no claude.ai (privado, só o estudante acessa): https://claude.ai/artifact/KByessN86rhHbGwmUw6df8
- A Etapa 2 começa revisando este mockup. Os XMLs devem seguir o visual aprovado nele.

### Finalidade do app (decidida pelo estudante)
**Recomendar uma MÚSICA nova** com base no gosto do usuário e nas respostas do quiz, em vez de um álbum fixo.

### Lógica de recomendação (`logic/SoundMatchEngine.kt` + `logic/SongCatalog.kt`)
1. **Score:**
   - Cada pergunta vale de 0 a 3 pontos, pelo índice da opção, ordenadas do mais "raiz" ao mais "experimental":
     - Playlist de viagem (`playlist`): Blues de estrada 0, Riff pesado 1, Groove 2, Faixa longa e viajante 3
     - No show (`concert`): Na grade 0, Roda punk 1, Dançando no fundo 2, Prestando atenção nos detalhes 3
     - Música de 10 min (`longTrack`): Pula 0, Dá uma chance 1,5, Fica até o fim 3
   - Subgêneros: média dos pesos marcados (Blues 0, Hard 1, Funk 2, Prog 3). Se nenhum estiver marcado, vale 1,5.
   - Fórmula: `score = 10 + (soma / 12) × 90`, arredondado e limitado a 10–100.
2. **Perfil sonoro (`model/Archetype.kt`):**

   | Score | Perfil |
   |---|---|
   | 10–35 | Vintage Blues & Classic Rock |
   | 36–60 | Hard Rock & Heavy Riffs |
   | 61–80 | Groove & Funk Rock Alternativo |
   | 81–100 | Explorador Progressivo & Conceitual |

3. **Música:** cada perfil tem 5 músicas no catálogo, marcadas por subgênero. A escolha:
   - **exclui o artista favorito** (a ideia é descobrir algo novo);
   - **prioriza** as músicas com mais subgêneros marcados em comum;
   - desempata de forma determinística (nome + score).
4. **Explicação:** um texto com o porquê da recomendação. Se o usuário toca instrumento, ganha uma dica do que prestar atenção.
5. **Testes unitários:** `app/src/test/.../logic/SoundMatchEngineTest.kt` (JUnit 4).

### Estrutura do código (decidida com a delegação do estudante)
- **Pacotes** em `com.example.soundmatch`:
  - `ui/`: Activities
  - `adapter/`: RecyclerView
  - `network/`: Retrofit
  - `model/`: dados
  - `logic/`: regra de negócio, sem Android
- **Layouts:** ScrollView + LinearLayout. Campos com `TextInputLayout` neon (`Widget.SoundMatch.TextInput`).
- **Dados entre telas:** `UserProfile` e `SoundResult` são `Serializable`, lidos com `IntentCompat.getSerializableExtra`. As chaves (`EXTRA_*`) ficam no `companion object` da Activity que recebe.

### Ajustes de 16/11 (pedidos pelo estudante)
- **Textos de tela sem jargão técnico:** nada de "API" ou "Banco" para o usuário. O histórico se chama "Minhas Descobertas".
- **Comentários curtos e naturais:** sem marcações `REQUISITO:` e sem KDoc óbvio. O mapa de onde está cada requisito fica no `GUIA_APRESENTACAO.md`.
- **API externa:** `network/ITunesApi.kt` + `model/ITunesModels.kt`. O `RetrofitClient` tem `api` (a nossa) e `itunes`. A imagem é baixada sem biblioteca nova (`URL.openStream` em `Dispatchers.IO`).
- **App rodando no celular físico** (Galaxy S23 FE) com `BASE_URL = http://localhost:8000/` + `adb reverse`.
- **Ambiente:** o projeto Android é aberto no Android Studio (Windows) a partir de uma **cópia** em `C:\Users\pedro\AndroidStudioProjects\SoundMatch`. Depois de editar no WSL, sincronizar com `rsync -a --delete --exclude build --exclude .gradle --exclude .idea --exclude local.properties android/ /mnt/c/Users/pedro/AndroidStudioProjects/SoundMatch/`. O Gradle JDK é o temurin-17 (o Java 25 do AS não roda o Gradle 8.9). O backend roda no WSL (venv com Python 3.14).

### Ainda a decidir com o estudante
- **Top 3 músicas favoritas** no perfil: adiado para depois da apresentação, porque mexe no backend e no banco.
- Ajustes visuais restantes do mockup (fontes, contraste do botão magenta).

---

## 6. Status das Etapas
0. ✅ `.gitignore` + backend
1. ✅ Esqueleto Android: Gradle, tema, cores, textos, fonte e drawables (build validado)
2. ✅ MainActivity + UserProfileActivity
3. ✅ Lógica de recomendação de música + SoundQuizActivity + ResultActivity (YouTube/Compartilhar) + testes
4. ✅ Retrofit (POST/GET/PUT/DELETE) + salvar no Resultado + HistoryActivity/HistoryAdapter com AlertDialog
5. ✅ README.md + GUIA_APRESENTACAO.md (faltam os screenshots em docs/screenshots/)
