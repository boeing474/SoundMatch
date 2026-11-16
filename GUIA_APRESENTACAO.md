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
   - Digitar um artista (ex.: BTS) e tocar em **"Buscar artista"**:
     - o app consulta o **iTunes**;
     - aparece um card com a capa, o nome oficial e o gênero ("K-pop"), com contorno na cor do perfil.
   - Escolher a situação e o instrumento nos chips (o `Chip` é subclasse de `CheckBox`).
   - Mostrar o "Passo X de 4" com a `ProgressBar`, e que o voltar do celular volta um passo.
   - Mostrar a frase que usa a resposta anterior ("Prazer, Pedro.") e o sol do rodapé subindo a cada passo (`moveSun`, com `animate().translationY`).
   - Avançar → *Intent explícita com `putExtra`* levando o `UserProfile`.
3. **Quiz**
   - Mostrar que a 1ª pergunta usa o perfil: músicas reais do artista (vindas do iTunes no passo do artista) para a situação escolhida.
   - Escolher a energia (ex.: Dançante) e explicar que ela escolhe a música dentro do estilo.
   - Responder: a `ProgressBar` avança, e o botão só habilita quando as 2 perguntas têm resposta.
   - (Opcional) Ligar o **Me surpreenda** sem marcar estilo: o app pede para marcar outro estilo (feedback visual).
4. **Resultado**
   - Mostrar a capa em destaque (contorno na cor do perfil), a música e o **% de match**.
   - Explicar a conta. Exemplo:
     - BTS = 3 votos para Pop;
     - Dynamite (gênero K-pop no iTunes) = mais 1 voto para Pop;
     - marcar Eletrônica = 1 voto para Eletrônica;
     - total: Pop tem 4 de 5 votos = 80%.
   - Mostrar o card **"Por que essa música?"** (processamento dos dados).
   - Tocar em **YouTube** ou **Spotify** → *Intent implícita `ACTION_VIEW`*. O Spotify abre direto no app; se ele não estiver instalado, abre no navegador.
   - Tocar em **"Compartilhar"** → *Intent implícita `ACTION_SEND`*.
   - Tocar em **"Curti"**: aparece a `ProgressBar` e depois a confirmação (**POST**).
5. **Swagger**
   - Abrir https://soundmatch-api-dnh5.onrender.com/docs e mostrar o `GET /recommendations` com o registro que acabou de ser salvo, o que prova a persistência no banco.
6. **Histórico**
   - Voltar ao início e abrir **"Minhas Descobertas"** (**GET**). Cada música aparece com a capa contornada na cor do perfil.
   - Tocar no **coração** para mudar a avaliação (**PUT**).
   - Tocar na **lixeira** para excluir, com confirmação em `AlertDialog` (**DELETE**).
7. **Código**
   - Abrir `SoundMatchEngine.kt` e explicar a votação.
   - Mostrar os 12 testes passando (`SoundMatchEngineTest`, botão Run).

---

## 3. Onde está cada requisito

Para achar rápido no Android Studio: Ctrl+Shift+F (busca no projeto) ou Ctrl+N (abrir classe).

| Requisito | Arquivo |
|---|---|
| 5 Activities | `ui/MainActivity.kt`, `UserProfileActivity.kt`, `SoundQuizActivity.kt`, `ResultActivity.kt`, `HistoryActivity.kt` (registradas no `AndroidManifest.xml`) |
| Intent explícita | `MainActivity.setupNavigation()`, `UserProfileActivity.openQuiz()`, `SoundQuizActivity.showResult()` |
| Intent implícita | `ResultActivity.openYoutube()` e `openSpotify()` (`ACTION_VIEW`), `shareProfile()` (`ACTION_SEND`) |
| Views (9 tipos) | Veja a lista abaixo |
| Feedback visual | erro no campo + `Toast` (Perfil), `ProgressBar` na busca do artista, botão desabilitado (Quiz), `ProgressBar` + `Toast` (Resultado e Histórico) |
| API REST + Swagger | `backend/main.py` (os `summary` e `tags` aparecem no `/docs`) |
| CRUD Retrofit | `network/SoundMatchApi.kt` + chamadas em `ResultActivity.saveRecommendation()` e `HistoryActivity` (`loadHistory`, `updateRating`, `deleteRecommendation`) |
| API externa (extra) | `network/ITunesApi.kt` + `UserProfileActivity.searchArtist()` e `ResultActivity.loadAlbumCover()` |
| Banco relacional | `backend/models.py` (`ForeignKey("users.id")` + `relationship`) |
| Organização | pacotes `ui`, `adapter`, `network`, `model`, `logic` |

