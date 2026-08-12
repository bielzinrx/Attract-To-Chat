#!/usr/bin/env sh
set -eu

atc_repo_dir=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
atc_java_home=${ATC_HOTSWAP_JAVA_HOME:-/mnt/dados/PrismLauncher-home/java/jbrsdk-17.0.14-hotswap}

if [ ! -x "$atc_java_home/bin/java" ]; then
    printf 'JBR 17 com DCEVM não encontrado em: %s\n' "$atc_java_home" >&2
    exit 1
fi

cd "$atc_repo_dir"

env \
    JAVA_HOME="$atc_java_home" \
    PATH="$atc_java_home/bin:$PATH" \
    "$atc_repo_dir/gradlew" --no-daemon \
    :common:classes :fabric:classes "$@"

printf 'Classes recompiladas. Confirme "Architectury Transformer: Redefined" no terminal do jogo.\n'
