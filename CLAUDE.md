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
  - `recommendations`: `id`, `user_id` (FK → `users.id`), `score` (10–100), `archetype`, `recommended_song`, `song_artist`, `subgenres`, `liked` (bool), `created_at`.
  - `users.listening_mode` guarda a **situação** escolhida no perfil (ex.: "Academia").
  - **Os nomes das colunas ficaram da 1ª versão:** `archetype` guarda o perfil/gênero (ex.: "Pop & K-pop"), `score` guarda o **% de match** (10–100) e `subgenres` guarda os "outros estilos" marcados. O backend não mudou na troca para gêneros. Isso está comentado em `model/ApiModels.kt`.
  - Se o schema mudar, apague o `soundmatch.db` (é gitignored). A API recria o banco ao iniciar. Ele já foi recriado com a coluna `liked`.
  - Relação 1 usuário → N recomendações.
- **Endpoints** (Swagger em `/docs`):
  - `POST /recommendations`: cria o usuário ou reaproveita um existente pelo nome.
  - `GET /recommendations`: lista com o usuário aninhado.
  - `GET /recommendations/{id}`
  - `PUT /recommendations/{id}`: altera o `liked` (curti / não curti).
  - `DELETE /recommendations/{id}`
- **Rodar:** `uvicorn main:app --host 0.0.0.0 --port 8000`.
- **Endereço no Android:** o `BASE_URL` aponta para a API no **Render** (`https://soundmatch-api-dnh5.onrender.com/`, Swagger em `/docs`; plano grátis: dorme após 15 min, acorda em ~1 min, SQLite volta vazio ao reiniciar; deploy automático a cada push na `main`; Root Directory `backend`, `PYTHON_VERSION=3.12.7`). Plano B local: `http://localhost:8000/` + `adb reverse` (emulador: `http://10.0.2.2:8000/`).

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
  - **cada perfil (gênero) tem uma cor** (`genre_*` em `colors.xml`: pop magenta, rock laranja, hip-hop amarelo, eletrônica ciano, brasileira verde `#2BE89A`), usada no card do resultado e na faixa do histórico.
- Cores, estilos e textos centralizados em `colors.xml`, `themes.xml` e `strings.xml`. Não deixar cor nem texto fixo nos layouts.

### As 5 telas (Activities)
1. **MainActivity:** vinil nascendo no horizonte e girando (camadas `synth_sky`/`synth_sun`/`synth_ground`), título neon, frase curta ("descubra uma música nova, do estilo que é a sua cara") e 2 botões ("Descobrir Meu Som" e "Minhas Descobertas"). Intents explícitas.
2. **UserProfileActivity: "conversa", uma pergunta por vez** (mesma Activity, 4 blocos `step*` no XML e o Kotlin troca a visibilidade; "Passo X de 4" + ProgressBar; Voltar/Continuar; o voltar do celular volta um passo):
   1. "Como você se chama?" (`TextInputLayout`)
   2. "Qual artista você mais ouve?" → botão "Buscar artista" consulta o **iTunes** e mostra um card (capa, nome oficial via `logic/ArtistName.kt`, gênero do iTunes). Sem internet ou não encontrado: aviso e segue só com o texto digitado (`artistGenre = null`).
   3. "Quando você mais ouve música?" (`ChipGroup`: Relaxando em casa, Academia, Na rua, Festa com amigos, Estudando)
   4. "Você toca algum instrumento?" (`ChipGroup`: Não toco, Guitarra, Bateria, Baixo, Teclado, Canto)
   - **Spinner e Switch saíram** (ainda são 9 tipos de View: TextView, EditText, Button, ImageView, CheckBox/Chip, RadioButton, RecyclerView, ProgressBar, CardView; o edital pede 6).
