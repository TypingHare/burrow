package kernel

import (
	"fmt"
	"os"
	"path/filepath"
)

const (
	EnvName        = "NAME"
	EnvHome        = "HOME"
	EnvConfigHome  = "CONFIG_HOME"
	EnvDataHome    = "DATA_HOME"
	EnvStateHome   = "STATE_HOME"
	EnvBinDir      = "BIN_DIR"
	EnvChambersDir = "CHAMBERS_DIR"
	EnvRootChamber = "ROOT_CHAMBER"
)

// Burrow manages a collection of in-memory CLI applications called chambers.
type Burrow struct {
	// Env stores environment variables for the Burrow.
	Env Vars

	// architect is the Architect that manages the chambers in the Burrow.
	architect *Architect

	// warehouse is the Warehouse that manages the cartons in the Burrow.
	warehouse *Warehouse
}

// NewBurrow creates a new burrow.
func NewBurrow() *Burrow {
	burrow := &Burrow{Env: NewVars()}
	burrow.architect = NewArchitect(burrow)
	burrow.warehouse = NewWarehouse(burrow)

	return burrow
}

// Architect returns the Architect that manages the chambers in the Burrow.
func (b *Burrow) Architect() *Architect {
	return b.architect
}

// Warehouse returns the Warehouse that manages the cartons in the Burrow.
func (b *Burrow) Warehouse() *Warehouse {
	return b.warehouse
}

// Init initializes the burrow with the given name and sets up the environment
// variables.
func (b *Burrow) Init(name string) error {
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
	b.Env.Set(EnvChambersDir, "chambers")
	b.Env.Set(EnvRootChamber, ".")

	return nil
}

// GetDir returns the directory of the burrow.
func (b *Burrow) GetDir() string {
	return filepath.Join(b.Env.Get(EnvConfigHome), b.Env.Get(EnvName))
}

// Handle handles the execution of a command in the burrow.
func (b *Burrow) Handle(args []string) (int, error) {
	if len(args) == 0 {
		return GENERAL_ERROR, fmt.Errorf("no chamber specified")
	}
	chamberName := args[0]
	chamberArgs := args[1:]

	chamber, err := b.architect.GetOrDig(chamberName)
	if err != nil {
		return GENERAL_ERROR, fmt.Errorf(
			"failed to get or dig chamber: %w",
			err,
		)
	}

	handler := chamber.Handler
	if handler == nil {
		return GENERAL_ERROR, fmt.Errorf("chamber handler is nil")
	}

	return handler(chamber, chamberArgs)
}
