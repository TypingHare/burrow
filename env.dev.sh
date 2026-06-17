# Source this script to set environment variables for the development
# environment.

# Export Burrow environment variables.
export BURROW_NAME="burrow-dev"
export BURROW_VERBOSE=1

# Export PATH to include the development bin directory.
DEV_BIN="$HOME/.local/share/$BURROW_NAME/bin"
case ":$PATH:" in *":$DEV_BIN:"*) ;; *) export PATH="$DEV_BIN:$PATH" ;; esac

# Define project-wise alias.
alias br="burrow ."
