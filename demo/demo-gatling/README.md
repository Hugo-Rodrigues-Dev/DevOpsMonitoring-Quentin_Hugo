# Gatling Sample - Tests de charge

Le bench est lance depuis le `docker-compose.yml` principal du projet, via le
service `gatling` et le profil `bench`.

Le mode par defaut est maintenant `ihm`, afin que la simulation principale
charge l'application avec des quetes visibles et lancables depuis l'IHM.

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
|   |-- ArtefactSearchSimulation.scala  # Variante simple de navigation IHM
|   `-- IhmQuestLaunchSimulation.scala  # Scenario principal pour le mode IHM
|-- docs/
|   |-- RECORDER.md
|   `-- VARIABLES.md
`-- result/                          # Rapports HTML (gitignores)
```

---

## Lancer un tir de charge

```bash
docker compose --profile bench up --build
```

Par defaut, cela lance `IhmQuestLaunchSimulation` avec **20 utilisateurs** en
rampe sur **60 secondes** contre `http://royaume-app:8080` depuis le reseau
Docker, avec l'application en mode `ihm`.

Pour lancer le bench en mode explicite :

```bash
APP_MODE=ihm docker compose --profile bench up --build
```

---

## Variables d'environnement

| Variable | Description | Defaut |
|---|---|---|
| `APP_MODE` | Profil Spring applique a l'application | `ihm` |
| `GATLING_SIMULATION_CLASS` | Classe Scala a executer | `IhmQuestLaunchSimulation` |
| `GATLING_USERS` | Nombre d'utilisateurs | `20` |
| `GATLING_DURATION_SECONDS` | Duree (secondes) | `60` |
| `GATLING_THINK_TIME_MS` | Temps de pause utilisateur entre actions | `750` |

```bash
APP_MODE=ihm \
GATLING_USERS=50 \
GATLING_DURATION_SECONDS=60 \
GATLING_SIMULATION_CLASS=IhmQuestLaunchSimulation \
docker compose --profile bench up --build
```

---

## Simulations disponibles

### `BasicSimulation`

Un seul GET repete par N utilisateurs en rampe. Ideal pour tester rapidement la
tenue en charge de `GET /api/royaume/quests`.

```text
rampUsers(N) during (D secondes)
  -> GET /api/royaume/quests -> attend HTTP 200
```

### `ArtefactSearchSimulation`

Variante simple de navigation IHM :
- liste les quetes
- rafraichit la liste une seconde fois
- lance une quete aleatoire issue de la liste

```text
rampUsers(N) during (D secondes)
  -> GET /api/royaume/quests
  -> GET /api/royaume/quests
  -> POST /api/royaume/quests/{id}/launch?delayMs=0
```

### `IhmQuestLaunchSimulation`

Scenario utilisateur principal pour le mode IHM.
Chaque V-User :
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

---

## Lire les resultats

```bash
xdg-open result/$(ls -t result/ | head -1)/index.html
```

Le rapport HTML s'ouvre directement dans le navigateur.

Sous Windows, ouvrir simplement le fichier `index.html` du dernier dossier cree
dans `result/`.

---

## Ajouter une simulation

1. Creer `simulations/MaSimulation.scala` (etendre `Simulation`)
2. Lancer :

```bash
GATLING_SIMULATION_CLASS=MaSimulation docker compose --profile bench up --build
```

> Les simulations sont copiees dans l'image au build - toute modification
> necessite `--build`.
