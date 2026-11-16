# SoundMatch 🎧 – Descoberta Musical por Perfil Sonoro

Aplicativo Android nativo (Kotlin + XML Views) integrado a uma **API REST em FastAPI** com **banco de dados relacional SQLite**.

O usuário conta como ouve música (artista favorito, em que situação ouve e se toca algum instrumento) e responde a um quiz com músicas e discos reais. As perguntas citam o que ele contou no perfil. O app calcula um **score de 10 a 100** (de "raiz" a "experimental"), classifica o usuário em um dos **4 Perfis Sonoros** e **recomenda um clássico do rock**:
- nunca do artista que ele já ouve;
- priorizando os subgêneros que ele curte e, no desempate, músicas em que o instrumento dele se destaca;
- com a capa do álbum e uma explicação do porquê da escolha.

A pessoa responde "Curti" ou "Não curti", e a música é salva no banco via API. Em "Minhas Descobertas" dá para consultar, mudar a avaliação ou excluir.

> Projeto da **Avaliação Parcial 2 (AP2) – Desenvolvimento Mobile**.

---

## 📱 Telas e fluxo

```
MainActivity ──► UserProfileActivity ──► SoundQuizActivity ──► ResultActivity ──► (POST na API)
     │                                                              ├─► capa do álbum (iTunes Search API)
     │                                                              ├─► YouTube / Spotify (Intent implícita)
     └──────────────► HistoryActivity (GET / PUT / DELETE)          └─► Compartilhar (Intent implícita)
```

| # | Activity | O que faz | Views principais |
|---|---|---|---|
| 1 | `MainActivity` | Tela inicial: o disco de vinil nasce no horizonte e gira, e o título neon pulsa | ImageView, TextView, Button |
| 2 | `UserProfileActivity` | Coleta o nome, o artista favorito, a situação em que a pessoa mais ouve música (chips) e o instrumento (o Switch revela o Spinner). Valida os campos | EditText, Spinner, Switch, Button |
| 3 | `SoundQuizActivity` | 3 perguntas personalizadas com o perfil (música para a situação, o que prende no artista favorito, disco preferido) e subgêneros. A barra de progresso avança a cada resposta | ProgressBar, RadioButton, CheckBox, CardView |
| 4 | `ResultActivity` | Tela de "tocando agora": capa do álbum em destaque, música, perfil sonoro, score e explicação. Curti / Não curti salva na API; abre no Spotify ou no YouTube e compartilha | CardView, ImageView, ProgressBar, Button |
| 5 | `HistoryActivity` | Lista as descobertas salvas. Permite mudar a avaliação (PUT) e excluir, com confirmação em AlertDialog | RecyclerView, CardView, ProgressBar |

**11 tipos de Views usados** (o edital exige 6): TextView, EditText, Button, ImageView, CheckBox, RadioButton, Spinner, Switch, RecyclerView, ProgressBar e CardView.

### Screenshots

> Prints tirados do emulador e salvos em `docs/screenshots/`.

| Início | Perfil | Quiz | Resultado | Histórico |
|---|---|---|---|---|
| `docs/screenshots/01-main.png` | `docs/screenshots/02-perfil.png` | `docs/screenshots/03-quiz.png` | `docs/screenshots/04-resultado.png` | `docs/screenshots/05-historico.png` |

---

## 🛠️ Tecnologias

| Camada | Tecnologias |
|---|---|
| **Android** | Kotlin, XML Views + ViewBinding, Material Components 3, RecyclerView, CardView, Retrofit 2 + Gson, Coroutines (`lifecycleScope`) |
| **Backend** | Python 3, FastAPI, SQLAlchemy 2.0, Pydantic 2, Uvicorn |
| **Banco** | SQLite (relacional, com chave estrangeira) |
| **Documentação da API** | Swagger / OpenAPI, gerado automaticamente pelo FastAPI |
| **Testes** | JUnit 4 (regra de negócio do app) |

**Build Android:** AGP 8.7.3, Kotlin 2.0.21, Gradle 8.9, `compileSdk`/`targetSdk` 35, `minSdk` 24.

**Identidade visual:** "Neon Miami / synthwave anos 80":
- tema escuro, magenta `#FF2A6D` e ciano `#05D9E8`;
- fonte retrô **Monoton** (OFL) nos títulos;
- ilustrações em Vector Drawable;
- cada perfil sonoro tem a sua cor.

---

## 📂 Estrutura do projeto

```
SoundMatch/
├── backend/                     API REST (FastAPI + SQLite)
│   ├── main.py                  endpoints CRUD
│   ├── models.py                tabelas (users 1:N recommendations)
│   ├── schemas.py               formato/validação do JSON
│   ├── database.py              conexão com o SQLite
│   └── requirements.txt
└── android/app/src/main/java/com/example/soundmatch/
    ├── ui/                      as 5 Activities
    ├── adapter/                 HistoryAdapter (RecyclerView)
    ├── network/                 Retrofit (SoundMatchApi, ITunesApi e RetrofitClient)
    ├── model/                   dados (perfil, resultado, arquétipos, JSON da API)
    └── logic/                   regra de negócio (SoundMatchEngine + SongCatalog)
```

---

## ▶️ Como executar

### 1. Backend (API)

