# Guia de Apresentação – SoundMatch (AP2)

A apresentação oral e o domínio técnico valem **2,5 pontos**. Este guia traz:
- o roteiro da demonstração;
- onde está cada requisito no código;
- como os dados fluem pelo sistema;
- respostas para as perguntas mais prováveis.

---

## 1. Checklist antes de apresentar

- [ ] Apagar o `backend/soundmatch.db` antigo, se ele for de uma versão anterior.
- [ ] Subir a API:
  ```bash
  cd backend
  source .venv/bin/activate
  uvicorn main:app --host 0.0.0.0 --port 8000
  ```
- [ ] Abrir http://localhost:8000/docs no navegador (Swagger), para mostrar depois.
- [ ] Ligar o celular no cabo (Depuração USB ativa) e rodar `adb reverse tcp:8000 tcp:8000`. Sem isso, o app não alcança a API. Se o cabo desconectar, rodar de novo.
- [ ] Celular com internet (Wi-Fi ou 4G), para carregar a capa do álbum.
- [ ] Fazer **um** cadastro de teste antes, para o histórico não aparecer vazio.

---

## 2. Roteiro da demonstração (5–7 min)

1. **Tela inicial**
   - Mostrar a identidade visual: o disco de vinil gira (`ObjectAnimator`) e o título pulsa (`ValueAnimator`).
   - Tocar em **"Descobrir Meu Som"** → *Intent explícita*.
2. **Perfil**
   - Tentar avançar com um campo vazio: o campo fica em erro e aparece um `Toast` (**feedback visual**).
   - Escolher a situação nos chips (o `Chip` é subclasse de `CheckBox`) e ligar o `Switch`: aparece o `Spinner` de instrumento.
   - Avançar → *Intent explícita com `putExtra`* levando o `UserProfile`.
3. **Quiz**
   - Mostrar que as perguntas citam o perfil: a situação na 1ª e o artista favorito na 2ª.
   - Responder uma pergunta por vez: a `ProgressBar` avança.
   - O botão só habilita quando as 3 perguntas têm resposta.
4. **Resultado**
   - Mostrar a capa em destaque (contorno na cor do perfil), o score e o texto que explica que ele vai de raiz a experimental.
   - Mostrar o card **"Por que essa música?"** (processamento dos dados).
   - Mostrar a capa do álbum, que veio da **iTunes Search API** (uma API externa, chamada com o mesmo Retrofit).
   - Tocar em **"YouTube"** ou **"Spotify"** → *Intent implícita `ACTION_VIEW`*. O Spotify abre direto no app; se ele não estiver instalado, abre no navegador.
   - Tocar em **"Compartilhar"** → *Intent implícita `ACTION_SEND`*.
   - Tocar em **"Curti"**: aparece a `ProgressBar` e depois "Salva em Minhas Descobertas como Curti" (**POST**).
5. **Swagger**
   - Mostrar o `GET /recommendations` no navegador com o registro que acabou de ser salvo, o que prova a persistência no banco.
6. **Histórico**
   - Voltar ao início e abrir **"Minhas Descobertas"** (**GET**).
   - **Mudar para Não curti** (**PUT**).
   - **Excluir**, com confirmação (**DELETE**).
7. **Código**
   - Abrir `SoundMatchEngine.kt` e explicar a fórmula.
   - Mostrar os testes passando (`SoundMatchEngineTest`, botão ▶).

---

## 3. Onde está cada requisito

Para achar rápido no Android Studio: Ctrl+Shift+F (busca no projeto) ou Ctrl+N (abrir classe).

| Requisito | Arquivo |
|---|---|
| 5 Activities | `ui/MainActivity.kt`, `UserProfileActivity.kt`, `SoundQuizActivity.kt`, `ResultActivity.kt`, `HistoryActivity.kt` (registradas no `AndroidManifest.xml`) |
| Intent explícita | `MainActivity.setupNavigation()`, `UserProfileActivity.openQuiz()`, `SoundQuizActivity.showResult()` |
| Intent implícita | `ResultActivity.openYoutube()` e `openSpotify()` (`ACTION_VIEW`), `shareProfile()` (`ACTION_SEND`) |
| Views | `TextView`/`ImageView`/`Button`: `activity_main.xml` · `EditText`/`Spinner`/`Switch`/`Chip` (subclasse de CheckBox): `activity_user_profile.xml` · `RadioButton`/`CheckBox`/`ProgressBar`/`CardView`: `activity_sound_quiz.xml` · `RecyclerView`: `activity_history.xml` |
| Feedback visual | erro no campo + `Toast` (Perfil), botão desabilitado (Quiz), `ProgressBar` + `Toast` (Resultado e Histórico) |
| API REST + Swagger | `backend/main.py` (os `summary` e `tags` aparecem no `/docs`) |
| CRUD Retrofit | `network/SoundMatchApi.kt` + chamadas em `ResultActivity.saveRecommendation()` e `HistoryActivity` |
| API externa (extra) | `network/ITunesApi.kt` + `ResultActivity.loadAlbumCover()` |
| Banco relacional | `backend/models.py` (`ForeignKey("users.id")` + `relationship`) |
| Organização | pacotes `ui`, `adapter`, `network`, `model`, `logic` |