3. **SoundQuizActivity:** tela com rolagem, 3 cards com `RadioGroup` de **5 opções (uma por gênero, na ordem do enum `Genre`)** + "Outros estilos que você curte" em `CheckBox` (um por gênero) + `ProgressBar`. Perguntas atuais: música para a situação (`rgSongPick`), o que prende no artista favorito (`rgArtistPick`), disco que ouviria inteiro (`rgAlbumPick`). **O estudante ainda acha as perguntas genéricas (ver "Ainda a decidir").**
4. **ResultActivity:** estilo "tocando agora": capa grande (260dp, contorno na cor do perfil; iTunes, vinil como reserva), música, ícones Spotify/YouTube (Simple Icons, brancos), card do perfil com **"67% de match"** + "É quanto das suas respostas apontou para esse estilo, contando o seu artista favorito.", destaque do gênero, outros estilos, explicação. **"Curti" / "Não curti"** já salvam (POST com `liked`) com `ProgressBar`. Compartilhar (`ACTION_SEND`); YouTube/Spotify (`ACTION_VIEW`, Spotify tenta `spotify:search:` e cai no web).
5. **HistoryActivity:** `RecyclerView` + `CardView`, Curtiu (ciano)/Não curtiu (magenta), botão "Mudar para…" (PUT do `liked`), Excluir com `AlertDialog` (DELETE), estado vazio.

### Mockup visual
- Canvas antigo (anterior às mudanças de 16/11): https://claude.ai/artifact/KByessN86rhHbGwmUw6df8

### Finalidade do app (decidida pelo estudante)
**Recomendar uma MÚSICA nova de qualquer estilo** (não só rock), a partir do artista favorito e das respostas. Se a pessoa coloca BTS, a recomendação tem que ser do universo dela (ex.: NewJeans), nunca um rock aleatório.

### Lógica de recomendação: votação por gênero (`logic/SoundMatchEngine.kt`)
- **5 perfis** (`model/Genre.kt`, a ordem importa): POP "Pop & K-pop", ROCK "Rock & Alternativo", HIPHOP "Hip-hop & R&B", ELECTRONIC "Eletrônica", BRAZILIAN "Música Brasileira". `Genre.fromITunes(primaryGenreName)` mapeia por palavras-chave (busca Brasileira primeiro, Pop por último; ex.: "K-pop"→POP, "Metal"→ROCK, "Hip-Hop/Rap"→HIPHOP, "Dance"→ELECTRONIC, "Sertanejo"/"MPB"/"Pagode"→BRAZILIAN; "Reggae"→null).
- **Votos:** gênero do artista favorito (iTunes) = **3 votos** (`ARTIST_VOTES`); cada pergunta do quiz = 1; cada estilo marcado = 1. Ganha o mais votado; **empate → gênero do artista**.
- **% de match** = votos do vencedor / total × 100, arredondado e limitado a 10–100 (substituiu o score "raiz → experimental", que o estudante achou sem sentido e com "cara de IA").
- **Música** (`logic/SongCatalog.kt`: 5 por gênero, 25 no total, cada uma com `featured: Instrument` e um de cada instrumento por gênero; nenhum artista do catálogo aparece nas opções do quiz): exclui o artista favorito; quem toca recebe a música do seu instrumento; os outros, uma escolha fixa pelo hash do nome.
- **Explicação:** cita o nome, o artista, o gênero, o artista da descoberta e, para quem toca, `Instrument.practice` + `focus` ("Você toca baixo: repare na linha de baixo de Espresso").
- **iTunes** (`network/ITunesApi.search`, `country=BR`, que acerta mais os gêneros brasileiros); `network/ImageLoader` (capa 600x600, download em `Dispatchers.IO`).
- **Testes:** `SoundMatchEngineTest.kt`, 12 testes (votação, empate, sem internet, match mínimo, exclusão do favorito, instrumento, determinismo, explicação, `fromITunes`, `ArtistName.clean`). Todos passando.

### Estrutura do código (decidida com a delegação do estudante)
- **Pacotes** em `com.example.soundmatch`: `ui/` (Activities), `adapter/` (RecyclerView), `network/` (Retrofit + ImageLoader), `model/` (dados), `logic/` (regra de negócio, sem Android).
- **Layouts:** ScrollView + LinearLayout. Campos com `TextInputLayout` neon (`Widget.SoundMatch.TextInput`), perguntas do perfil com `Widget.SoundMatch.ProfileQuestion`, chips `Widget.SoundMatch.Chip` (48dp, 16sp).
- **Dados entre telas:** `UserProfile` (name, favoriteArtist, situation, instrument?, artistGenre?) e `SoundResult` (profile, match, genre, song, reason, likedGenres) são `Serializable`, lidos com `IntentCompat.getSerializableExtra`. As chaves `EXTRA_*` ficam no `companion object` da Activity que recebe.

