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
**Trabalho individual e autoral.** Entrega e apresentação oral em **17/11** (adiada de 16/11), em aula. Deve ser entregue o link de um repositório **público** no GitHub, e o app precisa estar **funcionando na apresentação**.

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
  - `recommendations`: `id`, `user_id` (FK → `users.id`), `score` (10–100), `archetype`, `recommended_song`, `song_artist`, `subgenres`, `liked` (bool), `surprise` (bool, padrão false, veio do Me surpreenda), `created_at`.
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
1. **MainActivity:** vinil nascendo no horizonte e girando (camadas `synth_sky`/`synth_sun`/`synth_ground`), título neon, frase curta ("Conte qual é o seu artista favorito, escolha o clima do momento e descubra uma música nova de um artista parecido.") e 2 botões ("Descobrir Meu Som" e "Minhas Descobertas"). Intents explícitas.
2. **UserProfileActivity: "conversa", uma pergunta por vez** (mesma Activity, 3 blocos `step*` no XML e o Kotlin troca a visibilidade; "Passo X de 3" + ProgressBar; Voltar/Continuar; o voltar do celular volta um passo). **Desde 17/11 são 3 passos: o do instrumento (item 4 abaixo) saiu, e a busca do artista usa o Deezer (nome oficial, foto, parecidos) com o iTunes dando o gênero/cor do card e servindo de reserva:**
   1. "Como você se chama?" (`TextInputLayout`)
   2. "Qual seu cantor ou banda favorita?" → botão "Buscar artista" consulta o **Deezer** (nome oficial, foto, parecidos) e o **iTunes** (gênero) e mostra um card. Sem internet ou não encontrado: aviso e segue só com o texto digitado (`artistGenre = null`).
   3. "Quando você mais ouve música?" (`ChipGroup`: Relaxando em casa, Academia, Na rua, Festa com amigos, Estudando)
   4. "Você toca algum instrumento?" (`ChipGroup`: Não toco, Guitarra, Bateria, Baixo, Teclado, Canto)
   - **Spinner saiu**; o **Switch voltou** no quiz (hoje "Quero me arriscar"). São 10 tipos de View: TextView, EditText, Button, ImageView, CheckBox/Chip, RadioButton, Switch, RecyclerView, ProgressBar, CardView (o edital pede 6).
3. **SoundQuizActivity (refeita em 17/11): uma pergunta por vez na mesma Activity, como o perfil** (pedido do estudante). Cada passo é um `Step(cards, question?)`; `showStep` mostra só os cards do passo, "Pergunta X de N" + ProgressBar, Voltar/Continuar fixos embaixo (o último vira "Ver Minha Música"), botão travado até responder a pergunta do passo, voltar do celular volta uma pergunta. **Descoberta (3 passos, quando há parecidos do Deezer):** 1) "Outros artistas que você curte": grade de 2 colunas (`GridLayout`) com um `MaterialCardView` marcável por parecido (`item_artist_card.xml`, foto quadrada do Deezer, contorno ciano e ícone quando marcado; contador "2 marcados · 6 na disputa"); **os marcados saem da disputa** (decisão do estudante: é descoberta); 2) "Que clima você quer agora?" (as 6 energias de `model/Energy`); 3) "Hit do momento ou pérola escondida?" (`model/Popularity`) + **Switch "Quero me arriscar"**. **Quiz reserva (2 passos, sem parecidos):** clima; "Outros estilos" (CheckBox) + o mesmo Switch (sem outro estilo marcado, Toast pede para marcar). Opções de escolha única criadas no Kotlin com `item_quiz_option.xml`. Durante a busca das músicas aparece `layoutSearching` e os botões travam.
4. **ResultActivity:** estilo "tocando agora": capa grande (260dp; a do Deezer vem em `Song.coverUrl`, a do catálogo fixo é buscada no iTunes, vinil como reserva), música, ícones Spotify/YouTube (Simple Icons, brancos). Na descoberta o card é **"PARECIDO COM / <artista favorito>"** com o **% de match** ("É o quanto essa música chega perto do clima que você pediu.") e a linha "Dançante · Hit do momento"; a cor é a do estilo do artista (magenta se o iTunes não souber). No quiz reserva continua "SEU PERFIL SONORO / <estilo>", destaque do gênero e outros estilos. `cardTitle` e `cardDetail` servem à tela, ao POST (`archetype`, `subgenres`) e ao compartilhar. **"Curti" / "Não curti"** salvam (POST com `liked`) com `ProgressBar`. Compartilhar (`ACTION_SEND`); YouTube/Spotify (`ACTION_VIEW`, Spotify tenta `spotify:search:` e cai no web).
5. **HistoryActivity:** `RecyclerView` + `CardView` em lista de músicas (capa via `network/CoverRepository`, linha "Parecido com Metallica · 98%"), coração = PUT do `liked`, lixeira = DELETE com `AlertDialog`, estado vazio.

