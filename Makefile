BUILD_DIR = build

.PHONY: build run

# Builds the project and outputs the binary to the build directory.
build:
	go build -o $(BUILD_DIR)/burrow ./cmd
