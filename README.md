# DevOpsMonitoring-Quentin_Hugo

## Démarrer l'application

L'application expose l'IHM et les API de quêtes sur `http://localhost:8080`.

### Choisir un mode

Deux modes existent :
- **Auto** : l'application récupère périodiquement une quête chez le prof, la stocke en base, puis planifie sa résolution.
- **IHM** : l'application récupère les quêtes mais ne planifie pas la résolution (déclenchement manuel).

### En local (H2)

Auto :
```bash
cd demo
SPRING_PROFILES_ACTIVE=auto ./mvnw spring-boot:run
```

IHM :
```bash
cd demo
SPRING_PROFILES_ACTIVE=ihm ./mvnw spring-boot:run
```

### Via Docker Compose (Postgres)

Choisir le mode dans `.env` (à la racine du repo) :
```
APP_MODE=AUTO
```
ou
```
APP_MODE=IHM
```

Puis lancer (depuis la racine du repo) :
```bash
docker compose up -d --build
```

Arrêter :
```bash
docker compose down
```

### Vérifier rapidement

Liste des quêtes (IHM consomme ces endpoints) :
```bash
curl --noproxy '*' http://localhost:8080/quests
curl --noproxy '*' http://localhost:8080/api/quests
curl --noproxy '*' http://localhost:8080/api/royaume/quests
```

Relancer une résolution manuelle :
```bash
curl -X POST http://localhost:8080/api/royaume/quests/{id}/resolve
```

## Ce qui se passe concrètement (étape par étape)

1. **Démarrage** : Spring Boot charge le profil actif (H2 en local, Postgres en docker).
2. **Scheduler (tous modes)** : toutes les `fetch-delay`, l'appli appelle le microservice du prof (`GET /api/quests?group=...`).
3. **Persistance** : la quête reçue est stockée en base locale (`Quest`).
4. **Planification** : une résolution est planifiée sans bloquer de thread (TaskScheduler).
5. **Résolution** : à l'heure prévue, l'appli envoie `POST /api/quests/{id}/resolve` au prof.
6. **Statut** : la quête passe en `RESOLVED` si la réponse est valide (`ok=true` et `codeRetour=OK`), sinon `FAILED`.
7. **IHM** : l'interface lit la liste via `/api/royaume/quests` et affiche les quêtes sur la carte. En mode IHM, la résolution est manuelle.
