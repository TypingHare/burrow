package kernel

import "path/filepath"

const Version = "2026.1.0"

const CartonName = "github.com/TypingHare/burrow"

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

	EnvDebugLevel = "DEBUG_LEVEL"

	EnvExecutablePath        = "EXECUTABLE_PATH"
	EnvMinimalExecutablePath = "MINIMAL_EXECUTABLE_PATH"
)

type Burrow struct {
	// Env stores environment variables for the Burrow.
	Env Vars

	// warehouse is the Warehouse that manages the cartons in the Burrow.
	Warehouse *Warehouse

	// architect is the Architect that manages the chambers in the Burrow.
	Architect *Architect
}

// NewBurrow creates a new Burrow instance.
func NewBurrow() *Burrow {
	burrow := &Burrow{Env: NewVars()}
	burrow.Warehouse = NewWarehouse(burrow)
	burrow.Architect = NewArchitect(burrow)

	return burrow
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
