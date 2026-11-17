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
1. **MainActivity:** vinil nascendo no horizonte e girando (camadas `synth_sky`/`synth_sun`/`synth_ground`), título neon, frase curta ("descubra uma música nova, do estilo que é a sua cara") e 2 botões ("Descobrir Meu Som" e "Minhas Descobertas"). Intents explícitas.
2. **UserProfileActivity: "conversa", uma pergunta por vez** (mesma Activity, 3 blocos `step*` no XML e o Kotlin troca a visibilidade; "Passo X de 3" + ProgressBar; Voltar/Continuar; o voltar do celular volta um passo). **Desde 17/11 são 3 passos: o do instrumento (item 4 abaixo) saiu, e a busca do artista usa o Deezer (nome oficial, foto, parecidos) com o iTunes dando o gênero/cor do card e servindo de reserva:**
   1. "Como você se chama?" (`TextInputLayout`)
   2. "Qual artista você mais ouve?" → botão "Buscar artista" consulta o **iTunes** e mostra um card (capa, nome oficial via `logic/ArtistName.kt`, gênero do iTunes). Sem internet ou não encontrado: aviso e segue só com o texto digitado (`artistGenre = null`).
   3. "Quando você mais ouve música?" (`ChipGroup`: Relaxando em casa, Academia, Na rua, Festa com amigos, Estudando)
   4. "Você toca algum instrumento?" (`ChipGroup`: Não toco, Guitarra, Bateria, Baixo, Teclado, Canto)
   - **Spinner saiu**; o **Switch voltou** no quiz (Me surpreenda). São 10 tipos de View: TextView, EditText, Button, ImageView, CheckBox/Chip, RadioButton, Switch, RecyclerView, ProgressBar, CardView (o edital pede 6).
3. **SoundQuizActivity (refeita em 16/11):** 1ª pergunta "Qual dessas de BTS você colocaria pra tocar numa festa com amigos?" com as 4 músicas do artista (`UserProfile.artistSongs`, vindas da busca do perfil com `limit=25` filtrada por `logic/TrackPicker`; sem artista, pergunta reserva com 5 músicas de 5 gêneros). 2ª pergunta: as **6 energias** (`model/Energy`: Porradeira, Dançante, Pra cantar junto, Sofrência, Viajada, De boa; nome em negrito + `hint`). Opções criadas no Kotlin com `item_quiz_option.xml`. Depois "Outros estilos" (CheckBox) + **Switch Me surpreenda** (sem outro estilo marcado, Toast pede para marcar). Caption "Última parte".
4. **ResultActivity:** estilo "tocando agora": capa grande (260dp, contorno na cor do perfil; iTunes, vinil como reserva), música, ícones Spotify/YouTube (Simple Icons, brancos), card do perfil com **"67% de match"** + "É quanto das suas respostas apontou para esse estilo, contando o seu artista favorito.", destaque do gênero, outros estilos, explicação. **"Curti" / "Não curti"** já salvam (POST com `liked`) com `ProgressBar`. Compartilhar (`ACTION_SEND`); YouTube/Spotify (`ACTION_VIEW`, Spotify tenta `spotify:search:` e cai no web).
5. **HistoryActivity:** `RecyclerView` + `CardView`, Curtiu (ciano)/Não curtiu (magenta), botão "Mudar para…" (PUT do `liked`), Excluir com `AlertDialog` (DELETE), estado vazio.

### Mockup visual
- Canvas antigo (anterior às mudanças de 16/11): https://claude.ai/artifact/KByessN86rhHbGwmUw6df8

### Finalidade do app (decidida pelo estudante)
**Recomendar uma MÚSICA nova de qualquer estilo** (não só rock), a partir do artista favorito e das respostas. Se a pessoa coloca BTS, a recomendação tem que ser do universo dela (ex.: NewJeans), nunca um rock aleatório.