### Mockup visual
- Canvas antigo (anterior às mudanças de 16/11): https://claude.ai/artifact/KByessN86rhHbGwmUw6df8

### Finalidade do app (decidida pelo estudante)
**Recomendar uma MÚSICA nova de qualquer estilo** (não só rock), a partir do artista favorito e das respostas. Se a pessoa coloca BTS, a recomendação tem que ser do universo dela (ex.: NewJeans), nunca um rock aleatório.

### Motor reserva: votação por gênero (`logic/SoundMatchEngine.kt`)
- **Desde 17/11 é a reserva:** a recomendação principal é o `logic/DiscoveryEngine.kt` (seção "Recomendação por artistas parecidos"). Este motor entra quando não há artistas parecidos (quiz reserva) ou quando o motor novo devolve null.
- **5 perfis** (`model/Genre.kt`, a ordem importa): POP "Pop & K-pop", ROCK "Rock & Alternativo", HIPHOP "Hip-hop & R&B", ELECTRONIC "Eletrônica", BRAZILIAN "Música Brasileira". `Genre.fromITunes(primaryGenreName)` mapeia por palavras-chave (busca Brasileira primeiro, Pop por último; ex.: "K-pop"→POP, "Metal"→ROCK, "Hip-Hop/Rap"→HIPHOP, "Dance"→ELECTRONIC, "Sertanejo"/"MPB"/"Pagode"→BRAZILIAN; "Reggae"→null).
- **Votos:** gênero do artista favorito (iTunes) = **3 votos** (`ARTIST_VOTES`); cada estilo marcado = 1. Ganha o mais votado; **empate → gênero do artista, depois a ordem do enum**. **Quero me arriscar** (antigo Me surpreenda): estilo marcado mais votado fora o de sempre (`surpriseGenre`); `SoundResult.surprise` esconde o % no resultado ("Fora da sua zona de conforto") e o histórico mostra "· surpresa".
- **% de match** = votos do vencedor / total × 100, arredondado e limitado a 10–100 (substituiu o score "raiz → experimental", que o estudante achou sem sentido e com "cara de IA").
- **Música** (`logic/SongCatalog.kt`: **12 por gênero, 60 no total, 2 de cada energia**, todas conferidas no iTunes BR): filtra pela energia pedida, exclui o artista favorito e faz uma escolha fixa pelo hash do nome. O instrumento saiu do catálogo em 17/11.
- **Explicação:** cita o nome, o artista, o gênero, a energia (`Energy.phrase`), a situação (`Situation.context`) e a descoberta. Para quem se arrisca: "você quis sair da zona de conforto: em vez de X, a descoberta vem de Y".
- **iTunes** (`network/ITunesApi.search`, `country=BR`, que acerta mais os gêneros brasileiros); `network/ImageLoader` (capa 600x600, download em `Dispatchers.IO`).
- **Testes (38):** `DiscoveryEngineTest.kt` (12), `SoundMatchEngineTest.kt` (15), `DiscoveryRulesTest.kt` (9), `TrackPickerTest.kt` (2). Capas do resultado/histórico usam `TrackPicker.bestMatch` (artista certo, versão de estúdio).

