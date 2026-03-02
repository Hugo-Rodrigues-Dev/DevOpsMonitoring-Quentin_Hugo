#!/bin/sh
set -e

cd ${GATLING_HOME}

# Lancer Gatling en mode local sans prompts interactifs
# -rm local : spécifie le mode d'exécution local
# -s : spécifie la classe de simulation à lancer
./bin/gatling.sh -rm local -s ${GATLING_SIMULATION_CLASS}

