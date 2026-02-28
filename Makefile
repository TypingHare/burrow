BUILD_DIR = build
EXECUTABLE = burrow
EXECUTABLE_MIN = burrow-min
TEST_BURROW_SOURCE_DIR = ~/.local/share/burrow-test/source/TypingHare/burrow

.PHONY: prepare clean build install-test

# Prepare the project.
prepare:
	bun install
	bun husky

# Clean third-party libraries and build artifact.
clean:
	rm -rf .cache build node_modules

# Build the project and output the executable to the build directory.
build:
	go build -o $(BUILD_DIR)/$(EXECUTABLE) ./cmd
	cp $(BUILD_DIR)/$(EXECUTABLE) $(BUILD_DIR)/$(EXECUTABLE_MIN)

# Copy this directory to the test Burrow source directory.
install-test:
	mkdir -p $(TEST_BURROW_SOURCE_DIR)
	rm -rf $(TEST_BURROW_SOURCE_DIR)
	rsync -a --delete \
		--exclude '.cache' \
		--exclude '.git' \
		--exclude '.husky' \
		--exclude 'docs' \
		--exclude 'node_modules' \
		--exclude 'build' \
		./ $(TEST_BURROW_SOURCE_DIR)