---

## 4. Fluxo de dados (do toque ao banco)

```
[Tela] ResultActivity
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
Resposta 201 com o JSON salvo → Retrofit converte em RecommendationResponse → Toast "salvo!"
```

---

## 5. Papel de cada arquivo

### Android – `com.example.soundmatch`

| Pacote / arquivo | Papel |
|---|---|
| `ui/MainActivity` | Tela inicial: animações e navegação para o Perfil e o Histórico |
| `ui/UserProfileActivity` | Coleta e valida o perfil e envia o `UserProfile` para o quiz |
| `ui/SoundQuizActivity` | Lê as respostas, controla a ProgressBar e chama o `SoundMatchEngine` |
| `ui/ResultActivity` | Exibe o resultado, busca a capa no iTunes, faz o POST na API e dispara as Intents implícitas |
| `ui/HistoryActivity` | GET, PUT (muda a avaliação) e DELETE (com confirmação em AlertDialog) |
| `adapter/HistoryAdapter` | Transforma cada registro da API em um card do RecyclerView (`ListAdapter` + `DiffUtil`) |
| `network/SoundMatchApi` | Interface Retrofit: um método por endpoint |
| `network/RetrofitClient` | Instância única do Retrofit com a `BASE_URL` |
| `model/UserProfile`, `SoundResult` | Dados que passam entre as telas (`Serializable`) |
| `model/Archetype` | Os 4 perfis: título, descrição, cor e faixa de score |
| `model/Situation`, `Instrument` | Situação (com o peso no score) e instrumento do perfil |
| `model/Subgenre`, `Song`, `QuizAnswers` | Dados do quiz e do catálogo |
| `model/ApiModels` | Espelho do JSON da API (`@SerializedName`) |
| `logic/SoundMatchEngine` | **Regra de negócio**: score, perfil, escolha da música e explicação |
| `logic/SongCatalog` | Catálogo de músicas por perfil |

### Recursos (`res/`)

| Pasta / arquivo | Papel |
|---|---|
| `values/colors.xml` | Paleta inteira, incluindo a cor de cada perfil |
| `values/themes.xml` | Tema e estilos reutilizáveis: títulos, botões, cards, campos |
| `values/strings.xml` | Todos os textos (facilita traduzir e evita texto fixo no código) |
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

**Como o celular acessa uma API que roda no PC usando `localhost`?**
No celular, `localhost` é o próprio celular. O `adb reverse tcp:8000 tcp:8000` cria um túnel pelo cabo USB: o que chega na porta 8000 do celular vai para a porta 8000 do PC, onde a API está. No emulador daria para usar `10.0.2.2`, um endereço especial que aponta para o computador.

**De onde vem a capa do álbum?**
Da iTunes Search API, uma API pública da Apple. Uso o mesmo Retrofit com outra `baseUrl` (`RetrofitClient.itunes`). A imagem é baixada em `Dispatchers.IO`, porque o Android não permite usar a rede na thread da tela. Se der erro (sem internet), o app mantém o disco de vinil.

**Como o botão do Spotify funciona?**
Ele tenta abrir `spotify:search:<música>`, que só o app do Spotify entende. Se o app não estiver instalado, o Android lança `ActivityNotFoundException`, e eu abro a versão web (`open.spotify.com/search/...`).

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

**Como o score é calculado?**
- Cada resposta vale de 0 a 3 pontos (Blues → Prog).
- Os subgêneros entram pela média dos pesos.
- A soma vai de 0 a 12 e é convertida para 10–100.

Exemplo: Baixo (2) + Dançante (2) + 5 min (1,5) + Hard/Funk (1,5) = 7 → `10 + 7/12 × 90 ≈ 63` → Groove & Funk.

**Como garante que a música é uma "descoberta"?**
O `pickSong` remove do catálogo as músicas do artista favorito digitado, depois prioriza as músicas com mais subgêneros marcados em comum.

**Por que separar o pacote `logic`?**
Para separar responsabilidades: a regra de negócio não depende de tela. Por isso dá para testá-la com JUnit, sem emulador.

**Por que `ListAdapter` + `DiffUtil` no RecyclerView?**
Quando a lista muda (após editar ou excluir), o `DiffUtil` calcula só o que mudou e atualiza apenas esses itens, em vez de redesenhar tudo.

**Por que ViewBinding em vez de `findViewById`?**
O ViewBinding gera uma classe por layout com todas as Views tipadas. É mais seguro (sem `null` nem cast errado) e o código fica menor.