```bash
cd backend
python -m venv .venv
source .venv/bin/activate            # Windows: .venv\Scripts\activate
pip install -r requirements.txt
uvicorn main:app --host 0.0.0.0 --port 8000 --reload
```

- **Swagger:** http://localhost:8000/docs
- O banco `soundmatch.db` é criado automaticamente na primeira execução.

### 2. App Android

1. Abra a pasta **`android/`** no Android Studio e aguarde o *Gradle Sync* (Gradle JDK: **17**).
2. Ligue o celular por USB, com a **Depuração USB** ativada, e rode no PC:
   ```bash
   adb reverse tcp:8000 tcp:8000
   ```
   O app acessa a API em `http://localhost:8000/`, e o `adb reverse` leva essa porta do celular até o PC. Também funciona no emulador.
3. Execute o app (▶). A capa do álbum precisa de internet no celular; sem internet, aparece um disco de vinil no lugar.

**Testes da regra de negócio:** abra `app/src/test/.../logic/SoundMatchEngineTest.kt` e clique em ▶. Pelo terminal, use `./gradlew test`.

---

## 🌐 API REST

Base: `http://localhost:8000`. Documentação interativa em **[/docs](http://localhost:8000/docs)** (Swagger UI).

| Método | Endpoint | Descrição | Usado em |
|---|---|---|---|
| `POST` | `/recommendations` | Cria o usuário, ou reaproveita um existente pelo nome, e salva a recomendação | ResultActivity |
| `GET` | `/recommendations` | Lista todas as recomendações com o usuário aninhado | HistoryActivity |
| `GET` | `/recommendations/{id}` | Busca uma recomendação | Swagger |
| `PUT` | `/recommendations/{id}` | Muda a avaliação (curti / não curti) | HistoryActivity |
| `DELETE` | `/recommendations/{id}` | Remove a recomendação (resposta 204) | HistoryActivity |

**Exemplo de `POST /recommendations`:**

```json
{
  "name": "Ana",
  "favorite_artist": "Arctic Monkeys",
  "listening_mode": "Academia",
  "plays_instrument": true,
  "score": 63,
  "archetype": "Groove & Funk Rock Alternativo",
  "recommended_song": "Superstition",
  "song_artist": "Stevie Wonder",
  "subgenres": "Hard Rock, Funk/Alternative",
  "liked": true
}
```

A resposta traz o registro salvo, com `id`, `created_at` e o objeto `user` aninhado. O score é validado entre 10 e 100; um valor fora dessa faixa retorna 422.

### API externa: iTunes Search

Na tela de resultado, o app consulta a [iTunes Search API](https://performance-partners.apple.com/search-api), que é pública e não precisa de chave, para mostrar o álbum e a capa da música recomendada:
`GET https://itunes.apple.com/search?term=<música artista>&entity=song&limit=1`

### Banco de dados relacional

```
users (1) ────────< (N) recommendations
  id PK                   id PK
  name                    user_id FK → users.id
  favorite_artist         score, archetype
  listening_mode          recommended_song, song_artist
  plays_instrument        subgenres, liked
  created_at              created_at
```

---

## 🧠 Lógica de recomendação

1. **Score** (não é nota: mede de "raiz" a "experimental"):
   - cada resposta do quiz vale de 0 a 3 pontos, pela posição da opção;
   - os subgêneros marcados entram pela média dos pesos;
   - a situação do perfil também vale de 0 a 3 (Relaxando em casa 0, Academia 1, Na rua 1,5, Festa 2, Estudando 3);
   - a soma (0–15) vira a escala 10–100: `score = 10 + soma/15 × 90`.
2. **Perfil sonoro:**

   | Score | Perfil |
   |---|---|
   | 10–35 | Vintage Blues & Classic Rock |
   | 36–60 | Hard Rock & Heavy Riffs |
   | 61–80 | Groove & Funk Rock Alternativo |
   | 81–100 | Explorador Progressivo & Conceitual |

3. **Música:** cada perfil tem 5 músicas, marcadas por subgênero e pelo instrumento em destaque. O app:
   - **exclui o artista favorito**, para garantir uma descoberta;
   - calcula a afinidade: 2 pontos por subgênero em comum e 1 se o instrumento da pessoa é o destaque da música;
   - desempata de forma determinística (nome + score).
4. **Explicação:** cita o nome, a situação, os subgêneros e o artista da pessoa. Para quem toca, diz em que prestar atenção na música.

O código fica em `logic/SoundMatchEngine.kt` e é coberto por testes unitários.

---

## ✅ Requisitos do edital

| Requisito | Onde está |
|---|---|
| 5 Activities | `ui/` (Main, UserProfile, SoundQuiz, Result, History) |
| Intents explícitas | Main → Perfil / Histórico, Perfil → Quiz, Quiz → Resultado (com `putExtra`) |
| Intent implícita | `ResultActivity`: `ACTION_VIEW` (YouTube e Spotify) e `ACTION_SEND` (compartilhar) |
| 6+ Views | 11 tipos (lista acima) |
| API REST + Swagger | `backend/` + `/docs` |
| CRUD + JSON | POST, GET, PUT e DELETE via Retrofit (`network/SoundMatchApi.kt`) |
| Banco relacional | SQLite com `users` 1:N `recommendations` (FK) |
| Organização | Pacotes `ui`, `adapter`, `network`, `model`, `logic` |
