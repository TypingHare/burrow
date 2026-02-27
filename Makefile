BUILD_DIR = build

.PHONY: build install

# Builds the project and outputs the binary to the build directory.
build:
	go build -o $(BUILD_DIR)/burrow ./cmd

install-test:
	mkdir -p ~/.local/share/burrow-test
	rm -rf ~/.local/share/burrow-test/source
	cp -rf . ~/.local/share/burrow-test/source
