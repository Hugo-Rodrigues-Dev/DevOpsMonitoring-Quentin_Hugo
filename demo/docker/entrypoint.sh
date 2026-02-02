#!/bin/sh
set -e

mode="${APP_MODE:-AUTO}"
mode_lc=$(printf '%s' "$mode" | tr '[:upper:]' '[:lower:]')

if [ -z "${SPRING_PROFILES_ACTIVE:-}" ]; then
	export SPRING_PROFILES_ACTIVE="docker,${mode_lc}"
fi

exec java -jar /app/app.jar