Onde está cada tipo de View:
- `TextView`, `ImageView` e `Button`: `activity_main.xml`;
- `EditText` (`TextInputEditText`), `Chip` (subclasse de `CheckBox`), `CardView` e `ProgressBar`: `activity_user_profile.xml`;
- `RadioButton` (criados no Kotlin a partir de `item_quiz_option.xml`), `CheckBox` e `Switch`: `activity_sound_quiz.xml`;
- `RecyclerView`: `activity_history.xml`.

---

## 4. Fluxo de dados (do toque ao banco)

```
[Tela] ResultActivity (Curti / Não curti)
   │  RecommendationRequest.from(result, liked)     → objeto Kotlin
   ▼
[Retrofit] SoundMatchApi.createRecommendation()      → Gson converte em JSON
   │  POST http://localhost:8000/recommendations   (adb reverse leva até o PC)
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

## 5. Papel de cada arquivo

### Android – `com.example.soundmatch`

| Pacote / arquivo | Papel |
|---|---|
| `ui/MainActivity` | Tela inicial: animações e navegação para o Perfil e o Histórico |
| `ui/UserProfileActivity` | Perfil em 4 passos na mesma tela (troca a visibilidade dos blocos), busca o artista no iTunes e envia o `UserProfile` para o quiz |
| `ui/SoundQuizActivity` | Lê as respostas, controla a ProgressBar e chama o `SoundMatchEngine` |
| `ui/ResultActivity` | Exibe o resultado, busca a capa no iTunes, faz o POST na API e dispara as Intents implícitas |
| `ui/HistoryActivity` | GET, PUT (muda a avaliação) e DELETE (com confirmação em AlertDialog) |
| `adapter/HistoryAdapter` | Transforma cada registro da API em um card do RecyclerView (`ListAdapter` + `DiffUtil`) |
| `network/SoundMatchApi` | Interface Retrofit: um método por endpoint |
| `network/ITunesApi` | Busca na iTunes Search API (gênero e músicas do artista, capa) |
| `network/RetrofitClient` | Instâncias do Retrofit: a nossa API (`BASE_URL`) e o iTunes |
| `network/ImageLoader` | Baixa a capa em `Dispatchers.IO` e devolve o `Bitmap` |
| `network/CoverRepository` | Busca a capa de cada música do histórico no iTunes e guarda num cache, para não baixar de novo ao rolar a lista |
| `model/Genre` | Os 5 perfis: título, descrição, cor e palavras-chave para traduzir o gênero do iTunes |
| `model/Energy` | As 6 energias do quiz, cada uma com o nome, a frase de critério e o trecho da explicação |
| `model/Situation`, `Instrument` | Situação e instrumento do perfil (o instrumento sabe "em que prestar atenção") |
| `model/UserProfile`, `SoundResult` | Dados que passam entre as telas (`Serializable`) |
| `model/QuizAnswers`, `Song`, `ArtistSong` | Respostas do quiz, música do catálogo e música do artista favorito |
| `model/ApiModels`, `ITunesModels` | Espelho do JSON das APIs (`@SerializedName`) |
| `logic/SoundMatchEngine` | **Regra de negócio**: votos, perfil, Me surpreenda, % de match, escolha da música pela energia e explicação |
| `logic/SongCatalog` | 60 músicas: 12 por perfil, 2 de cada energia, cada uma destacando um instrumento |
| `logic/TrackPicker` | Filtra os resultados do iTunes: as músicas do artista para o quiz (sem versões ao vivo, remix ou repetidas) e a faixa certa para a capa |
| `logic/ArtistName` | Acerta o nome do artista vindo do iTunes: usa a grafia oficial ("bts" vira "BTS") e separa parcerias ("LUDMILLA & Caio Luccas") |

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

**Como o perfil é escolhido?**
Por votação entre 5 perfis:
- o gênero do artista favorito vale 3 votos;
- a música escolhida na 1ª pergunta vale 1, no gênero dela no iTunes;
- cada estilo marcado vale 1.

Ganha o mais votado. No empate, ganha o gênero do artista favorito, porque é a informação mais forte sobre o gosto da pessoa.

**Se a 1ª pergunta é sobre músicas do próprio artista, por que ela vota?**
Porque o iTunes informa o gênero de cada música, não só do artista. Para BTS dá sempre K-pop, mas para quem varia, como Anitta (Pop, Brasileira, Latina), a música escolhida mostra qual lado a pessoa curte. Ela também aparece na explicação ("você colocaria Dynamite pra tocar numa festa").

**O que a energia faz?**
Escolhe a música dentro do estilo. Cada estilo tem 2 músicas de cada uma das 6 energias, então sempre existe uma música da energia pedida. Cada energia tem uma frase de critério (`Energy.hint`, ex.: "triste, de dor de cotovelo"), que é a mesma que aparece no quiz e a que usei para classificar as músicas.

**Como funciona o Me surpreenda?**
Entre os estilos marcados, o motor pega o mais votado que não seja o de sempre (`surpriseGenre`). Como esse estilo não foi o mais votado, o % não faz sentido, e o resultado mostra "Fora da sua zona de conforto". A API guarda `surprise = true`, e o histórico mostra "· surpresa" no lugar do %.

**O que é o % de match?**
É a parte dos votos que foi para o perfil: votos do perfil ÷ total × 100, limitado entre 10 e 100. Se tudo apontou para Pop, dá 100%. Se a pessoa marcou vários outros estilos, o gosto está mais espalhado e o número cai.

**Por que o artista favorito vale 3 votos?**
Porque é o dado mais concreto que a pessoa dá: é o que ela realmente ouve. Com peso 3, quem coloca BTS recebe uma recomendação do universo do pop e do K-pop, a não ser que a música escolhida e os estilos marcados apontem para outro lado.

**E se o iTunes não achar o artista ou não houver internet?**
O `artistGenre` fica `null`, o artista não vota e a lista de músicas dele vem vazia. A 1ª pergunta vira a pergunta reserva (5 músicas de 5 estilos, a opção N vota no perfil N), e o app continua funcionando (isso também está nos testes).

**Como o gênero do iTunes vira um dos 5 perfis?**
`Genre.fromITunes` procura palavras-chave no nome do gênero. Exemplos: "K-pop" → Pop; "Hip-Hop/Rap" → Hip-hop; "Sertanejo" → Brasileira.

A busca começa pela Música Brasileira e deixa o Pop por último, porque "pop" aparece em muitos nomes (ex.: "Pop Brasileiro").

**Como garante que a música é uma "descoberta"?**
O `pickSong` fica com as músicas da energia pedida e tira as do artista favorito. Quem toca recebe a música em que o seu instrumento se destaca. Os outros recebem uma escolha fixa pelo nome, de modo que pessoas diferentes ganham músicas diferentes, mas a mesma pessoa sempre recebe a mesma.

**Onde a API está rodando?**
No Render, um serviço de hospedagem na nuvem. Ele baixa o código do GitHub (pasta `backend`), instala o `requirements.txt` e roda o `uvicorn` na porta que ele escolhe (`$PORT`). O app acessa por HTTPS. No plano grátis, o disco não é permanente, então o SQLite volta vazio quando a API reinicia. Para guardar os dados para sempre, eu trocaria por um PostgreSQL gerenciado, mudando só a `DATABASE_URL` no `database.py`.

**Como o celular acessava a API no PC usando `localhost`?** (plano B)
No celular, `localhost` é o próprio celular. O `adb reverse tcp:8000 tcp:8000` cria um túnel pelo cabo USB: o que chega na porta 8000 do celular vai para a porta 8000 do PC, onde a API está. No emulador daria para usar `10.0.2.2`, um endereço especial que aponta para o computador.

**De onde vem a capa do álbum?**
Da iTunes Search API, uma API pública da Apple. Uso o mesmo Retrofit com outra `baseUrl` (`RetrofitClient.itunes`). A imagem é baixada em `Dispatchers.IO`, porque o Android não permite usar a rede na thread da tela. Se der erro (sem internet), o app mantém o disco de vinil.

**Como o botão do Spotify funciona?**
Ele tenta abrir `spotify:search:<música>`, que só o app do Spotify entende. Se o app não estiver instalado, o Android lança `ActivityNotFoundException`, e eu abro a versão web (`open.spotify.com/search/...`).

**Por que o perfil é uma tela só com 4 passos, e não 4 Activities?**
São 4 perguntas curtas do mesmo formulário. Os 4 blocos ficam no mesmo XML, e o Kotlin mostra só o passo atual (`showStep`). Assim os dados ficam numa Activity só, e o voltar do celular volta um passo (`OnBackPressedCallback`).

**Por que as colunas se chamam `score`, `archetype` e `subgenres`?**
Os nomes vêm da primeira versão do projeto. Hoje:
- `score` guarda o % de match;
- `archetype` guarda o perfil;
- `subgenres` guarda os outros estilos marcados.
- `surprise` (a mais nova) diz se a música veio do Me surpreenda.

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
