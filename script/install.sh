#!/usr/bin/env bash

################################################################################
# Copyright 2026 James Chen                                                    #
#                                                                              #
# This script installs Burrow v2026.1 to the current environment. It exists on #
# any errors encountered during installation.                                  #
#                                                                              #
# Required commands: git, go, make                                             #
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
    printf '\033[36m[burrow-install] %s\033[0m\n' "$*"
}

# Print an error message and stop immediately.
fail() {
    printf '\033[31m[burrow-install] error: %s\033[0m\n' "$*" >&2
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

# If any step of the installation fails, remove any directories that were
# created during the installation process to avoid leaving a broken installation
# behind.
cleanup_install_dirs() {
    exit_code=$?
    if [[ $exit_code -ne 0 ]]; then
        rm -rf "$burrow_config_dir" "$burrow_data_dir"
    fi
}
trap cleanup_install_dirs EXIT

# Ensure that the Burrow bin directory can be created and is writable.
log "creating directories under $burrow_data_dir"
mkdir -p "$burrow_bin_dir"

# Clone the repository into a temporary location under the home directory.
temp_burrow_dir="$HOME/.burrow.iZFxHCUWpME="

# If any step of the installation fails, remove the temporary directory to avoid
# leaving behind a clone of the repository.
cleanup_temp_dir() {
    if [[ -n "${temp_burrow_dir:-}" && -d "$temp_burrow_dir" ]]; then
        rm -rf "$temp_burrow_dir"
    fi
}
trap 'cleanup_install_dirs; cleanup_temp_dir' EXIT

log "cloning $REPOSITORY_URL into $temp_burrow_dir"
git clone "$REPOSITORY_URL" "$temp_burrow_dir"

# Build the repository once with Make so we have a working Burrow executable
# before Burrow rebuilds itself using its own build command.
log 'building bootstrap executable'
make -C "$temp_burrow_dir" build
cp "$temp_burrow_dir/build/burrow" "$binary_path"
chmod 755 "$binary_path"

# Add essential decorations to the root chamber.
carton_name="github.com/TypingHare/burrow"
log 'adding clutter decoration to root chamber'
"$binary_path" . decoration add "clutter@$carton_name"
log 'adding dictator decoration to root chamber'
"$binary_path" . decoration add "dictator@$carton_name"
log 'adding shell decoration to root chamber'
"$binary_path" . decoration add "shell@$carton_name"

# Rebuild Burrow executable (burrow).
log 'building Burrow executable'
"$binary_path" . burrow build

# Build minimal Burrow executable (burrow-min).
log 'building minimal Burrow executable'
"$binary_path" . burrow build --minimal

# Create broot (an alias for "burrow .").
log 'creating broot'
"$binary_path" . shell create

# Show the user where the binary was installed.
log "Burrow installed successfully to $binary_path"

# If the bin directory is not already on the PATH, print a message to the user
# about how to add it.
if ! echo "$PATH" | tr ':' '\n' | grep -qx "$burrow_bin_dir"; then
    printf "\033[38;5;208m[burrow-install] LAST STEP: Please add %s to your \
PATH.\033[0m\n" "$burrow_bin_dir"
fi

# Finally, print a message about how to verify the installation.
printf "\033[38;5;208m[burrow-install] After restarting your shell, run \
\"broot --version\" to verify the installation.\033[0m\n"
