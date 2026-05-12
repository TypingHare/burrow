# The directory containing all build artifacts.
BUILD_DIR := build

# The file name of the Burrow executable.
EXECUTABLE := burrow

# The file name of the minimal Burrow executable, which only contains the
# built-in carton.
EXECUTABLE_MIN := burrow-min

# The root directory of the `burrow-dev` Burrow application. This application is
# being used and tested in the development environment.
DEV_BURROW_DIR := $(HOME)/.local/share/burrow-dev

# The source directory of Burrow in the `burrow-dev` Burrow application. It
# contains the entire sources code of Burrow.
DEV_BURROW_SOURCE_DIR := $(DEV_BURROW_DIR)/source/github.com/TypingHare/burrow

# The binary directory in the `burrow-dev` Burrow application.
DEV_BURROW_BIN_DIR := $(DEV_BURROW_DIR)/bin

# The path to the Burrow magic file.
MAGIC_FILE := cmd/magic.go

# Phony targets.
PHONY: clean install-dev

$(MAGIC_FILE):
	pas

# Build the Burrow executable.
$(BUILD_DIR)/$(EXECUTABLE): $(MAGIC_FILE)
	mkdir -p $(BUILD_DIR)
	go build -o $@ ./cmd

# Build the minimal Burrow executable. In the source directory, because no
# cartons are specified in the Burrow magic file, so minimal Burrow executable
# is the same as the Burrow executable.
$(BUILD_DIR)/$(EXECUTABLE_MIN): $(MAGIC_FILE)
	cp $(BUILD_DIR)/$(EXECUTABLE) $(BUILD_DIR)/$(EXECUTABLE_MIN)

# Format Go source files in the module.
format:
	gofmt -w $$(find . -name '*.go' -not -path $(BUILD_DIR))

# Remove local build outputs and caches.
clean:
	rm -rf $(BUILD_DIR) .cache $(MAGIC_FILE)

# install-dev syncs this repository into the local burrow-dev source tree.
install-dev:
	mkdir -p $(DEV_BURROW_SOURCE_DIR)
	rm -rf $(DEV_BURROW_SOURCE_DIR)
	rsync -a --delete \
		--exclude '.gocache' \
		--exclude '.git' \
		--exclude '.husky' \
		--exclude 'docs' \
		--exclude 'node_modules' \
		--exclude 'build' \
		./ $(DEV_BURROW_SOURCE_DIR)
	cd $(DEV_BURROW_SOURCE_DIR) && make $(BUILD_DIR)/$(EXECUTABLE)
	cd $(DEV_BURROW_SOURCE_DIR) && make $(BUILD_DIR)/$(EXECUTABLE_MIN)
	mv $(DEV_BURROW_SOURCE_DIR)/$(BUILD_DIR)/* $(DEV_BURROW_BIN_DIR)/