### Estrutura do código (decidida com a delegação do estudante)
- **Pacotes** em `com.example.soundmatch`: `ui/` (Activities), `adapter/` (RecyclerView), `network/` (Retrofit + ImageLoader), `model/` (dados), `logic/` (regra de negócio, sem Android).
- **Layouts:** ScrollView + LinearLayout. Campos com `TextInputLayout` neon (`Widget.SoundMatch.TextInput`), perguntas do perfil com `Widget.SoundMatch.ProfileQuestion`, chips `Widget.SoundMatch.Chip` (48dp, 16sp).
- **Dados entre telas:** `UserProfile` (name, favoriteArtist, situation, artistGenre?, relatedArtists) e `SoundResult` (profile, match, genre, song, reason, likedGenres, surprise, fromSimilarArtists) são `Serializable`, lidos com `IntentCompat.getSerializableExtra`. As chaves `EXTRA_*` ficam no `companion object` da Activity que recebe.

### Ajustes de 16/11 (pedidos pelo estudante)
- **Sem emoji** nos textos do app e **sem "cara de IA"**: nada de jargão técnico na tela ("API", "Banco"), comentários curtos e naturais, sem `REQUISITO:`. O mapa dos requisitos fica no `GUIA_APRESENTACAO.md`.
- **Não gosta de "cara de formulário"** (por isso o perfil virou conversa em passos).
- Edge-to-edge do Android 16 desligado com `windowOptOutEdgeToEdgeEnforcement` (o topo ficava embaixo da barra de status).
- **App rodando no celular físico** (Galaxy S23 FE) com `BASE_URL = http://localhost:8000/` + `adb reverse tcp:8000 tcp:8000`. Ao capturar prints com `adb exec-out screencap`, **nunca tocar no celular** (o estudante usa ao mesmo tempo); se a tela não for o SoundMatch, apagar o print sem abrir.
- **Ambiente:** o projeto Android é aberto no Android Studio (Windows) a partir de uma **cópia** em `C:\Users\pedro\AndroidStudioProjects\SoundMatch`. Depois de editar no WSL, sincronizar com `rsync -a --delete --exclude build --exclude .gradle --exclude .idea --exclude local.properties android/ /mnt/c/Users/pedro/AndroidStudioProjects/SoundMatch/`. Build: `gradlew.bat --no-daemon` com `JAVA_HOME=C:\Users\pedro\.jdks\temurin-17.0.21` (o Java 25 do AS não roda o Gradle 8.9). O backend roda no WSL (venv com Python 3.14).

