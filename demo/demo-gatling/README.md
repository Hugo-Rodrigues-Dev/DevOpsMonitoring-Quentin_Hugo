# Gatling Sample - Tests de charge

Le bench est lance depuis le `docker-compose.yml` principal du projet, via deux
profils distincts :
- `bench-ihm` pour la simulation IHM
- `bench-auto` pour la simulation auto

> **Linux uniquement** pour le mode recorder local.

---

## Documentation complementaire

| Guide | Contenu |
|---|---|
| [docs/RECORDER.md](docs/RECORDER.md) | Capturer une navigation et generer une simulation |
| [docs/VARIABLES.md](docs/VARIABLES.md) | Injecter des variables, feeders, et chainer les appels API |

---

## Prerequis

| Composant | Version |
|---|---|
| Docker + Compose | any |

---

## Structure

```text
gatling-sample/
|-- setup.sh                         # Installe Gatling localement (mode recorder)
|-- record.sh                        # Lance Recorder + navigateur
|-- Dockerfile                       # Image pour les tirs de charge
|-- entrypoint.sh
|-- simulations/                     # Simulations Scala
|   |-- BasicSimulation.scala        # Smoke test de l'API des quetes
|   |-- AutoQuestLaunchSimulation.scala # Simulation mode auto (autre binome)
|   `-- IhmQuestLaunchSimulation.scala  # Scenario principal pour le mode IHM
|-- docs/
|   |-- RECORDER.md
|   `-- VARIABLES.md
`-- result/                          # Rapports HTML (gitignores)
```

---

## Lancer un tir de charge

Toutes les valeurs sont centralisees dans le `.env` racine.
Il n'y a pas de fallback dans le `docker-compose.yml` : les variables attendues
doivent etre presentes dans `.env`.

1. Si la stack est arretee, demarrer les services:

```bash
docker compose up -d --build
```

2. Bench IHM nominal (attendu OK):

```bash
APP_MODE=ihm docker compose --profile bench-ihm up --build gatling-ihm
```

3. Bench AUTO nominal (attendu OK):

```bash
APP_MODE=auto docker compose --profile bench-auto up --build gatling-auto
```

4. Cas de controle volontairement en erreur:

```bash
APP_MODE=ihm docker compose --profile bench-auto up --build gatling-auto
```

Pourquoi ce dernier cas renvoie des erreurs:
- en `APP_MODE=ihm`, les quetes visibles sont principalement `RECEIVED`.
- `AutoQuestLaunchSimulation` exige au moins une quete `PROCESSING`.
- la simulation verifie explicitement cette condition (`Verifier au moins une quete PROCESSING (AUTO)`), puis tente un `resolve` qui peut renvoyer `404`.
- le rapport affiche donc des `KO` explicites pour eviter un faux vert.

---

## Variables d'environnement

| Variable | Description |
|---|---|
| `APP_MODE` | Profil Spring applique a l'application |
| `ROYAUME_PROFESSOR_FETCH_DELAY` | Frequence du scheduler de recuperation |
| `POSTGRES_DB` | Nom de la base Postgres |
| `POSTGRES_USER` | Utilisateur Postgres |
| `POSTGRES_PASSWORD` | Mot de passe Postgres |
| `GATLING_API_BASE_URL` | URL cible du bench |
| `GATLING_API_URI` | Endpoint pour `BasicSimulation` |
| `GATLING_QUESTS_BASE_PATH` | Endpoint base pour `IhmQuestLaunchSimulation` |
| `GATLING_LAUNCH_DELAY_MS` | Delai envoye lors du lancement de quete |
| `GATLING_USERS` | Nombre d'utilisateurs |
| `GATLING_DURATION_SECONDS` | Duree (secondes) |
| `GATLING_THINK_TIME_MS` | Pause utilisateur entre actions |
| `GATLING_WARMUP_TIMEOUT_SECONDS` | Temps max d'attente avant la charge utile |

---

## Simulations disponibles

### `BasicSimulation`

Un seul GET repete par N utilisateurs en rampe. Ideal pour tester rapidement la
tenue en charge de `GET /api/royaume/quests`.

```text
rampUsers(N) during (D secondes)
  -> GET /api/royaume/quests -> attend HTTP 200
```

### `IhmQuestLaunchSimulation`

Scenario utilisateur principal pour le mode IHM.
Chaque V-User :
- attend au debut qu'au moins une quete soit visible via `/api/royaume/quests`
- liste les quetes
- selectionne une quete aleatoire
- simule un temps de reflexion
- lance la quete selectionnee

Voir aussi [docs/VARIABLES.md](docs/VARIABLES.md) pour les explications sur les
variables de session et l'extraction JSON.

```text
rampUsers(N) during (D secondes)
  -> GET /api/royaume/quests
  -> extraction des ids / coordonnees
  -> POST /api/royaume/quests/{id}/launch?delayMs=0
```

### `AutoQuestLaunchSimulation`

Simulation dediee au mode auto.
Chaque V-User:
- attend au debut qu'au moins une quete `PROCESSING` soit presente
- liste les quetes auto
- tente un `resolve` sur une quete en cours

La simulation est configuree en fail-fast:
- si aucune quete `PROCESSING` n'est disponible dans le delai de warmup, elle genere des `KO` explicites.

---

## Lire les resultats

Linux:
```bash
xdg-open result/$(ls -t result/ | head -1)/index.html
```

WSL/Windows (depuis n'importe quel dossier):
```bash
ROOT="$(git rev-parse --show-toplevel)"
LATEST="$(ls -t "$ROOT/demo/demo-gatling/result" | head -1)"
explorer.exe "$(wslpath -w "$ROOT/demo/demo-gatling/result/$LATEST/index.html")"
```

---

## Ajouter une simulation

1. Creer `simulations/MaSimulation.scala` (etendre `Simulation`)
2. Lancer :

```bash
docker compose --profile bench-ihm up --build
```

> Les simulations sont copiees dans l'image au build - toute modification
> necessite `--build`.
