# DevOpsMonitoring-Quentin_Hugo

## Démarrer l'application

L'application expose l'IHM et les API de quêtes sur `http://localhost:8080`.

### Choisir un mode

Deux modes existent :
- **Auto** : l'application récupère périodiquement une quête chez le prof, la stocke en base, puis planifie sa résolution.
- **IHM** : l'application récupère les quêtes mais ne planifie pas la résolution (déclenchement manuel).

### En local (H2)

Configurer d'abord l'acces Maven prive dans `~/.m2/settings.xml` :
```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 https://maven.apache.org/xsd/settings-1.0.0.xsd">
  <servers>
    <server>
      <id>devonn-registry</id>
      <username>reader</username>
      <password>TOKEN_DEVONN</password>
    </server>
  </servers>
</settings>
```

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

### Via Docker Compose (Postgres + Observabilité)

Créer votre `.env` à partir du template puis ajuster les valeurs :
```bash
cp .env.example .env
```

Variables principales :
```
APP_MODE=auto # ou ihm
ROYAUME_PROFESSOR_FETCH_DELAY=2s
DEVONN_REGISTRY_USERNAME=reader
DEVONN_REGISTRY_PASSWORD=...
OTEL_TRACES_EXPORTER_ENDPOINT=http://otel-collector:4317
OTEL_METRICS_EXPORTER_ENDPOINT=http://otel-collector:4318/v1/metrics
GF_SECURITY_ADMIN_USER=admin
GF_SECURITY_ADMIN_PASSWORD=admin
POSTGRES_DB=royaume
POSTGRES_USER=royaume
POSTGRES_PASSWORD=...
GATLING_WARMUP_TIMEOUT_SECONDS=30
```

`DEVONN_REGISTRY_USERNAME` et `DEVONN_REGISTRY_PASSWORD` doivent correspondre aux credentials du `~/.m2/settings.xml`.

Commandes attendues :
# Si vous êtes sur powershell, pensez à adapter les commandes (ex: ajouter $env: devant les variables d'environnement).

```bash
docker compose down -v; APP_MODE=auto docker compose build --no-cache royaume-app; docker compose up
```

### Bench Gatling - commandes par cas

1. Cas IHM nominal (attendu OK)
```bash
docker compose down -v; APP_MODE=ihm  docker compose --profile bench-ihm build --no-cache royaume-app; docker compose --profile bench-ihm up
```

2. Cas AUTO nominal (attendu OK)
```bash
docker compose down -v; APP_MODE=auto docker compose --profile bench-auto build --no-cache royaume-app; docker compose --profile bench-auto up
```

3. Cas de controle volontairement en erreur (attendu KO)
```bash
docker compose down -v; APP_MODE=ihm docker compose --profile bench-auto build --no-cache royaume-app; docker compose --profile bench-auto up
```
Ce cas produit des erreurs par design: en mode `ihm`, l'API renvoie surtout des quetes `RECEIVED`, alors que la simulation `AutoQuestLaunchSimulation` exige des quetes `PROCESSING` et verifie explicitement cette condition.

Conteneurs démarrés :
- `royaume-app` (Spring Boot + Actuator 8080/8090)
- `postgres`
- `otel-collector` + `jaeger` (traces)
- `prometheus` (scrape /actuator/prometheus) + `grafana` (http://localhost:3000)
- `elasticsearch` + `filebeat` + `kibana` (http://localhost:5601)

Ports utiles :
- API : `http://localhost:8080`
- Actuator/Prometheus scrape : `http://localhost:8090/actuator/*`
- Prometheus : `http://localhost:9090`
- Grafana : `http://localhost:3000`
- Elasticsearch : `http://localhost:9200`
- Kibana : `http://localhost:5601`
- Jaeger UI : `http://localhost:16686`

### Build natif GraalVM + image distroless

Pre-requis local :
- Java 25 GraalVM installe (pas un OpenJDK classique)
- variable `JAVA_HOME` pointe vers GraalVM 25

Compiler un binaire natif dans `target/app` :
```bash
cd demo
./mvnw -Pnative -DskipTests native:compile
```

Construire l'image runtime distroless (sans shell/outils systeme) :
```bash
docker build -f demo/Dockerfile.native -t royaume-app:native ./demo
```

### Kibana - Data View

Pour afficher les logs Filebeat dans Kibana, la Data View doit utiliser ces valeurs :
- **Name** : `Royaume Quests`
- **Index pattern** : `royaume-quests-*`

En Docker Compose, cette Data View est créée automatiquement par le service `kibana-init`.

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
