# Recorder Gatling — Capturer une navigation

Le **Recorder** permet de générer automatiquement une simulation Scala en interceptant les requêtes d'un navigateur via un proxy local.  
Il ne nécessite pas Docker — il tourne directement sur la machine.

> **Linux uniquement.** Le script `record.sh` n'est pas compatible Windows/macOS.

---

## Prérequis

| Composant | Version | Usage |
|---|---|---|
| Java (JDK) | 21+ | Exécuter le Recorder (JVM) |
| Firefox ou Chromium | any | Navigation à enregistrer |

### Installer Java si nécessaire

```bash
# SDKMAN (recommandé, toutes distros)
curl -s https://get.sdkman.io | bash
source ~/.sdkman/bin/sdkman-init.sh
sdk install java 21.0.5-tem

# Fedora / RHEL
sudo dnf install java-21-openjdk-devel

# Debian / Ubuntu
sudo apt install openjdk-21-jdk
```

---

## Installation (une seule fois)

```bash
./setup.sh
```

Le script :
1. Vérifie Java 21+
2. Télécharge le bundle officiel Gatling 3.10.0 depuis Maven Central
3. L'extrait dans `.gatling/` (dossier gitignore, non versionné)

---

## Lancer le Recorder

```bash
./record.sh
```

Le script :
- Détecte un **port libre** automatiquement
- Génère `recorder.conf` avec ce port pré-configuré
- Ouvre la **fenêtre GUI Gatling Recorder** (Java/Swing)
- Ouvre **Firefox** (fenêtre normale)

Le port à utiliser est affiché dans le terminal :

```
[INFO] Port proxy : 54321
```

---

## Étape 1 — Configurer le proxy dans Firefox

Dans Firefox : `☰` → `Paramètres` → rechercher **"proxy"** → `Paramètres réseau`

Sélectionner **Configuration manuelle du proxy** :

```
Proxy HTTP   : localhost    Port : <PORT AFFICHÉ DANS LE TERMINAL>
Proxy HTTPS  : localhost    Port : <PORT AFFICHÉ DANS LE TERMINAL>
```

Cocher **"Utiliser ce proxy pour HTTPS"** → `OK`

> Le port change à chaque lancement. Le terminal affiche toujours la valeur exacte.

---

## Étape 2 — Démarrer l'enregistrement

Dans le Recorder Gatling : cliquer **Start !**

Le port est déjà pré-rempli dans le champ "Listening port" (généré dans `recorder.conf`).

---

## Étape 3 — Naviguer

Naviguez librement sur l'application à tester dans Firefox.  
Chaque requête HTTP interceptée apparaît dans la liste du Recorder.

> **Note :** Les ressources statiques (images, fonts, JS) sont filtrées par défaut (`inferHtmlResources = false`). Seules les requêtes API/page sont capturées.

---

## Étape 4 — Sauvegarder

Cliquer **Stop and save** dans le Recorder.

Le fichier `.scala` est généré dans `./simulations/` sous le nom configuré  
(défaut : `RecordedSimulation.scala`).

---

## Étape 5 — Restaurer Firefox

Une fois l'enregistrement terminé, **restorer Firefox** en mode sans proxy :  
`Paramètres réseau` → `Pas de proxy` → `OK`

> Ne pas oublier cette étape — Firefox resterait en erreur sans proxy actif.

---

## Nommer la simulation

```bash
SIMULATION_CLASS=MonScenario ./record.sh
```

Le fichier généré s'appellera `MonScenario.scala` dans `./simulations/`.

---

## Utiliser la simulation générée

Une fois le fichier `.scala` dans `simulations/`, lancer le tir de charge Docker :

```bash
GATLING_SIMULATION_CLASS=MonScenario docker compose up --build
```

> Voir [../README.md](../README.md) pour le guide complet des tirs de charge.

---

## Nettoyage automatique

À la fermeture du script (`Ctrl+C` ou fermeture du Recorder), les processus fils  
(Recorder + navigateur) sont terminés automatiquement via le trap `EXIT`.
