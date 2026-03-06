#!/usr/bin/env bash

################################################################################
# Copyright 2026 James Chen                                                    #
#                                                                              #
# This script installs latest Burrow to the current environment. It exists on  #
# any errors encountered during installation.                                  #
#                                                                              #
#                                                                              #
#                                                                              #
#                                                                              #
#                                                                              #
################################################################################

# Exit on errors, unset variables, and failed pipeline segments.
set -euo pipefail

# Allow callers to override the repo URL or installation name from the
# environment without editing the script.
REPOSITORY_URL="${REPOSITORY_URL:-https://github.com/TypingHare/burrow.git}"
BURROW_NAME="${BURROW_NAME:-burrow}"

# Print installer progress messages to stdout.
log() {
    printf '[burrow-install] %s\n' "$*"
}

# Print an error message and stop immediately.
fail() {
    printf '[burrow-install] error: %s\n' "$*" >&2
    exit 1
}

# Ensure a required external command exists before continuing.
require_command() {
    if ! command -v "$1" >/dev/null 2>&1; then
        fail "required command not found: $1"
    fi
}

# Burrow's install flow shells out to these tools, so fail early if they are
# missing.
require_command git
require_command go
require_command make

# Read HOME defensively so the script fails clearly if the shell environment is
# incomplete.
home_dir="${HOME:-}"
if [[ -z "$home_dir" ]]; then
    fail 'HOME is not set'
fi

# Respect XDG directories when present, otherwise fall back to the common
# per-user defaults.
xdg_data_home="${XDG_DATA_HOME:-$home_dir/.local/share}"
xdg_config_home="${XDG_CONFIG_HOME:-$home_dir/.config}"

burrow_data_dir="$xdg_data_home/$BURROW_NAME"
burrow_bin_dir="$burrow_data_dir/bin"
binary_path="$burrow_bin_dir/burrow"

# Refuse to install over an existing Burrow instance for the same name.
burrow_config_dir="$xdg_config_home/$BURROW_NAME"
if [[ -e "$burrow_config_dir" ]]; then
    fail "config directory already exists: $burrow_config_dir"
fi

burrow_data_dir="$xdg_data_home/$BURROW_NAME"
if [[ -e "$burrow_data_dir" ]]; then
    fail "data directory already exists: $burrow_data_dir"
fi

# Ensure that the Burrow bin directory can be created and is writable.
log "creating directories under $burrow_data_dir"
mkdir -p "$burrow_bin_dir"

# Clone the repository into a temporary location under the home directory.
temp_burrow_dir="$HOME/.burrow.iZFxHCUWpME="
log "cloning $REPOSITORY_URL into $temp_burrow_dir"
git clone "$REPOSITORY_URL" "$temp_burrow_dir"

# Build the repository once with Make so we have a working Burrow executable
# before Burrow rebuilds itself using its own build command.
log 'building bootstrap executable'
make -C "$temp_burrow_dir" build
cp "$temp_burrow_dir/build/burrow" "$binary_path"
chmod 755 "$binary_path"

# Add essential decorations to the root chamber.
log 'adding essential decorations to root chamber'
carton_name="github.com/TypingHare/burrow"
"$binary_path" . decoration add "clutter@$carton_name"
log 'added clutter decoration to root chamber'
"$binary_path" . decoration add "dictator@$carton_name"
log 'added dictator decoration to root chamber'
"$binary_path" . decoration add "shell@$carton_name"
log 'added shell decoration to root chamber'

# Rebuild Burrow executable (burrow).
log 'building Burrow executable'
"$binary_path" . burrow build

# Build minimal Burrow executable (burrow-min).
log 'building minimal Burrow executable'
"$binary_path" . burrow build --minimal

# Clean up the temporary directory.
log "removing temporary directory $temp_burrow_dir"
rm -rf "$temp_burrow_dir"
