BUILD_DIR := build
EXECUTABLE := burrow
EXECUTABLE_MIN := burrow-min
DEV_BURROW_SOURCE_DIR := ~/.local/share/burrow-dev/source/TypingHare/burrow
MAGIC_FILE := cmd/magic.go

.PHONY: help fmt test build build-min clean install-dev magic

# help prints the available targets.
help:
	@printf '%s\n' \
		'Available targets:' \
		'  make magic        Generate cmd/magic.go' \
		'  make build        Build the main executable into $(BUILD_DIR)' \
		'  make build-min    Copy the built executable to $(EXECUTABLE_MIN)' \
		'  make fmt          Format Go source files' \
		'  make test         Run Go tests with a writable cache' \
		'  make clean        Remove build artifacts and local caches' \
		'  make install-dev  Sync the repository to the dev Burrow source tree'

# fmt formats Go source files in the module.
fmt:
	gofmt -w $$(find . -name '*.go' -not -path './build/*')

# magic generates cmd/magic.go for the CLI entrypoint.
magic: $(MAGIC_FILE)

$(MAGIC_FILE):
	mkdir -p $(dir $@)
	printf '%s\n' \
		'package main' \
		'' \
		'import (' \
		'	"github.com/TypingHare/burrow/v2026/burrow"' \
		'	"github.com/TypingHare/burrow/v2026/kernel"' \
		')' \
		'' \
		'func registerCartons(warehouse *kernel.Warehouse) {' \
		'	burrow.RegisterCartonTo(warehouse)' \
		'}' > $@

# test runs Go tests using a cache directory that works in the local sandbox.
test: $(MAGIC_FILE)
	go test ./...

# build compiles the Burrow CLI into the build directory.
build: $(MAGIC_FILE)
	mkdir -p $(BUILD_DIR)
	go build -o $(BUILD_DIR)/$(EXECUTABLE) ./cmd

# build-min creates the minimal executable copy expected by local tooling.
build-min: build
	cp $(BUILD_DIR)/$(EXECUTABLE) $(BUILD_DIR)/$(EXECUTABLE_MIN)

# clean removes local build outputs and caches created by this Makefile.
clean:
	rm -rf $(BUILD_DIR) .cache /tmp/burrow-gocache $(MAGIC_FILE)

# install-dev syncs this repository into the local burrow-dev source tree.
install-dev:
	mkdir -p $(DEV_BURROW_SOURCE_DIR)
	rm -rf $(DEV_BURROW_SOURCE_DIR)
	rsync -a --delete \
		--exclude '.cache' \
		--exclude '.gocache' \
		--exclude '.git' \
		--exclude '.husky' \
		--exclude 'docs' \
		--exclude 'node_modules' \
		--exclude 'build' \
		./ $(DEV_BURROW_SOURCE_DIR)