### Ainda a decidir com o estudante (próximos passos, nesta ordem)
1. ✅ **Perguntas do quiz (16/11):** a partir da análise do prompt do Gemini, ficou só a ideia de energia + funil; quiz refeito em 5 etapas (catálogo, motor, iTunes, tela, campo `surprise`).
2. ✅ **Design (16/11):** A) synthwave contido (Monoton só na Main, títulos sans-serif-black + `HorizonLine` em degradê, cards sem contorno); B) Histórico como lista de músicas (capa do iTunes via `network/CoverRepository`, coração = PUT, lixeira = DELETE); C) Perfil novo (céu/sol/chão no rodapé com os botões por cima, sol sobe a cada passo em `moveSun`, frase `tvLead` com a resposta anterior, card do artista na cor do gênero). Prints e README/guia atualizados.
3. ✅ README e `GUIA_APRESENTACAO.md` atualizados para gêneros/votação/% de match (16/11), com 6 prints em `docs/screenshots/` tirados no celular. `LEIA-ME.txt` apagado.
4. ✅ Commit e push da reforma e da documentação (repositório público). API publicada no Render em 16/11 e app apontando para ela.
5. ✅ Mockup do novo Perfil (https://claude.ai/artifact/B7KUviGeVDxj1Vw9NfRrFj) implementado.
6. ✅ "Passo 2 de 2" virou "Última parte"; "outros estilos" não repete mais o perfil vencedor.
7. ✅ `/code-review` (17/11): achados de casos raros (Taylor's Version, "Queen" x ABBA, rotação) ficaram de lado; o estudante quer resolver a causa maior, a recomendação pouco pessoal.
8. ⏳ **Recomendação por artistas parecidos (17/11, aprovada):** ver seção abaixo.
- Adiado: Top 3 músicas favoritas, swipe estilo Tinder.

### Recomendação por artistas parecidos (aprovada em 17/11, em andamento)
- **Problema:** 5 gêneros + catálogo fixo de 60 músicas não personalizam (forró caía em "Música Brasileira" com Sepultura e Tom Jobim; a 1ª pergunta só repetia o gênero do artista).
- **Deezer** (público, sem chave): `search/artist` (escolher o de mais fãs, `nb_fan`, ex.: há vários "BTS"), `artist/{id}/related`, `artist/{id}/top` (tem `rank`, capa, `preview`; **não** traz ISRC), `track/{id}` (traz `isrc`). O BPM do Deezer vem 0 quase sempre e o `gain` não separa calmo de pesado (testado), por isso não usar.
- **ReccoBeats** (grátis, sem chave): `GET https://api.reccobeats.com/v1/audio-features?ids=<ISRC,ISRC...>` aceita ISRC em lote e devolve `energy`, `valence`, `danceability`, `acousticness`, `instrumentalness`, `tempo`. Testado: Slipknot 0.98 de energia, Cais 0.20, Garota de Ipanema acústica 0.93.
- **Perfil:** nome → artista (Deezer) → quando ouve (a situação ajusta o alvo: Academia mais energia, Estudando mais calmo/acústico, Festa mais dançante). **Instrumento saiu.**
- **Quiz:** 1) "Desses parecidos com X, quais você já conhece?" (chips com foto; os conhecidos saem da disputa); 2) clima = as 6 energias (alvo nos números do ReccoBeats; Sofrência usa `valence` baixa, Viajada música longa e pouco dançante, Pra cantar junto alegria alta + mais tocada); 3) "Hit do momento ou pérola escondida?" (`rank` do Deezer); 4) **Switch "Quero me arriscar"** (parecidos dos parecidos), no lugar do Me surpreenda. "Outros estilos" sai.
- **Escolha:** entre as top músicas dos parecidos não conhecidos, a mais perto do alvo. **% de match** = proximidade do clima pedido.
- **Reserva:** sem Deezer → iTunes + catálogo atual; sem ReccoBeats (nenhuma candidata com os números do som) → o `DiscoveryEngine` devolve null e a tela usa o motor reserva (mudou em relação ao plano, que era escolher pela popularidade: sem os números não há % honesto e a API exige um). API própria (CRUD, Swagger, banco) não muda.
- **Etapas (um commit cada):** 1) rede (Deezer + ReccoBeats + testes); 2) perfil; 3) quiz; 4) motor; 5) resultado/histórico, build, celular, README e guia.
- **Andamento:**
  - ✅ **Etapa 1** (`15566f7`): `model/DeezerModels.kt` (`DeezerList`, `DeezerArtist` Serializable, `DeezerAlbum`, `DeezerTrack`), `model/AudioFeatures.kt`, `model/Candidate.kt` (música do Deezer + features ou null), `network/DeezerApi.kt`, `network/ReccoBeatsApi.kt`, `network/DiscoveryRepository.kt` (`findArtist`, `relatedArtists`, `candidates` com `async` em paralelo; falha do ReccoBeats vira lista vazia), `logic/DiscoveryRules.kt` + `DiscoveryRulesTest.kt` (6 testes).
  - ✅ **Etapa 2** (`6909c9e`): perfil com 3 passos; `searchArtist()` faz Deezer + iTunes; `network/NetworkCall.kt` com `networkOrNull` (falha de rede vira null, evita repetir try/catch); `UserProfile.relatedArtists` leva os parecidos para o quiz; `DiscoveryRules.pickArtist` (entre os de nome igual ao digitado, sem acento/maiúscula, o de mais fãs; sem nome igual, o 1º resultado: "Queen" de verdade é o 5º resultado do Deezer e "Matuê" o 2º). Testado no emulador (Wesley Safadao → card com foto e "Brasileira"). **26 testes passando.** Etapas 1 e 2 com push em 17/11.
  - ✅ **Etapa 3** (`ceec03d`): quiz novo em `ui/SoundQuizActivity.kt` + `activity_sound_quiz.xml` (5 cards; o Kotlin esconde os que não são do modo com `isVisible`), `item_artist_chip.xml` (chip com o vinil até a foto chegar; foto recortada com `RoundedBitmapDrawableFactory`), `model/Popularity.kt` (HIT, GEM), `QuizAnswers` com `knownArtists` e `popularity`. Mockup aprovado: opção A de https://claude.ai/artifact/96VMHxHZXv7AGX9ZDDbuag; o chip marcado ficou magenta (mesmo estilo do perfil), não ciano. O "Me surpreenda" saiu dos textos: o switch é "Quero me arriscar" nos dois modos. Testado no emulador nos dois modos (Wesley Safadao → 10 chips; artista inventado → reserva). 26 testes passando.
  - ✅ **Etapa 4** (`cc416d3`): `logic/DiscoveryEngine.kt`. Entram as 4 mais tocadas de até 8 parecidos não conhecidos (`MAX_ARTISTS`, `TRACKS_PER_ARTIST`: o Deezer devolve "Quota limit exceeded" com HTTP 200 e `data` null acima de 50 chamadas em 5 s, por isso `DeezerList.data` é nullable e `withIsrc` cai na faixa sem ISRC). **Quero me arriscar** ou todos conhecidos (`DiscoveryRules.goesFarther`): parecidos dos 3 primeiros (`fartherArtists`), fora os parecidos diretos. Só disputam músicas com os números do ReccoBeats (cobriu 24 de 39 no forró). **Hit ou pérola:** ordena por `rank` e corta no meio (`popularityHalf`). **Clima:** alvo por energia em `targetFor` (Porradeira energy 0,95 + acousticness 0,05; Dançante danceability 0,85 + energy 0,8; Pra cantar junto valence 0,9 + energy 0,75; Sofrência valence 0,15; Viajada danceability 0,3 + energy 0,45 + música de 6 min ou mais; De boa energy 0,3 + valence 0,6); `closeness` = 1 − média das distâncias; **% de match** = closeness × 100 (10–100). **Situação** só desempata (`situationBonus` × 0,1: academia energia, festa dançante, estudando acústica e calma, relaxando calma, na rua nada). "Pra cantar junto" não puxa mais para a mais tocada (o hit/pérola já faz isso). `SoundResult.fromSimilarArtists` troca a frase do % ("É o quanto essa música chega perto do clima que você pediu"). A tela do quiz busca com `DiscoveryRepository.candidatesFor` e mostra `progressSearch`. Limpeza: saíram `Instrument.kt`, `ArtistSong.kt`, `TrackPicker.artistSongs`, `firstPick`, `UserProfile.instrument/artistSongs`; a API ainda recebe `plays_instrument = false`. Testado no emulador: Wesley Safadão + Dançante + Hit → "Tapão Na Raba" (Raí Saia Rodada, 98%); arriscando → Nuzio Medeiros (94%); Sofrência no forró → 62%; Metallica + Porradeira + Pérola → "Wasted Years" (Iron Maiden, 98%).
  - ✅ **Etapa 5** (`95e8bf4`): card "Parecido com" escolhido pelo estudante entre 3 opções; `SoundResult.genre` virou nullable e ganhou `popularity`; `Song` ganhou `album` e `coverUrl`; `RecommendationRequest.from(result, liked, cardTitle, cardDetail)`. O Deezer passou a devolver **8 parecidos** (`DeezerApi.relatedArtists`, limit 8), que é quantos o motor percorre. Testado no **celular físico** (SM-S711B, serial `RXCWB08Q97J`, 1080x2340; "Descobrir Meu Som" em 540,1477 e "Minhas Descobertas" em 540,1680), salvando no Render. 5 prints novos em `docs/screenshots/` (02 a 06), README e `GUIA_APRESENTACAO.md` reescritos (roteiro, fluxo Deezer/ReccoBeats, papel dos arquivos, perguntas prováveis).
  - ✅ **Ajustes pedidos depois da etapa 5 (17/11, madrugada):** pergunta do perfil "Qual seu cantor ou banda favorita?"; frase da tela inicial "Conte qual é o seu artista favorito, escolha o clima do momento e descubra uma música nova de um artista parecido."; parecidos em cards grandes (os chips com foto ficaram pequenos no celular: um por linha, por causa da fonte maior do aparelho); quiz em passos. A explicação virou "você ouve X, e Y tem um som parecido". `DeezerArtist.picture` usa `picture_big`. `DiscoveryRules.attachFeatures` faz a **média** das medições repetidas do ReccoBeats (ele devolve várias por ISRC, com energia variando até ~0,09): antes ficava a última, e as mesmas respostas chegaram a dar músicas diferentes (Iron Maiden x Slash, empate de 0,002 na nota). **38 testes.** Os 6 prints de `docs/screenshots/` foram refeitos no celular.
  - **Testes no celular por adb:** scripts no scratchpad da sessão se perdem; o caminho é `uiautomator dump` + toque pelo centro do elemento (id ou começo do texto). Opções do quiz têm o texto "Nome&#10;explicação" (procurar por "Hit do momento&#10;" para não bater no título). O estudante liberou tocar na tela nesta sessão; a regra geral continua sendo pedir antes.
