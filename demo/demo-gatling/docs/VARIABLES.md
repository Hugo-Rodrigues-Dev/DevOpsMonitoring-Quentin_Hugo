# Dynamisation des variables — Feeders & Session Chaining

Gatling permet d'injecter des données dynamiques dans les simulations de deux façons :

| Mécanisme | Usage |
|---|---|
| **Feeder** | Injecter des données externes (CSV, JSON, aléatoires) dans chaque utilisateur virtuel |
| **Session chaining** | Extraire une valeur de la réponse d'un appel pour la réutiliser dans le suivant |

---

## 1. Feeder — Données externes

Un feeder alimente chaque utilisateur virtuel avec un enregistrement.  
Le feeder le plus simple utilise un fichier CSV :

### `resources/search.csv`

```csv
keyword,category,page
laptop,electronics,0
phone,electronics,1
chair,furniture,0
```

### Simulation avec feeder CSV

```scala
val feeder = csv("search.csv").random  // .circular, .queue ou .random

val scn = scenario("Search with feeder")
  .feed(feeder)
  .exec(
    http("Search items")
      .get("/api/items")
      .queryParam("keyword", "#{keyword}")
      .queryParam("category", "#{category}")
      .queryParam("page", "#{page}")
      .check(status.is(200))
  )
```

> Les variables du feeder sont disponibles via `#{nomDeLaColonne}` dans les requêtes.

### Feeder programmatique (sans fichier)

```scala
val feeder = Iterator.continually(Map(
  "page" -> Random.nextInt(10),
  "size" -> List(10, 20, 50)(Random.nextInt(3))
))
```

---

## 2. Session Chaining — Extraire et réutiliser

Le session chaining consiste à :
1. Faire un premier appel HTTP
2. **Extraire** un champ de la réponse JSON avec `jsonPath`
3. **Sauvegarder** la valeur dans la session (`.saveAs("maVariable")`)
4. **Réutiliser** la valeur dans l'appel suivant via `#{maVariable}`

### Exemple minimal

```scala
exec(
  http("Get items list")
    .get("/api/items")
    .check(
      status.is(200),
      jsonPath("$.items[0].id").saveAs("firstItemId")   // (1) extraction
    )
)
.exec(
  http("Get item detail")
    .get("/api/items/#{firstItemId}")                   // (2) réutilisation
    .check(status.is(200))
)
```

### Extraction avancée

| Expression | Cible |
|---|---|
| `jsonPath("$.id")` | Champ `id` à la racine |
| `jsonPath("$.items[0].id")` | Premier élément d'un tableau |
| `jsonPath("$.items[*].id").findAll` | Tous les IDs d'un tableau |
| `jsonPath("$.token").saveAs("jwt")` | Récupérer un token d'auth |

---

## 3. Simulation complète : `ChainedApiSimulation`

Voir [`../simulations/ChainedApiSimulation.scala`](../simulations/ChainedApiSimulation.scala).

Cette simulation enchaîne :

```
rampUsers(N) during (D secondes)
  └─ GET /api/items
         ↓ extrait items[0].id → session["firstItemId"]
  └─ GET /api/items/${firstItemId}
         ↓ extrait le champ "name"  → session["itemName"]
  └─ GET /api/items?search=${itemName}
         ↓ attend HTTP 200
```

### Tester avec l'API publique JSONPlaceholder

La simulation utilise par défaut [https://jsonplaceholder.typicode.com](https://jsonplaceholder.typicode.com) (API de test publique, sans auth) :

```bash
# Tir de charge avec l'API de test publique
API_BASE_URL=https://jsonplaceholder.typicode.com \
GATLING_SIMULATION_CLASS=ChainedApiSimulation \
docker compose up --build
```

```bash
# Sur votre propre API
API_BASE_URL=http://mon-api.local \
GATLING_SIMULATION_CLASS=ChainedApiSimulation \
docker compose up --build
```

---

## 4. Gérer les cas d'échec d'extraction

Si le `jsonPath` ne trouve pas de valeur, la session est marquée en échec et l'étape suivante est sautée.  
Pour fournir une valeur par défaut :

```scala
jsonPath("$.items[0].id").optional.saveAs("firstItemId")

// Plus loin :
.exec(session => {
  val id = session("firstItemId").asOption[String].getOrElse("fallback-id")
  session.set("firstItemId", id)
})
```

---

## 5. Cas d'usage typiques

| Cas | Extraction |
|---|---|
| Récupérer un token JWT après login | `jsonPath("$.access_token").saveAs("token")` puis `.header("Authorization", "Bearer #{token}")` |
| Pagination dynamique | `jsonPath("$.totalPages").saveAs("totalPages")` |
| Créer puis lire une ressource | POST → extraire l'`id` → GET `/{id}` |
| Chaîner une recherche | GET liste → extraire le premier résultat → GET détail |