### Ajustes de 16/11 (pedidos pelo estudante)
- **Sem emoji** nos textos do app e **sem "cara de IA"**: nada de jargão técnico na tela ("API", "Banco"), comentários curtos e naturais, sem `REQUISITO:`. O mapa dos requisitos fica no `GUIA_APRESENTACAO.md`.
- **Não gosta de "cara de formulário"** (por isso o perfil virou conversa em passos).
- Edge-to-edge do Android 16 desligado com `windowOptOutEdgeToEdgeEnforcement` (o topo ficava embaixo da barra de status).
- **App rodando no celular físico** (Galaxy S23 FE) com `BASE_URL = http://localhost:8000/` + `adb reverse tcp:8000 tcp:8000`. Ao capturar prints com `adb exec-out screencap`, **nunca tocar no celular** (o estudante usa ao mesmo tempo); se a tela não for o SoundMatch, apagar o print sem abrir.
- **Ambiente:** o projeto Android é aberto no Android Studio (Windows) a partir de uma **cópia** em `C:\Users\pedro\AndroidStudioProjects\SoundMatch`. Depois de editar no WSL, sincronizar com `rsync -a --delete --exclude build --exclude .gradle --exclude .idea --exclude local.properties android/ /mnt/c/Users/pedro/AndroidStudioProjects/SoundMatch/`. Build: `gradlew.bat --no-daemon` com `JAVA_HOME=C:\Users\pedro\.jdks\temurin-17.0.21` (o Java 25 do AS não roda o Gradle 8.9). O backend roda no WSL (venv com Python 3.14).

### Ainda a decidir com o estudante (próximos passos, nesta ordem)
1. **Perguntas do quiz: o estudante ainda acha genéricas.** Ideia escolhida por ele: a **1ª pergunta usa músicas reais do artista favorito** ("Qual dessas do BTS você mais ouve?"; a mesma busca do iTunes com `limit=25` filtrada pelo artista devolve as mais conhecidas, com ano e gênero). Ele quer que **a música escolhida influencie a recomendação, mas NÃO pelo ano**. Também **não gostou** da proposta "O que mais te prende em Dynamite? voz/batida/guitarra/baixo/teclados". Ainda é preciso propor outra forma, e conversar antes de programar.
2. ✅ **Design (16/11):** A) synthwave contido (Monoton só na Main, títulos sans-serif-black + `HorizonLine` em degradê, cards sem contorno); B) Histórico como lista de músicas (capa do iTunes via `network/CoverRepository`, coração = PUT, lixeira = DELETE); C) Perfil novo (céu/sol/chão no rodapé com os botões por cima, sol sobe a cada passo em `moveSun`, frase `tvLead` com a resposta anterior, card do artista na cor do gênero). Prints e README/guia atualizados.
3. ✅ README e `GUIA_APRESENTACAO.md` atualizados para gêneros/votação/% de match (16/11), com 6 prints em `docs/screenshots/` tirados no celular. `LEIA-ME.txt` apagado.
4. ✅ Commit e push da reforma e da documentação (repositório público). API publicada no Render em 16/11 e app apontando para ela.
5. ✅ Mockup do novo Perfil (https://claude.ai/artifact/B7KUviGeVDxj1Vw9NfRrFj) implementado.
6. Notado e não corrigido: o quiz mostra "Passo 2 de 2" (o perfil agora tem 4 passos); no Resultado, "outros estilos" pode repetir o próprio perfil vencedor.
- Adiado: Top 3 músicas favoritas, swipe estilo Tinder.

## 6. Status das Etapas
0. ✅ `.gitignore` + backend
1. ✅ Esqueleto Android: Gradle, tema, cores, textos, fonte e drawables (build validado)
2. ✅ MainActivity + UserProfileActivity
3. ✅ Lógica de recomendação + SoundQuizActivity + ResultActivity + testes
4. ✅ Retrofit (POST/GET/PUT/DELETE) + HistoryActivity/HistoryAdapter com AlertDialog
5. ✅ README.md + GUIA_APRESENTACAO.md (atualizados em 16/11)
7. ✅ Redesign A/B/C (synthwave contido, Histórico em lista, Perfil com pôr do sol) com prints novos e push.
6. ✅ Reforma de 16/11 (gêneros + votação + % de match + perfil em passos + Curti/Não curti) com commit, build e testes passando. Pendentes: perguntas do quiz.
