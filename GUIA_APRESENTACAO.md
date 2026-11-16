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
     - aparece um card com a capa, o nome oficial e o gênero ("K-pop").
   - Escolher a situação e o instrumento nos chips (o `Chip` é subclasse de `CheckBox`).
   - Mostrar o "Passo X de 4" com a `ProgressBar`, e que o voltar do celular volta um passo.
   - Avançar → *Intent explícita com `putExtra`* levando o `UserProfile`.
3. **Quiz**
   - Mostrar que as perguntas citam o perfil: a situação na 1ª e o artista favorito na 2ª.
   - Explicar que cada pergunta tem 5 opções, uma por perfil.
   - Responder: a `ProgressBar` avança, e o botão só habilita quando as 3 perguntas têm resposta.
4. **Resultado**
   - Mostrar a capa em destaque (contorno na cor do perfil), a música e o **% de match**.
   - Explicar a conta. Exemplo:
     - BTS = 3 votos para Pop;
     - 3 respostas = mais 3 votos para Pop;
     - marcar Eletrônica = 1 voto para Eletrônica;
     - total: Pop tem 6 de 7 votos = 86%.
   - Mostrar o card **"Por que essa música?"** (processamento dos dados).
   - Tocar em **YouTube** ou **Spotify** → *Intent implícita `ACTION_VIEW`*. O Spotify abre direto no app; se ele não estiver instalado, abre no navegador.
   - Tocar em **"Compartilhar"** → *Intent implícita `ACTION_SEND`*.
   - Tocar em **"Curti"**: aparece a `ProgressBar` e depois a confirmação (**POST**).
5. **Swagger**
   - Abrir https://soundmatch-api-dnh5.onrender.com/docs e mostrar o `GET /recommendations` com o registro que acabou de ser salvo, o que prova a persistência no banco.
6. **Histórico**
   - Voltar ao início e abrir **"Minhas Descobertas"** (**GET**). Cada card tem a faixa na cor do perfil.
   - **Mudar para Não curti** (**PUT**).
   - **Excluir**, com confirmação em `AlertDialog` (**DELETE**).
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
- `RadioButton` e `CheckBox`: `activity_sound_quiz.xml`;
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
| `network/ITunesApi` | Busca na iTunes Search API (gênero do artista e capa) |
| `network/RetrofitClient` | Instâncias do Retrofit: a nossa API (`BASE_URL`) e o iTunes |
| `network/ImageLoader` | Baixa a capa em `Dispatchers.IO` e devolve o `Bitmap` |
| `model/Genre` | Os 5 perfis: título, descrição, cor e palavras-chave para traduzir o gênero do iTunes |
| `model/Situation`, `Instrument` | Situação e instrumento do perfil (o instrumento sabe "em que prestar atenção") |
| `model/UserProfile`, `SoundResult` | Dados que passam entre as telas (`Serializable`) |
| `model/QuizAnswers`, `Song` | Respostas do quiz e música do catálogo |
| `model/ApiModels`, `ITunesModels` | Espelho do JSON das APIs (`@SerializedName`) |
| `logic/SoundMatchEngine` | **Regra de negócio**: votos, perfil, % de match, escolha da música e explicação |
| `logic/SongCatalog` | 25 músicas: 5 por perfil, cada uma destacando um instrumento |
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
- cada pergunta do quiz vale 1 (a opção N vota no perfil N);
- cada estilo marcado vale 1.

Ganha o mais votado. No empate, ganha o gênero do artista favorito, porque é a informação mais forte sobre o gosto da pessoa.

**O que é o % de match?**
É a parte dos votos que foi para o perfil vencedor: votos do vencedor ÷ total × 100, limitado entre 10 e 100. Se tudo apontou para Pop, dá 100%. Se as respostas se dividiram, o número cai.

**Por que o artista favorito vale 3 votos?**
Porque é o dado mais concreto que a pessoa dá: é o que ela realmente ouve. Com peso 3, quem coloca BTS recebe uma recomendação do universo do pop e do K-pop, a não ser que todas as respostas do quiz apontem para outro lado.

**E se o iTunes não achar o artista ou não houver internet?**
O `artistGenre` fica `null` e o artista não vota. O perfil sai só do quiz, e o app continua funcionando (isso também está nos testes).

**Como o gênero do iTunes vira um dos 5 perfis?**
`Genre.fromITunes` procura palavras-chave no nome do gênero. Exemplos: "K-pop" → Pop; "Hip-Hop/Rap" → Hip-hop; "Sertanejo" → Brasileira.

A busca começa pela Música Brasileira e deixa o Pop por último, porque "pop" aparece em muitos nomes (ex.: "Pop Brasileiro").

**Como garante que a música é uma "descoberta"?**
O `pickSong` tira do catálogo as músicas do artista favorito. Quem toca recebe a música em que o seu instrumento se destaca. Os outros recebem uma escolha fixa pelo nome, de modo que pessoas diferentes ganham músicas diferentes, mas a mesma pessoa sempre recebe a mesma.

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
