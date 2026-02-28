BUILD_DIR = build
EXECUTABLE = burrow
EXECUTABLE_MIN = burrow-min
DEV_BURROW_SOURCE_DIR = ~/.local/share/burrow-dev/source/TypingHare/burrow

.PHONY: prepare clean build install-dev

# Prepare the project.
prepare:
	bun install
	bun husky

# Clean third-party libraries and build artifacts and restore the project to the
# original state.
clean:
	rm -rf .cache build node_modules

# Build the project and output an executable to the build directory. It also
# copies
build:
	go build -o $(BUILD_DIR)/$(EXECUTABLE) ./cmd
	cp $(BUILD_DIR)/$(EXECUTABLE) $(BUILD_DIR)/$(EXECUTABLE_MIN)

# Copy this directory to the dev Burrow source directory. It excludes some files
# and directories that are not needed for building a Burrow executable. The
# burrow name is "burrow-dev".
install-dev:
	mkdir -p $(DEV_BURROW_SOURCE_DIR)
	rm -rf $(DEV_BURROW_SOURCE_DIR)
	rsync -a --delete \
		--exclude '.cache' \
		--exclude '.git' \
		--exclude '.husky' \
		--exclude 'docs' \
		--exclude 'node_modules' \
		--exclude 'build' \
		./ $(DEV_BURROW_SOURCE_DIR)
