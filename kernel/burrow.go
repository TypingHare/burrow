// Package kernel provides Burrow's runtime primitives for chambers, cartons,
// decors, and command execution.
package kernel

import (
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
const Version = "2026.1.0"

// CartonName is the carton name of Burrow itself.
//
// It is equivalent to the Burrow repository path. When the repository is moved,
// this should be updated accordingly.
const CartonName = "github.com/TypingHare/burrow"

const (
	// EnvName is the Burrow application name key.
	EnvName = "NAME"

	// EnvHome is the user home directory key.
	EnvHome = "HOME"

	// EnvConfigHome is the config home directory key.
	EnvConfigHome = "CONFIG_HOME"

	// EnvDataHome is the data home directory key.
	EnvDataHome = "DATA_HOME"

	// EnvStateHome is the state home directory key.
	EnvStateHome = "STATE_HOME"

	// EnvSourceDir is the source directory key.
	EnvSourceDir = "SOURCE_DIR"

	// EnvChamberDir is the chamber directory key.
	EnvChamberDir = "CHAMBER_DIR"

	// EnvBinDir is the binary directory key.
	EnvBinDir = "BIN_DIR"

	// EnvBlueprintFileName is the blueprint file name key.
	EnvBlueprintFileName = "BLUEPRINT_FILE_NAME"

	// EnvAcceptProcessEnv controls whether process environment variables are
	// loaded into Burrow.
	EnvAcceptProcessEnv = "ACCEPT_PROCESS_ENV"

	// EnvUseChamber specifies the chamber to use for the current command.
	EnvUseChamber = "USE_CHAMBER"

	// EnvRootChamberName is the name of the root chamber, which is a special
	// chamber that manages all other chambers and provides shared resources.
	EnvRootChamberName = "ROOT_CHAMBER_NAME"

	// EnvExecutablePath is the path to the currently executing Burrow binary.
	EnvExecutablePath = "EXECUTABLE_PATH"

	// EnvMinimalExecutablePath is the path to the minimal Burrow executable.
	EnvMinimalExecutablePath = "MINIMAL_EXECUTABLE_PATH"
)

// Burrow manages a collection of in-memory CLI applications called chambers.
type Burrow struct {
	// Env stores environment variables for the Burrow.
	Env Vars

	// Warehouse is the Warehouse that manages the cartons in the Burrow.
	Warehouse *Warehouse

	// Architect is the Architect that manages the chambers in the Burrow.
	Architect *Architect
}

// NewBurrow returns a Burrow with its Warehouse and Architect initialized.
func NewBurrow() *Burrow {
	burrow := &Burrow{Env: NewVars()}
	burrow.Warehouse = NewWarehouse(burrow)
	burrow.Architect = NewArchitect(burrow)

	return burrow
}

// InitEnv initializes the Burrow environment for name.
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

	// Use relative defaults under the data directory for Burrow-managed paths.
	b.Env.Set(EnvSourceDir, "source")
	b.Env.Set(EnvChamberDir, "chamber")
	b.Env.Set(EnvBinDir, "bin")
	b.Env.Set(EnvBlueprintFileName, "blueprint.toml")
	b.Env.Set(EnvAcceptProcessEnv, "1")
	b.Env.Set(EnvUseChamber, "")
	b.Env.Set(EnvRootChamberName, ".")

	return nil
}

// LoadProcessEnv loads BURROW_ environment variables when process environment
// loading is enabled.
func (b *Burrow) LoadProcessEnv() {
	if b.Env.Get(EnvAcceptProcessEnv) != "1" {
		return
	}

	for _, envVar := range os.Environ() {
		key, value, hasSep := strings.Cut(envVar, "=")
		if !hasSep || !strings.HasPrefix(key, "BURROW_") {
			continue
		}

		b.Env.Set(strings.TrimPrefix(key, "BURROW_"), value)
	}
}

// GetConfigDir returns the Burrow config directory.
func (b *Burrow) GetConfigDir() string {
	return filepath.Join(b.Env.Get(EnvConfigHome), b.Env.Get(EnvName))
}

// GetDataDir returns the Burrow data directory.
func (b *Burrow) GetDataDir() string {
	return filepath.Join(b.Env.Get(EnvDataHome), b.Env.Get(EnvName))
}

// GetStateDir returns the Burrow state directory.
func (b *Burrow) GetStateDir() string {
	return filepath.Join(b.Env.Get(EnvStateHome), b.Env.Get(EnvName))
}

// GetSourceDir returns the Burrow source directory.
func (b *Burrow) GetSourceDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvSourceDir))
}

// GetChamberDir returns the Burrow chamber directory.
func (b *Burrow) GetChamberDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvChamberDir))
}

// GetBinDir returns the Burrow binary directory.
func (b *Burrow) GetBinDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvBinDir))
}

// Handle handles the execution of a command the Burrow receives.
func (b *Burrow) Handle(args []string) (int, error) {
	// EnvUseChamber overrides the chamber name from the argument list.
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

	chamber, err := b.Architect.GetChamberOrCreate(chamberName)
	if err != nil {
		return GeneralError, fmt.Errorf(
			"failed to get or create chamber %q: %w",
			chamberName,
			err,
		)
	}

	commandHandler := chamber.CommandHandler
	if commandHandler == nil {
		return GeneralError, fmt.Errorf(
			"command handler in chamber %q is nil",
			chamberName,
		)
	}

	exitCode, err := commandHandler(chamber, chamberArgs)
	if err != nil {
		errorHandler := chamber.ErrorHandler
		if errorHandler == nil {
			return GeneralError, fmt.Errorf(
				"error handler in chamber %q is nil",
				chamberName,
			)
		}

		return errorHandler(chamber, chamberArgs, exitCode, err), nil
	}

	return exitCode, nil
}

// Destroy deletes all chambers in the Burrow and cleans up their resources.
func (b *Burrow) Destroy() error {
	for chamberName := range b.Architect.ChambersByNames {
		if err := b.Architect.Delete(chamberName); err != nil {
			return fmt.Errorf(
				"failed to delete chamber %q: %w",
				chamberName,
				err,
			)
		}
	}

	return nil
}