### Lógica de recomendação: votação por gênero (`logic/SoundMatchEngine.kt`)
- **5 perfis** (`model/Genre.kt`, a ordem importa): POP "Pop & K-pop", ROCK "Rock & Alternativo", HIPHOP "Hip-hop & R&B", ELECTRONIC "Eletrônica", BRAZILIAN "Música Brasileira". `Genre.fromITunes(primaryGenreName)` mapeia por palavras-chave (busca Brasileira primeiro, Pop por último; ex.: "K-pop"→POP, "Metal"→ROCK, "Hip-Hop/Rap"→HIPHOP, "Dance"→ELECTRONIC, "Sertanejo"/"MPB"/"Pagode"→BRAZILIAN; "Reggae"→null).
- **Votos:** gênero do artista favorito (iTunes) = **3 votos** (`ARTIST_VOTES`); a música da 1ª pergunta = 1 voto no gênero dela no iTunes; cada estilo marcado = 1. Ganha o mais votado; **empate → gênero do artista, depois o da 1ª pergunta**. **Me surpreenda:** estilo marcado mais votado fora o de sempre (`surpriseGenre`); `SoundResult.surprise` esconde o % no resultado ("Fora da sua zona de conforto") e o histórico mostra "· surpresa".
- **% de match** = votos do vencedor / total × 100, arredondado e limitado a 10–100 (substituiu o score "raiz → experimental", que o estudante achou sem sentido e com "cara de IA").
- **Música** (`logic/SongCatalog.kt`: **12 por gênero, 60 no total, 2 de cada energia**, todos os instrumentos em cada gênero, todas conferidas no iTunes BR; nenhum artista do catálogo aparece na pergunta reserva): filtra pela energia pedida, exclui o artista favorito; quem toca recebe a música do seu instrumento; os outros, uma escolha fixa pelo hash do nome.
- **Explicação:** cita o nome, o artista, a música escolhida ("colocaria Dynamite pra tocar numa festa com amigos"), o gênero, a energia (`Energy.phrase`), a descoberta e, para quem toca, `Instrument.practice` + `focus`. Na surpresa: "você quis sair da zona de conforto: em vez de X, a descoberta vem de Y".
- **iTunes** (`network/ITunesApi.search`, `country=BR`, que acerta mais os gêneros brasileiros); `network/ImageLoader` (capa 600x600, download em `Dispatchers.IO`).
- **Testes:** `SoundMatchEngineTest.kt` (16: votação, empate, surpresa, energia, exclusão do favorito, instrumento, determinismo, explicação, catálogo, `fromITunes`, `ArtistName.clean`) + `TrackPickerTest.kt` (4). Capas do resultado/histórico usam `TrackPicker.bestMatch` (artista certo, versão de estúdio).

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
- **Reserva:** sem Deezer → iTunes + catálogo atual; sem ReccoBeats → escolhe pela popularidade e a energia fica só na explicação. API própria (CRUD, Swagger, banco) não muda.
- **Etapas (um commit cada):** 1) rede (Deezer + ReccoBeats + testes); 2) perfil; 3) quiz; 4) motor; 5) resultado/histórico, build, celular, README e guia.
- **Andamento:**
  - ✅ **Etapa 1** (`15566f7`): `model/DeezerModels.kt` (`DeezerList`, `DeezerArtist` Serializable, `DeezerAlbum`, `DeezerTrack`), `model/AudioFeatures.kt`, `model/Candidate.kt` (música do Deezer + features ou null), `network/DeezerApi.kt`, `network/ReccoBeatsApi.kt`, `network/DiscoveryRepository.kt` (`findArtist`, `relatedArtists`, `candidates` com `async` em paralelo; falha do ReccoBeats vira lista vazia), `logic/DiscoveryRules.kt` + `DiscoveryRulesTest.kt` (6 testes).
  - ✅ **Etapa 2** (`6909c9e`): perfil com 3 passos; `searchArtist()` faz Deezer + iTunes; `network/NetworkCall.kt` com `networkOrNull` (falha de rede vira null, evita repetir try/catch); `UserProfile.relatedArtists` leva os parecidos para o quiz; `DiscoveryRules.pickArtist` (entre os de nome igual ao digitado, sem acento/maiúscula, o de mais fãs; sem nome igual, o 1º resultado: "Queen" de verdade é o 5º resultado do Deezer e "Matuê" o 2º). Testado no emulador (Wesley Safadao → card com foto e "Brasileira"). **26 testes passando.** Commits das etapas 1 e 2 ainda **sem push**.
  - ⏳ **Etapa 3 (quiz), decidido:** pergunta 1 com **chips com a foto redonda** (opção A do mockup https://claude.ai/artifact/96VMHxHZXv7AGX9ZDDbuag; estilo `Widget.SoundMatch.Chip` com a foto no `chipIcon`); "Hit do momento / Pérola escondida" no mesmo formato das energias (nome em negrito + linha explicando); Switch "Quero me arriscar". **Falta o estudante responder:** quiz reserva (proposta: sem parecidos do Deezer, o quiz mostra só o clima + os "outros estilos" de hoje e a música vem do catálogo fixo).
  - Etapa 4: o campo `UserProfile.instrument` segue no código sempre null (e `model/Instrument.kt` existe) só para o build não quebrar; remover junto com a reforma do motor. O quiz e o motor atuais ainda são os antigos.
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
9. ⏳ Recomendação por artistas parecidos (Deezer + ReccoBeats): etapas 1 (rede) e 2 (perfil) com commit; faltam 3 (quiz), 4 (motor) e 5 (resultado, prints, README e guia). Detalhes na seção "Recomendação por artistas parecidos".
6. ✅ Reforma de 16/11 (gêneros + votação + % de match + perfil em passos + Curti/Não curti) com commit, build e testes passando. Perguntas do quiz refeitas depois (ver item 8).
