package kernel

import (
	"errors"
	"fmt"
	"os"
	"path/filepath"
	"strings"
)

// Version is Burrow's CalSemVer version in YYYY.minor.patch form.
//
// The major version is the release year. The minor version increments for each
// release within that year. The patch version increments for bug-fix releases.
//
// Burrow expects API and behavioral compatibility within the same major and
// minor version, and only links cartons that match both.
const Version = "2026.0.0"

// CartonName is the carton name of Burrow itself.
//
// It is equivalent to the Burrow repository path. When the repository is moved,
// this should be updated accordingly.
const CartonName = "github.com/TypingHare/burrow"

// DecorationIDSep joins decoration and carton names in a decoration ID.
const DecorationIDSep = "@"

// Environment variable names used by Burrow.
const (
	EnvName       = "NAME"
	EnvHome       = "HOME"
	EnvConfigHome = "CONFIG_HOME"
	EnvDataHome   = "DATA_HOME"
	EnvStateHome  = "STATE_HOME"
	EnvBinDir     = "BIN_DIR"
	EnvChamberDir = "CHAMBER_DIR"
	EnvSourceDir  = "SOURCE_DIR"

	EnvAcceptProcessEnv = "ACCEPT_PROCESS_ENV"
	EnvUseChamber       = "USE_CHAMBER"

	EnvRootChamber       = "ROOT_CHAMBER"
	EnvBlueprintFileName = "BLUEPRINT_FILE_NAME"

	EnvDebug = "DEBUG"

	EnvExecutablePath        = "EXECUTABLE_PATH"
	EnvMinimalExecutablePath = "MINIMAL_EXECUTABLE_PATH"
)

// Burrow manages a collection of in-memory CLI applications called chambers.
type Burrow struct {
	// Env stores environment variables for the Burrow.
	Env Vars

	// warehouse is the Warehouse that manages the cartons in the Burrow.
	warehouse *Warehouse

	// architect is the Architect that manages the chambers in the Burrow.
	architect *Architect
}

// NewBurrow creates a new Burrow instance.
func NewBurrow() *Burrow {
	burrow := &Burrow{Env: NewVars()}
	burrow.warehouse = NewWarehouse(burrow)
	burrow.architect = NewArchitect(burrow)

	return burrow
}

// Warehouse returns the Warehouse that manages the cartons in the Burrow.
func (b *Burrow) Warehouse() *Warehouse {
	return b.warehouse
}

// Architect returns the Architect that manages the chambers in the Burrow.
func (b *Burrow) Architect() *Architect {
	return b.architect
}

// InitEnv initializes the environment variables for b.
func (b *Burrow) InitEnv(name string) error {
	b.Env.Set(EnvName, name)

	home, err := os.UserHomeDir()
	if err != nil {
		return fmt.Errorf("unable to determine user home directory: %w", err)
	}
	b.Env.Set(EnvHome, home)

	configHome := os.Getenv("XDG_CONFIG_HOME")
	if configHome == "" {
		configHome = filepath.Join(home, ".config")
	}
	b.Env.Set(EnvConfigHome, configHome)

	dataHome := os.Getenv("XDG_DATA_HOME")
	if dataHome == "" {
		dataHome = filepath.Join(home, ".local", "share")
	}
	b.Env.Set(EnvDataHome, dataHome)

	stateHome := os.Getenv("XDG_STATE_HOME")
	if stateHome == "" {
		stateHome = filepath.Join(home, ".local", "state")
	}
	b.Env.Set(EnvStateHome, stateHome)

	// Set default values for other environment variables.
	b.Env.Set(EnvBinDir, "bin")
	b.Env.Set(EnvChamberDir, "chamber")
	b.Env.Set(EnvSourceDir, "source")
	b.Env.Set(EnvAcceptProcessEnv, "1")
	b.Env.Set(EnvUseChamber, "")
	b.Env.Set(EnvRootChamber, ".")
	b.Env.Set(EnvBlueprintFileName, "blueprint.json")
	b.Env.Set(EnvDebug, "0")
	b.Env.Set(EnvExecutablePath, "burrow")
	b.Env.Set(EnvMinimalExecutablePath, "burrow-min")

	return nil
}

