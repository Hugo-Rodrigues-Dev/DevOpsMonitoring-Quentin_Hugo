#!/bin/bash
# record.sh — Lance le Recorder Gatling et ouvre Firefox
# Prérequis : ./setup.sh exécuté une première fois  |  Linux only
set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GATLING_VERSION="3.10.0"
GATLING_HOME="$SCRIPT_DIR/.gatling/gatling-charts-highcharts-bundle-$GATLING_VERSION"
SIMULATIONS_DIR="$SCRIPT_DIR/simulations"
SIMULATION_CLASS="${SIMULATION_CLASS:-RecordedSimulation}"

# ---------------------------------------------------------------------------
# 1. Vérifier l'installation Gatling locale
# ---------------------------------------------------------------------------
if [[ ! -x "$GATLING_HOME/bin/recorder.sh" ]]; then
  echo "[ERROR] Gatling non installé. Lancez d'abord : ./setup.sh"
  exit 1
fi

# ---------------------------------------------------------------------------
# 2. Détecter un port libre
# ---------------------------------------------------------------------------
find_free_port() {
  python3 -c \
    "import socket; s=socket.socket(); s.bind(('',0)); p=s.getsockname()[1]; s.close(); print(p)" \
    2>/dev/null && return
  for p in 8000 8001 8002 8003 8100 8200; do
    ! ss -tlnp 2>/dev/null | grep -q ":$p " && echo "$p" && return
  done
  echo "[ERROR] Impossible de trouver un port libre" >&2; exit 1
}

PROXY_PORT=$(find_free_port)
echo "[INFO] Port proxy : $PROXY_PORT"

# ---------------------------------------------------------------------------
# 3. Détecter le navigateur
# ---------------------------------------------------------------------------
BROWSER=""
for candidate in firefox firefox-esr chromium-browser chromium google-chrome; do
  if command -v "$candidate" &>/dev/null; then BROWSER="$candidate"; break; fi
done
if [[ -z "$BROWSER" ]]; then
  echo "[ERROR] Aucun navigateur détecté (firefox, chromium, google-chrome)"
  exit 1
fi
echo "[INFO] Navigateur : $BROWSER"

# ---------------------------------------------------------------------------
# 4. Générer recorder.conf avec port et dossier pré-configurés
# ---------------------------------------------------------------------------
mkdir -p "$GATLING_HOME/conf"
cat > "$GATLING_HOME/conf/recorder.conf" << EOF
gatling {
  recorder {
    core {
      mode = "Proxy"
      simulationsFolder = "$SIMULATIONS_DIR"
      pkg = ""
      className = "$SIMULATION_CLASS"
      encoding = "utf-8"
    }
    http {
      automaticReferer   = true
      followRedirect     = false
      inferHtmlResources = false
    }
    proxy {
      port = $PROXY_PORT
      https {
        mode = "CertificateAuthority"
      }
    }
  }
}
EOF

# ---------------------------------------------------------------------------
# 5. Nettoyage à la sortie
# ---------------------------------------------------------------------------
FF_PID=""

cleanup() {
  [[ -n "$FF_PID" ]] && kill "$FF_PID" 2>/dev/null || true
  echo ""
  echo "[INFO] Simulation générée dans : simulations/$SIMULATION_CLASS.scala"
}
trap cleanup EXIT

# ---------------------------------------------------------------------------
# 6. Lancer le Recorder Gatling (processus de fond)
# ---------------------------------------------------------------------------
echo "[INFO] Lancement du Recorder Gatling..."
"$GATLING_HOME/bin/recorder.sh" &
RECORDER_PID=$!

# Attendre que le proxy soit actif
echo -n "[INFO] Attente du proxy"
for i in $(seq 1 40); do
  if ss -tlnp 2>/dev/null | grep -q ":$PROXY_PORT "; then
    echo " OK"
    break
  fi
  sleep 0.5
  echo -n "."
done
echo ""

# ---------------------------------------------------------------------------
# 7. Ouvrir le navigateur normalement (proxy à configurer manuellement)
# ---------------------------------------------------------------------------
echo "[INFO] Lancement $BROWSER..."
"$BROWSER" &>/dev/null &
FF_PID=$!

cat << MSG

╔══════════════════════════════════════════════════════════════╗
║  Recorder prêt — configurez le proxy dans votre navigateur  ║
╠══════════════════════════════════════════════════════════════╣
║                                                              ║
║  Proxy HTTP  : localhost    Port : $PROXY_PORT               ║
║  Proxy HTTPS : localhost    Port : $PROXY_PORT               ║
║                                                              ║
║  Firefox : Paramètres → Réseau → Config manuelle du proxy   ║
║                                                              ║
║  Étapes :                                                    ║
║    1. Configurez le proxy dans Firefox (voir ci-dessus)      ║
║    2. Dans le Recorder  → cliquez "Start !"                  ║
║    3. Naviguez sur l'application à tester                    ║
║    4. Dans le Recorder  → cliquez "Stop and save"            ║
║    5. La simulation apparaît dans simulations/               ║
╚══════════════════════════════════════════════════════════════╝

MSG

# Attendre la fin du Recorder
wait "$RECORDER_PID"
