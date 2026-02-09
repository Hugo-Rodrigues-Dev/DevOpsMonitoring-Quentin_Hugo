# DevOpsMonitoring-Quentin_Hugo

## Démarrer l'application

### En local

```bash
cd demo
./mvnw spring-boot:run
```

### Via Docker Compose

```bash
docker compose build
docker compose up
```

L'application sera disponible sur [http://localhost:8080](http://localhost:8080).
> Astuce : vous pouvez forcer le mode AUTO/IHM côté compose via `ROYAUME_API_MODE=AUTO` (valeur par défaut) ou `IHM`.

## API Royaume

- **Endpoints**
  - `GET /api/royaume/quests` : renvoie uniquement les quêtes actives (statuts `PENDING` ou `RUNNING` en AUTO, `PENDING` en IHM).
  - `POST /api/royaume/quests/fetch` : déclenche manuellement une récupération de quête auprès du service du professeur.
  - `POST ou GET /api/royaume/quests/{id}/launch` : lance la résolution d’une quête depuis l’IHM ; l’appel répond immédiatement.
  - `GET /api/royaume/mode` / `POST /api/royaume/mode?mode=IHM|AUTO` : permet de consulter ou modifier le mode d’exécution.
- **Traitement** :
  - En **mode AUTO** (valeur par défaut ou via `POST /api/royaume/mode?mode=AUTO`), un scheduler interroge `https://royaume.devonn.io/api/quests` toutes les 5 s (paramètre `royaume.api.poll-interval`) **et** déclenche immédiatement la résolution de chaque quête récupérée.
  - En mode **IHM**, aucune résolution automatique n’est effectuée : l’utilisateur déclenche `POST /fetch` et `POST /launch`.
  - Chaque réponse est persistée dans PostgreSQL via `QuestEntity` avec un statut (`PENDING`, `RUNNING`, `RESOLVED`).
  - Lorsqu'une résolution est lancée, la quête passe en `RUNNING`, le délai est attendu dans un thread dédié, puis `/api/quests/{id}/resolve` est appelé. À la confirmation du prof, la quête est marquée `RESOLVED` et une trace est loggée.

Exemple de réponse :

```json
{
  "codeRetour": "OK",
  "quest": {
    "id": "65b9b601-4e16-4bd0-be90-8e61fb4f5073",
    "kind": "LORDS",
    "titre": "Chemin secret vers Tol Morwen",
    "description": "Escortez un porteur portant l’Anneau teal à travers Tol Morwen. Méfiez-vous de Elrond.",
    "lieu": "Tol Morwen",
    "ennemi": "Elrond",
    "priorite": "ROYALE",
    "recompense": "EPEE_RUNIQUE",
    "dureeEstimee": "PT30S",
    "delaiLimite": "2026-02-02T10:32:12.618006Z",
    "latitude": 50.063,
    "longitude": 7.21
  },
  "errorMessage": "",
  "ok": true
}
```