// LoadProcessEnv loads environment variables from the process environment if
// allowed by the EnvAcceptProcessEnv variable.
func (b *Burrow) LoadProcessEnv() {
	if b.Env.Get(EnvAcceptProcessEnv) == "1" {
		for _, envVar := range os.Environ() {
			key, value, hasSep := strings.Cut(envVar, "=")
			if !hasSep || !strings.HasPrefix(key, "BURROW_") {
				continue
			}

			b.Env.Set(strings.TrimPrefix(key, "BURROW_"), value)
		}
	}
}

// Handle handles the execution of a command the Burrow receives.
func (b *Burrow) Handle(args []string) (int, error) {
	useChamber := b.Env.Get(EnvUseChamber)
	if len(args) == 0 && useChamber == "" {
		return GeneralError, fmt.Errorf("no chamber specified")
	}

	var chamberName string
	var chamberArgs []string
	if useChamber == "" {
		chamberName = args[0]
		chamberArgs = args[1:]
	} else {
		chamberName = useChamber
		chamberArgs = args
	}

	chamber, err := b.architect.GetOrDig(chamberName)
	if err != nil {
		return GeneralError, fmt.Errorf(
			"failed to get or dig chamber %q: %w",
			chamberName,
			err,
		)
	}

	handler := chamber.Handler
	if handler == nil {
		return GeneralError, fmt.Errorf("chamber handler is nil")
	}

	return handler(chamber, chamberArgs)
}

// Bury shuts down all currently dug chambers and persists their blueprints.
func (b *Burrow) Bury() error {
	for chamberName := range b.architect.chamberMap {
		if err := b.architect.Bury(chamberName); err != nil {
			return fmt.Errorf(
				"failed to bury chamber %q: %w",
				chamberName,
				err,
			)
		}
	}

	return nil
}

// PrintErrorStack prints an error and its unwrap chain to stderr from outermost
// to innermost with stable numeric indices.
func (b *Burrow) PrintErrorStack(err error) {
	stack := []string{}

	currentErr := err
	for currentErr != nil {
		currErrMsg := currentErr.Error()
		nextErr := errors.Unwrap(currentErr)
		if nextErr == nil {
			stack = append(stack, currErrMsg)
		} else {
			nextErrMsg := nextErr.Error()
			if len(nextErrMsg) < len(currErrMsg) &&
				strings.HasSuffix(currErrMsg, nextErrMsg) {
				currErrMsg = strings.TrimSuffix(currErrMsg, nextErrMsg)
				currErrMsg = strings.TrimSuffix(currErrMsg, ": ")
			}
			stack = append(stack, currErrMsg)
		}

		currentErr = nextErr
	}

	i := 0
	for _, errMsg := range stack {
		fmt.Fprintf(os.Stderr, "(%d) %s\n", i, errMsg)
		i++
	}
}

// GetConfigDir returns the config directory of the burrow.
func (b *Burrow) GetConfigDir() string {
	return filepath.Join(b.Env.Get(EnvConfigHome), b.Env.Get(EnvName))
}

// GetDataDir returns the data directory of the burrow.
func (b *Burrow) GetDataDir() string {
	return filepath.Join(b.Env.Get(EnvDataHome), b.Env.Get(EnvName))
}

// GetStateDir returns the state directory of the burrow.
func (b *Burrow) GetStateDir() string {
	return filepath.Join(b.Env.Get(EnvStateHome), b.Env.Get(EnvName))
}

// GetBinDir returns the bin directory of the burrow.
func (b *Burrow) GetBinDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvBinDir))
}

// GetChamberDir returns the chamber directory of the burrow.
func (b *Burrow) GetChamberDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvChamberDir))
}

// GetSourceDir returns the source directory of the burrow.
func (b *Burrow) GetSourceDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvSourceDir))
}