- **Aprendido nos testes:** ReccoBeats aceita **no máximo 40 ISRCs** por chamada (`ISRCS_PER_CALL`) e pode devolver mais de um item por ISRC; aceita o cliente OkHttp (bloqueia o `Python-urllib`, usar `curl` para testar). Achados do `/code-review` deixados de lado: "(Taylor's Version)" e "feat. Oliver" descartados pelo `TrackPicker.isAlternateVersion`, rotação da tela apaga respostas do quiz e permite salvar 2 vezes (nenhuma tela trava em retrato), `contains` com nomes curtos ("IU", "Ana").
- **Emulador:** AVD `Pixel_9_Pro_XL` (1344x2992). O PC tem 16 GB e fica com ~0,5 GB livre com ele aberto: abrir com `emulator.exe -avd Pixel_9_Pro_XL -no-window -no-audio -no-boot-anim -no-snapshot-load`, testar com `adb -s emulator-5554` (`uiautomator dump` funciona fora da MainActivity) e fechar com `adb emu kill`. O estudante também usa o Remote Control pelo celular, que só aparece no `adb` com cabo.

## 6. Status das Etapas
0. ✅ `.gitignore` + backend
1. ✅ Esqueleto Android: Gradle, tema, cores, textos, fonte e drawables (build validado)
2. ✅ MainActivity + UserProfileActivity
3. ✅ Lógica de recomendação + SoundQuizActivity + ResultActivity + testes
4. ✅ Retrofit (POST/GET/PUT/DELETE) + HistoryActivity/HistoryAdapter com AlertDialog
5. ✅ README.md + GUIA_APRESENTACAO.md (atualizados em 16/11)
7. ✅ Redesign A/B/C (synthwave contido, Histórico em lista, Perfil com pôr do sol) com prints novos e push.
8. ✅ Quiz novo (músicas do artista + 6 energias + Me surpreenda + `surprise` na API) com push e prints novos no README.
9. ✅ Recomendação por artistas parecidos (Deezer + ReccoBeats): as 5 etapas com commit e push (rede, perfil, quiz, motor, resultado + prints + README + guia). Detalhes na seção "Recomendação por artistas parecidos".
6. ✅ Reforma de 16/11 (gêneros + votação + % de match + perfil em passos + Curti/Não curti) com commit, build e testes passando. Perguntas do quiz refeitas depois (ver item 8).
