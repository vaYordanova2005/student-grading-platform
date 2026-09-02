#!/usr/bin/env bash
# Loads backend/.env (git-ignored, holds DB credentials) if present, then runs the app.
set -e
cd "$(dirname "$0")"
if [ -f .env ]; then
  set -a
  source .env
  set +a
fi
./mvnw.cmd spring-boot:run
