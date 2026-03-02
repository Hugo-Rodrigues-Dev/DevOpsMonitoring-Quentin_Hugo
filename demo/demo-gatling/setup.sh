#!/bin/bash
# setup.sh — Installe Gatling localement (bundle officiel)
# Prérequis : Java 21+  |  Linux only
set -e

GATLING_VERSION="3.10.0"
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
GATLING_DIR="$SCRIPT_DIR/.gatling"
GATLING_HOME="$GATLING_DIR/gatling-charts-highcharts-bundle-$GATLING_VERSION"
BUNDLE_URL="https://repo1.maven.org/maven2/io/gatling/highcharts/gatling-charts-highcharts-bundle/${GATLING_VERSION}/gatling-charts-highcharts-bundle-${GATLING_VERSION}-bundle.zip"

# ---------------------------------------------------------------------------
# 1. Vérifier Java
# ---------------------------------------------------------------------------
check_java() {
  if ! command -v java &>/dev/null; then return 1; fi
  local version
  version=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}' | cut -d. -f1)
  [[ "$version" -ge 21 ]] 2>/dev/null
}

if ! check_java; then
  echo ""
  echo "╔══════════════════════════════════════════════════════╗"
  echo "║  Java 21+ requis — non détecté sur cette machine    ║"
  echo "╚══════════════════════════════════════════════════════╝"
  echo ""
  echo "  Options d'installation :"
  echo ""
  echo "  ① SDKMAN (recommandé, multi-distro) :"
  echo "      curl -s https://get.sdkman.io | bash"
  echo "      source ~/.sdkman/bin/sdkman-init.sh"
  echo "      sdk install java 21.0.5-tem"
  echo ""
  echo "  ② Fedora / RHEL :"
  echo "      sudo dnf install java-21-openjdk-devel"
  echo ""
  echo "  ③ Debian / Ubuntu :"
  echo "      sudo apt install openjdk-21-jdk"
  echo ""
  echo "  ④ Arch Linux :"
  echo "      sudo pacman -S jdk21-openjdk"
  echo ""
  echo "  Relancez ./setup.sh après installation."
  exit 1
fi

JAVA_VER=$(java -version 2>&1 | awk -F '"' '/version/ {print $2}')
echo "[OK] Java détecté : $JAVA_VER"

# ---------------------------------------------------------------------------
# 2. Vérifier si Gatling est déjà installé
# ---------------------------------------------------------------------------
if [[ -x "$GATLING_HOME/bin/gatling.sh" ]]; then
  echo "[OK] Gatling $GATLING_VERSION déjà installé dans .gatling/"
  echo ""
  echo "  Pour réinstaller : rm -rf .gatling/ && ./setup.sh"
  exit 0
fi

# ---------------------------------------------------------------------------
# 3. Télécharger et extraire le bundle Gatling
# ---------------------------------------------------------------------------
echo "[INFO] Téléchargement de Gatling $GATLING_VERSION..."
mkdir -p "$GATLING_DIR"
TMP_ZIP=$(mktemp /tmp/gatling-XXXXXX.zip)

curl -L --progress-bar -o "$TMP_ZIP" "$BUNDLE_URL"

echo "[INFO] Extraction dans .gatling/ ..."
unzip -q "$TMP_ZIP" -d "$GATLING_DIR"
rm "$TMP_ZIP"

# Vider les simulations d'exemple pour éviter les conflits
rm -rf "$GATLING_HOME/user-files/simulations/"*

echo ""
echo "╔══════════════════════════════════════════════════════╗"
echo "║  Gatling $GATLING_VERSION installé dans .gatling/   ║"
echo "╚══════════════════════════════════════════════════════╝"
echo ""
echo "  Prochaine étape :"
echo "    ./record.sh          # Lancer le Recorder + Firefox"
echo "    docker compose up    # Lancer un tir de charge"
echo ""
