# Gatling Sample — Tests de charge

Tirs de charge scriptés via **Docker** — sans installation locale requise.  
Les simulations Scala sont montées dans l'image et exécutées par Gatling 3.10.0.

> **Linux uniquement** pour le mode recorder local.

---

## Documentation complémentaire

| Guide | Contenu |
|---|---|
| [docs/RECORDER.md](docs/RECORDER.md) | Capturer une navigation et générer une simulation |
| [docs/VARIABLES.md](docs/VARIABLES.md) | Injecter des variables, feeders, et chaîner les appels API |

---

## Prérequis

| Composant | Version |
|---|---|
| Docker + Compose | any |

---

## Structure

```
gatling-sample/
├── setup.sh                         # Installe Gatling localement (mode recorder)
├── record.sh                        # Lance Recorder + navigateur
├── Dockerfile                       # Image pour les tirs de charge
├── docker-compose.yml
├── entrypoint.sh
├── simulations/                     # Simulations Scala
│   ├── BasicSimulation.scala        # GET simple répété
│   ├── ArtefactSearchSimulation.scala  # GET avec params CSV aléatoires
│   └── ChainedApiSimulation.scala   # Extrait un champ JSON → appel suivant
├── docs/
│   ├── RECORDER.md
│   └── VARIABLES.md
└── result/                          # Rapports HTML (gitignorés)
```

---

## Lancer un tir de charge

```bash
docker compose up --build
```

Par défaut : **10 utilisateurs** en rampe sur **30 secondes** → `http://localhost:8080/actuator/health`.

---

## Variables d'environnement

| Variable | Description | Défaut |
|---|---|---|
| `API_BASE_URL` | URL de base | `http://localhost:8080` |
| `API_URI` | Endpoint ciblé | `/actuator/health` |
| `USERS` | Nombre d'utilisateurs | `10` |
| `DURATION_SECONDS` | Durée (secondes) | `30` |
| `GATLING_SIMULATION_CLASS` | Classe Scala à exécuter | `BasicSimulation` |

```bash
API_BASE_URL=http://mon-api.local \
USERS=50 \
DURATION_SECONDS=60 \
GATLING_SIMULATION_CLASS=ArtefactSearchSimulation \
docker compose up --build
```

---

## Simulations disponibles

### `BasicSimulation`

Un seul GET répété par N utilisateurs en rampe. Idéal pour tester la tenue en charge d'un endpoint simple.

```
rampUsers(N) during (D secondes)
  └─ GET {API_BASE_URL}{API_URI}  → attend HTTP 200
```

### `ArtefactSearchSimulation`

GET avec query params puisés dans un fichier CSV (feeder infini, aléatoire). Simule un trafic varié sur un endpoint de recherche paginé.

Variables supplémentaires : `MAX_PAGE` (défaut: `10`), `PAGE_SIZE` (défaut: `20`).

```
rampUsers(N) during (D secondes)
  └─ boucle pendant D secondes
       └─ GET {API_URI}?rarity=X&category=Y&description=Z&page=P&size=S
```

### `ChainedApiSimulation`

Enchaîne deux appels : récupère une liste JSON, extrait le premier `id`, puis appelle le détail.  
→ Voir [docs/VARIABLES.md](docs/VARIABLES.md) pour les explications.

```
rampUsers(N) during (D secondes)
  └─ GET /api/items              → extrait items[0].id → session["firstItemId"]
  └─ GET /api/items/${firstItemId} → attend HTTP 200
```

---

## Lire les résultats

```bash
xdg-open result/$(ls -t result/ | head -1)/index.html
```

Le rapport HTML s'ouvre directement dans le navigateur.

---

## Ajouter une simulation

1. Créer `simulations/MaSimulation.scala` (étend `Simulation`)
2. `GATLING_SIMULATION_CLASS=MaSimulation docker compose up --build`

> Les simulations sont copiées dans l'image au build — toute modification nécessite `--build`.
