#!/usr/bin/env sh
set -eu
PORT="${PORT:-8080}"
wget -qO- "http://127.0.0.1:${PORT}/health" >/dev/null
