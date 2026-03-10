# Set up Burrow environment variables for the development environment.
export BURROW_NAME="burrow-dev"
export BURROW_VERBOSE=1

# Add the build output bin directory to the PATH.
export PATH="$PWD/build/bin:$PATH"

# Set up project-wise aliases.
alias broot="burrow ."
