# Guia de Apresentação – SoundMatch (AP2)

A apresentação oral e o domínio técnico valem **2,5 pontos**. Este guia traz:
- o roteiro da demonstração;
- onde está cada requisito no código;
- como os dados fluem pelo sistema;
- respostas para as perguntas mais prováveis.

---

## 1. Checklist antes de apresentar

A API está no Render (https://soundmatch-api-dnh5.onrender.com). Não precisa de notebook nem de cabo.

- [ ] Celular com internet (Wi-Fi ou 4G).
- [ ] **Uns 2 minutos antes**, abrir https://soundmatch-api-dnh5.onrender.com/docs no navegador do celular. No plano grátis, a API dorme depois de 15 min sem uso e leva até 1 min para acordar.
- [ ] Lembrar que o banco do Render começa vazio sempre que a API reinicia. No roteiro, salvar uma descoberta ao vivo (Curti) **antes** de abrir o histórico.
- [ ] Mostrar o código pelo GitHub (repositório público), se não estiver com o notebook.

**Plano B (com o notebook na tomada):**
1. Subir a API local (`cd backend`, `source .venv/bin/activate`, `uvicorn main:app --host 0.0.0.0 --port 8000`).
2. Trocar o `BASE_URL` em `RetrofitClient.kt` para `http://localhost:8000/`.
3. Rodar o app pelo Android Studio e depois `adb reverse tcp:8000 tcp:8000`.

---

## 2. Roteiro da demonstração (5–7 min)

1. **Tela inicial**
   - Mostrar a identidade visual: o disco de vinil gira (`ObjectAnimator`) e o título pulsa entre magenta e ciano (`ValueAnimator`).
   - Tocar em **"Descobrir Meu Som"** → *Intent explícita*.
2. **Perfil (uma pergunta por vez)**
   - Tentar continuar com o nome vazio: o campo fica em erro (**feedback visual**).
   - Digitar um artista (ex.: Metallica) e tocar em **"Buscar artista"**:
     - o app consulta o **Deezer** (nome oficial, foto e artistas parecidos) e o **iTunes** (estilo);
     - aparece um card com a foto, o nome oficial e o estilo ("Metal"), com contorno na cor do estilo.
   - Escolher a situação nos chips (o `Chip` é subclasse de `CheckBox`).
   - Mostrar o "Passo X de 3" com a `ProgressBar`, e que o voltar do celular volta um passo.
   - Mostrar a frase que usa a resposta anterior ("Prazer, Pedro.") e o sol do rodapé subindo a cada passo (`moveSun`, com `animate().translationY`).
   - Avançar → *Intent explícita com `putExtra`* levando o `UserProfile` (com os artistas parecidos dentro).
3. **Quiz**
   - Mostrar que a 1ª pergunta usa o perfil: os parecidos com o artista, cada um num chip com foto. Marcar um ou dois: o contador muda ("2 conhecidos · 6 na disputa").
   - Escolher o clima (ex.: Porradeira) e hit ou pérola: a `ProgressBar` avança, e o botão só habilita com as 2 respostas.
   - (Opcional) Mostrar o Switch **Quero me arriscar**: a busca passa a ser nos parecidos dos parecidos.
   - Tocar em **"Ver Minha Música"**: aparece a `ProgressBar` enquanto o app busca as músicas no Deezer e as características no ReccoBeats.
4. **Resultado**
   - Mostrar a capa em destaque, a música e o card **"Parecido com Metallica"** com o **% de match**.
   - Explicar a conta com um exemplo:
     - Porradeira procura energia 0,95 e som acústico 0,05;
     - para uma música com energia 0,93 e acústico 0,03, as distâncias são 0,02 e 0,02;
     - a média é 0,02, então o match é 98%.
   - Mostrar o card **"Por que essa música?"** (processamento dos dados).
   - Tocar em **YouTube** ou **Spotify** → *Intent implícita `ACTION_VIEW`*. O Spotify abre direto no app; se ele não estiver instalado, abre no navegador.
   - Tocar em **"Compartilhar"** → *Intent implícita `ACTION_SEND`*.
   - Tocar em **"Curti"**: aparece a `ProgressBar` e depois a confirmação (**POST**).
5. **Swagger**
   - Abrir https://soundmatch-api-dnh5.onrender.com/docs e mostrar o `GET /recommendations` com o registro que acabou de ser salvo, o que prova a persistência no banco.
6. **Histórico**
   - Voltar ao início e abrir **"Minhas Descobertas"** (**GET**). Cada música aparece com a capa e a linha "Parecido com Metallica · 98%".
   - Tocar no **coração** para mudar a avaliação (**PUT**).
   - Tocar na **lixeira** para excluir, com confirmação em `AlertDialog` (**DELETE**).
7. **Código**
   - Abrir `logic/DiscoveryEngine.kt` e explicar `recommend`: quem entra, hit ou pérola, o alvo do clima e o % de match.
   - Mostrar os 37 testes passando (`DiscoveryEngineTest` e os outros, botão Run).

---

## 3. Onde está cada requisito

Para achar rápido no Android Studio: Ctrl+Shift+F (busca no projeto) ou Ctrl+N (abrir classe).

| Requisito | Arquivo |
|---|---|
| 5 Activities | `ui/MainActivity.kt`, `UserProfileActivity.kt`, `SoundQuizActivity.kt`, `ResultActivity.kt`, `HistoryActivity.kt` (registradas no `AndroidManifest.xml`) |
| Intent explícita | `MainActivity.setupNavigation()`, `UserProfileActivity.openQuiz()`, `SoundQuizActivity.openResult()` |
| Intent implícita | `ResultActivity.openYoutube()` e `openSpotify()` (`ACTION_VIEW`), `shareProfile()` (`ACTION_SEND`) |
| Views (10 tipos) | Veja a lista abaixo |
| Feedback visual | erro no campo + `Toast` (Perfil), `ProgressBar` na busca do artista, contador de conhecidos, botão desabilitado e `ProgressBar` na busca das músicas (Quiz), `ProgressBar` + `Toast` (Resultado e Histórico) |
| API REST + Swagger | `backend/main.py` (os `summary` e `tags` aparecem no `/docs`) |
| CRUD Retrofit | `network/SoundMatchApi.kt` + chamadas em `ResultActivity.saveRecommendation()` e `HistoryActivity` (`loadHistory`, `updateRating`, `deleteRecommendation`) |
| APIs externas (extra) | `network/DeezerApi.kt`, `ReccoBeatsApi.kt` e `ITunesApi.kt`, usadas por `network/DiscoveryRepository.kt`, `UserProfileActivity.searchArtist()` e `SoundQuizActivity.discover()` |
| Banco relacional | `backend/models.py` (`ForeignKey("users.id")` + `relationship`) |
| Organização | pacotes `ui`, `adapter`, `network`, `model`, `logic` |

Onde está cada tipo de View:
- `TextView`, `ImageView` e `Button`: `activity_main.xml`;
- `EditText` (`TextInputEditText`), `Chip` (subclasse de `CheckBox`), `CardView` e `ProgressBar`: `activity_user_profile.xml`;
- `RadioButton` (criados no Kotlin a partir de `item_quiz_option.xml`), `Chip` com foto (`item_artist_chip.xml`), `CheckBox` e `Switch`: `activity_sound_quiz.xml`;
- `RecyclerView`: `activity_history.xml`.

---

## 4. Fluxo de dados (do toque ao banco)

```
[Tela] ResultActivity (Curti / Não curti)
   │  RecommendationRequest.from(result, liked, cardTitle, cardDetail)   → objeto Kotlin
   ▼
[Retrofit] SoundMatchApi.createRecommendation()      → Gson converte em JSON
   │  POST https://soundmatch-api-dnh5.onrender.com/recommendations
   ▼
[FastAPI] main.py create_recommendation()
   │  schemas.RecommendationCreate valida o JSON (ex.: score 10–100)
   │  procura o usuário pelo nome → cria se não existir (tabela users)
   │  cria a Recommendation ligada ao usuário (user_id = FK)
   ▼
[SQLite] soundmatch.db  ← db.commit()
   ▼
Resposta 201 com o JSON salvo → Retrofit converte em RecommendationResponse → confirmação na tela
```

---

E o caminho da recomendação, antes disso:

```
[Perfil] searchArtist()            → Deezer: search/artist + artist/{id}/related   (iTunes: estilo)
   ▼  UserProfile (com relatedArtists) vai para o quiz pelo putExtra
[Quiz] discover(answers)
   │  DiscoveryRepository.candidatesFor()  → Deezer: artist/{id}/top + track/{id} (ISRC)
   │                                       → ReccoBeats: audio-features?ids=<ISRCs>
   ▼  lista de Candidate (música + características)
[Motor] DiscoveryEngine.recommend() → SoundResult   (null → SoundMatchEngine, o reserva)
   ▼  putExtra
[Resultado] ResultActivity
```

---

## 5. Papel de cada arquivo

### Android – `com.example.soundmatch`

| Pacote / arquivo | Papel |
|---|---|
| `ui/MainActivity` | Tela inicial: animações e navegação para o Perfil e o Histórico |
| `ui/UserProfileActivity` | Perfil em 3 passos na mesma tela (troca a visibilidade dos blocos), busca o artista no Deezer e no iTunes e envia o `UserProfile` para o quiz |
| `ui/SoundQuizActivity` | Monta os chips dos parecidos, lê as respostas, busca as candidatas e chama o `DiscoveryEngine` (ou o motor reserva) |
| `ui/ResultActivity` | Exibe o resultado e a capa, faz o POST na API e dispara as Intents implícitas |
| `ui/HistoryActivity` | GET, PUT (muda a avaliação) e DELETE (com confirmação em AlertDialog) |
| `adapter/HistoryAdapter` | Transforma cada registro da API em um card do RecyclerView (`ListAdapter` + `DiffUtil`) |
| `network/SoundMatchApi` | Interface Retrofit da nossa API: um método por endpoint |
| `network/DeezerApi` | Interface Retrofit do Deezer: busca do artista, parecidos, mais tocadas e a música pelo id |
| `network/ReccoBeatsApi` | Interface Retrofit do ReccoBeats: características do som, por ISRC |
| `network/ITunesApi` | Busca na iTunes Search API (estilo do artista e capa das músicas do catálogo fixo) |
| `network/DiscoveryRepository` | Junta Deezer e ReccoBeats: acha o artista, os parecidos e as músicas candidatas, em paralelo com `async` |
| `network/NetworkCall` | `networkOrNull`: uma falha de rede vira `null`, sem repetir `try/catch` em todo lugar |
| `network/RetrofitClient` | Instâncias do Retrofit: a nossa API (`BASE_URL`), Deezer, ReccoBeats e iTunes |
| `network/ImageLoader` | Baixa uma imagem em `Dispatchers.IO` e devolve o `Bitmap` |
| `network/CoverRepository` | Busca a capa de cada música do histórico no iTunes e guarda num cache, para não baixar de novo ao rolar a lista |
| `model/Energy` | Os 6 climas do quiz, cada um com o nome, a frase de critério e o trecho da explicação |
| `model/Popularity` | Hit do momento ou pérola escondida |
| `model/Situation` | Quando a pessoa mais ouve música |
| `model/Genre` | Os 5 estilos: título, cor e palavras-chave para traduzir o gênero do iTunes |
| `model/UserProfile`, `SoundResult` | Dados que passam entre as telas (`Serializable`) |
| `model/QuizAnswers`, `Song` | Respostas do quiz e a música recomendada |
| `model/DeezerModels`, `AudioFeatures`, `Candidate` | Respostas do Deezer e do ReccoBeats; `Candidate` é uma música do Deezer com as características (ou `null`) |
| `model/ApiModels`, `ITunesModels` | Espelho do JSON da nossa API e do iTunes (`@SerializedName`) |
| `logic/DiscoveryEngine` | **Regra de negócio**: quem entra na disputa, hit ou pérola, alvo de cada clima, % de match e explicação |
| `logic/DiscoveryRules` | Regras menores: escolher o artista certo entre homônimos, tirar os conhecidos, decidir quando ir aos parecidos dos parecidos, ligar música e características pelo ISRC |
| `logic/SoundMatchEngine` | Motor reserva: votação por estilo e música do catálogo fixo |
| `logic/SongCatalog` | Catálogo reserva: 60 músicas, 12 por estilo, 2 de cada clima |
| `logic/TrackPicker` | Escolhe a faixa certa entre os resultados do iTunes (artista certo, versão de estúdio) |
| `logic/ArtistName` | Acerta o nome do artista vindo do iTunes, quando o Deezer não respondeu |

### Recursos (`res/`)

| Pasta / arquivo | Papel |
|---|---|
| `values/colors.xml` | Paleta inteira, incluindo a cor de cada perfil (`genre_*`) |
| `values/themes.xml` | Tema e estilos reutilizáveis: títulos, botões, cards, campos, chips |
| `values/strings.xml` | Todos os textos (facilita traduzir e evita texto fixo no código) |
| `color/chip_*.xml` | Cores do chip selecionado e não selecionado |
| `font/monoton_regular.ttf` | Fonte retrô dos títulos |
| `drawable/` | Vetores: fundo synthwave em camadas (céu, sol, chão), disco de vinil, logos do Spotify e do YouTube (Simple Icons, CC0), ícone do app |
| `xml/network_security_config.xml` | Libera HTTP só para os endereços locais da API |

### Backend

| Arquivo | Papel |
|---|---|
| `database.py` | Conexão com o SQLite e sessão por requisição (`get_db`) |
| `models.py` | Tabelas `users` e `recommendations` (relação 1:N) |
| `schemas.py` | Formato e validação do JSON de entrada e saída (Pydantic) |
| `main.py` | Os endpoints e a criação das tabelas |

---

## 6. Perguntas prováveis (e respostas)

**Como a música é escolhida?**
Em quatro passos, todos em `DiscoveryEngine.recommend`:
1. entram as 4 músicas mais tocadas de cada artista parecido que a pessoa não marcou como conhecido (`newToUser` tira o favorito, os conhecidos e as repetidas);
2. só ficam as que têm as características do som;
3. `popularityHalf` ordena pela popularidade e corta no meio: hit fica com a metade de cima, pérola com a de baixo;
4. ganha a mais perto do alvo do clima (`closeness`), com a situação desempatando (`situationBonus`).

**De onde vêm os artistas parecidos?**
Do Deezer (`artist/{id}/related`). A busca acontece no passo do artista, no perfil, e a lista vai para o quiz dentro do `UserProfile`.

**Como o app sabe se uma música é calma ou pesada?**
Pelo ReccoBeats, que devolve números de 0 a 1 para cada gravação: `energy` (energia), `valence` (alegria), `danceability` (o quanto é dançante) e `acousticness` (o quanto é acústica). O app pede esses números pelo ISRC, o código internacional da gravação, que vem do Deezer.

**O que é o alvo de um clima?**
É o que o clima procura nesses números (`targetFor`). Porradeira quer energia 0,95 e som acústico 0,05; Sofrência quer alegria 0,15; Viajada quer música pouco dançante e longa. O que não está no alvo não conta (para Sofrência, tanto faz a energia).

**O que é o % de match?**
É o quanto a música chega perto do alvo. O app mede a distância em cada número do alvo, tira a média e faz 100 menos isso (`closeness` e `matchPercent`). Se a música está em cima do alvo, dá 100%. Quem pede Sofrência entre artistas de forró recebe por volta de 60%, porque a música mais triste que existe ali ainda é alegre.

**Para que serve a situação (academia, festa...)?**
Só para desempatar (`situationBonus`, com peso 0,1): na academia ganha a mais enérgica, na festa a mais dançante, estudando ou relaxando a mais calma. Ela não entra no % de match.

**Como funciona o hit ou pérola?**
O Deezer dá um `rank` de popularidade para cada música. As candidatas são ordenadas por ele e cortadas no meio (`popularityHalf`).

**Como funciona o Quero me arriscar?**
A busca vai um passo além: em vez dos parecidos com o artista, entram os parecidos dos 3 primeiros da lista, fora os que já eram parecidos diretos (`DiscoveryRepository.fartherArtists`). O mesmo acontece se a pessoa marcar todos como conhecidos (`DiscoveryRules.goesFarther`).

**Por que só 8 artistas e 4 músicas de cada?**
O Deezer aceita 50 chamadas a cada 5 segundos. Cada artista custa 1 chamada (as mais tocadas) e cada música mais 1 (para trazer o ISRC): 8 + 32 = 40. Se o limite estourar mesmo assim, a música segue sem ISRC e fica fora da disputa, sem derrubar o app (`withIsrc`).

**Por que as chamadas usam `async` e `awaitAll`?**
Para pedir as músicas de todos os artistas ao mesmo tempo, em vez de uma depois da outra. O `coroutineScope` espera todas terminarem antes de seguir.

**E se o Deezer não achar o artista ou não houver internet?**
A lista de parecidos vem vazia e o quiz muda para o modo reserva: só o clima, os "outros estilos" e o switch. A música sai do catálogo fixo pelo `SoundMatchEngine`: o estilo do artista vale 3 votos, cada estilo marcado vale 1, e o clima escolhe a música dentro do estilo vencedor.

**E se o ReccoBeats estiver fora do ar?**
Nenhuma candidata terá as características, o `DiscoveryEngine.recommend` devolve `null` e a tela usa o motor reserva. Preferi isso a mostrar um % inventado.

**Como garante que a música é uma "descoberta"?**
O `newToUser` tira as músicas do artista favorito e dos artistas marcados como conhecidos. No motor reserva, o `pickSong` também tira o artista favorito.

**Tem vários artistas com o mesmo nome no Deezer. Como escolhe?**
`DiscoveryRules.pickArtist` compara o nome sem acento e sem maiúscula e, entre os iguais ao digitado, fica com o de mais fãs. Há vários "BTS", por exemplo.

**Onde a API está rodando?**
No Render, um serviço de hospedagem na nuvem. Ele baixa o código do GitHub (pasta `backend`), instala o `requirements.txt` e roda o `uvicorn` na porta que ele escolhe (`$PORT`). O app acessa por HTTPS. No plano grátis, o disco não é permanente, então o SQLite volta vazio quando a API reinicia. Para guardar os dados para sempre, eu trocaria por um PostgreSQL gerenciado, mudando só a `DATABASE_URL` no `database.py`.

**Como o celular acessava a API no PC usando `localhost`?** (plano B)
No celular, `localhost` é o próprio celular. O `adb reverse tcp:8000 tcp:8000` cria um túnel pelo cabo USB: o que chega na porta 8000 do celular vai para a porta 8000 do PC, onde a API está. No emulador daria para usar `10.0.2.2`, um endereço especial que aponta para o computador.

**De onde vem a capa do álbum?**
Do próprio Deezer, que manda o endereço da capa junto com a música. Para as músicas do catálogo fixo e para o histórico, a capa vem da iTunes Search API. Uso o mesmo Retrofit com outra `baseUrl` para cada serviço (`RetrofitClient`). A imagem é baixada em `Dispatchers.IO`, porque o Android não permite usar a rede na thread da tela. Se der erro (sem internet), o app mantém o disco de vinil.

**Como o botão do Spotify funciona?**
Ele tenta abrir `spotify:search:<música>`, que só o app do Spotify entende. Se o app não estiver instalado, o Android lança `ActivityNotFoundException`, e eu abro a versão web (`open.spotify.com/search/...`).

**Por que o perfil é uma tela só com 3 passos, e não 3 Activities?**
São 3 perguntas curtas do mesmo formulário. Os 3 blocos ficam no mesmo XML, e o Kotlin mostra só o passo atual (`showStep`). Assim os dados ficam numa Activity só, e o voltar do celular volta um passo (`OnBackPressedCallback`).

**Por que as colunas se chamam `score`, `archetype` e `subgenres`?**
Os nomes vêm da primeira versão do projeto. Hoje:
- `score` guarda o % de match;
- `archetype` guarda o título do card ("Parecido com Metallica");
- `subgenres` guarda a linha de detalhe do card ("Porradeira · Pérola escondida");
- `surprise` diz se a música saiu do estilo de sempre no motor reserva;
- `plays_instrument` ficou da época em que o perfil perguntava sobre instrumento e hoje vai sempre `false`.

Mantive os nomes para não quebrar a API. Isso está comentado em `model/ApiModels.kt`.

**Por que precisou do `network_security_config.xml`?**
Desde o Android 9, HTTP sem criptografia é bloqueado por padrão. A configuração libera HTTP apenas para os endereços locais da API.

**O que é `suspend` / `lifecycleScope.launch`?**
São coroutines. A chamada de rede roda fora da thread principal, então a tela não trava. O `lifecycleScope` cancela a chamada automaticamente se a tela for fechada.

**Como os dados passam de uma tela para outra?**
Com `Intent.putExtra`. `UserProfile` e `SoundResult` são `Serializable`, e são lidos com `IntentCompat.getSerializableExtra`. As chaves (`EXTRA_PROFILE`, `EXTRA_RESULT`) ficam no `companion object` da tela que recebe.

**Qual a diferença entre Intent explícita e implícita?**
- Na explícita, eu digo **qual** Activity abrir (`Intent(this, ResultActivity::class.java)`).
- Na implícita, eu digo **o que** quero fazer (`ACTION_VIEW`, `ACTION_SEND`), e o Android escolhe o app.

**Como as tabelas se relacionam?**
`recommendations.user_id` é uma `ForeignKey` para `users.id`, ou seja, 1 usuário tem N recomendações. No POST, a API procura o usuário pelo nome: reaproveita se existir e cria se não existir.

**Por que separar o pacote `logic`?**
Para separar responsabilidades: a regra de negócio não depende de tela. Por isso dá para testá-la com JUnit, sem emulador.

**Por que `ListAdapter` + `DiffUtil` no RecyclerView?**
Quando a lista muda (após editar ou excluir), o `DiffUtil` calcula só o que mudou e atualiza apenas esses itens, em vez de redesenhar tudo.

**Por que ViewBinding em vez de `findViewById`?**
O ViewBinding gera uma classe por layout com todas as Views tipadas. É mais seguro (sem `null` nem cast errado) e o código fica menor.
