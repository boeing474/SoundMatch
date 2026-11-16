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
- [ ] Abrir o emulador e rodar o app. No celular físico (bônus), rodar `adb reverse tcp:8000 tcp:8000` e trocar a `BASE_URL`.
- [ ] Fazer **um** cadastro de teste antes, para o histórico não aparecer vazio.

---

## 2. Roteiro da demonstração (5–7 min)

1. **Tela inicial**
   - Mostrar a identidade visual: o globo gira (`ObjectAnimator`) e o título pulsa (`ValueAnimator`).
   - Tocar em **"Descobrir Meu Som"** → *Intent explícita*.
2. **Perfil**
   - Tentar avançar com um campo vazio: o campo fica em erro e aparece um `Toast` (**feedback visual**).
   - Preencher os campos e mostrar o `Spinner` e o `Switch`.
   - Avançar → *Intent explícita com `putExtra`* levando o `UserProfile`.
3. **Quiz**
   - Responder uma pergunta por vez: a `ProgressBar` avança.
   - O botão só habilita quando as 3 perguntas têm resposta.
4. **Resultado**
   - Mostrar o card na cor do perfil, o score e a música.
   - Mostrar o card **"Por que essa música?"** (processamento dos dados).
   - Tocar em **"Ouvir no YouTube"** → *Intent implícita `ACTION_VIEW`*.
   - Tocar em **"Compartilhar"** → *Intent implícita `ACTION_SEND`*.
   - Escrever uma nota e tocar em **"Salvar no Banco via API"**: aparece a `ProgressBar` e depois o `Toast` de sucesso (**POST**).
5. **Swagger**
   - Mostrar o `GET /recommendations` no navegador com o registro que acabou de ser salvo, o que prova a persistência no banco.
6. **Histórico**
   - Voltar ao início e abrir o Histórico (**GET**).
   - **Editar Nota** no `AlertDialog` (**PUT**).
   - **Excluir**, com confirmação (**DELETE**).
7. **Código**
   - Abrir `SoundMatchEngine.kt` e explicar a fórmula.
   - Mostrar os testes passando (`SoundMatchEngineTest`, botão ▶).

---

## 3. Onde está cada requisito

Para achar rápido, procure por `REQUISITO` no Android Studio (Ctrl+Shift+F).

| Requisito | Arquivo |
|---|---|
| 5 Activities | `ui/MainActivity.kt`, `UserProfileActivity.kt`, `SoundQuizActivity.kt`, `ResultActivity.kt`, `HistoryActivity.kt` (registradas no `AndroidManifest.xml`) |
| Intent explícita | `MainActivity.setupNavigation()`, `UserProfileActivity.openQuiz()`, `SoundQuizActivity.showResult()` |
| Intent implícita | `ResultActivity.openYoutube()` (`ACTION_VIEW`) e `shareProfile()` (`ACTION_SEND`) |
| Views | layouts em `res/layout/` (comentários `REQUISITO`) |
| Feedback visual | erro no campo + `Toast` (Perfil), botão desabilitado (Quiz), `ProgressBar` + `Toast` (Resultado e Histórico) |
| API REST + Swagger | `backend/main.py` (os `summary` e `tags` aparecem no `/docs`) |
| CRUD Retrofit | `network/SoundMatchApi.kt` + chamadas em `ResultActivity` e `HistoryActivity` |
| Banco relacional | `backend/models.py` (`ForeignKey("users.id")` + `relationship`) |
| Organização | pacotes `ui`, `adapter`, `network`, `model`, `logic` |

---

## 4. Fluxo de dados (do toque ao banco)

```
[Tela] ResultActivity
   │  RecommendationRequest.from(result, nota)      → objeto Kotlin
   ▼
[Retrofit] SoundMatchApi.createRecommendation()      → Gson converte em JSON
   │  POST http://10.0.2.2:8000/recommendations
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
| `ui/ResultActivity` | Exibe o resultado, faz o POST na API e dispara as Intents implícitas |
| `ui/HistoryActivity` | GET, PUT (AlertDialog) e DELETE (com confirmação) |
| `adapter/HistoryAdapter` | Transforma cada registro da API em um card do RecyclerView (`ListAdapter` + `DiffUtil`) |
| `network/SoundMatchApi` | Interface Retrofit: um método por endpoint |
| `network/RetrofitClient` | Instância única do Retrofit com a `BASE_URL` |
| `model/UserProfile`, `SoundResult` | Dados que passam entre as telas (`Serializable`) |
| `model/Archetype` | Os 4 perfis: título, descrição, cor e faixa de score |
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
| `drawable/` | Vetores: fundo synthwave, globo disco, ícone do app |
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

**Por que `10.0.2.2` e não `localhost`?**
No emulador, `localhost` é o próprio emulador. `10.0.2.2` é o endereço especial que aponta para o computador. No celular físico, o `adb reverse` faz a porta 8000 do celular apontar para a do PC.

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
