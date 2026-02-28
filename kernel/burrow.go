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
const Version = "2026.0.0"

// Environment variable names used by Burrow.
const (
	EnvName        = "NAME"
	EnvHome        = "HOME"
	EnvConfigHome  = "CONFIG_HOME"
	EnvDataHome    = "DATA_HOME"
	EnvStateHome   = "STATE_HOME"
	EnvBinDir      = "BIN_DIR"
	EnvChambersDir = "CHAMBERS_DIR"

	EnvRootChamber       = "ROOT_CHAMBER"
	EnvBlueprintFileName = "BLUEPRINT_FILE_NAME"
	EnvDecorationIDSep   = "DECORATION_ID_SEP"

	EnvVerbose = "VERBOSE"
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

// Init initializes the Burrow with the given name and sets up the environment
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
	b.Env.Set(EnvVerbose, "0")
	b.Env.Set(EnvBlueprintFileName, "blueprint.json")
	b.Env.Set(EnvDecorationIDSep, "@")

	// Allow overrides from process environment variables prefixed with
	// "BURROW_". For example, BURROW_NAME overrides NAME.
	for _, envVar := range os.Environ() {
		key, value, hasSep := strings.Cut(envVar, "=")
		if !hasSep || !strings.HasPrefix(key, "BURROW_") {
			continue
		}

		b.Env.Set(strings.TrimPrefix(key, "BURROW_"), value)
	}

	// TODO
	// Ensure the config directory exists so follow-up reads/writes can happen
	// without additional setup steps.
	// if err := os.MkdirAll(b.GetConfigDir(), 0o755); err != nil {
	// 	return fmt.Errorf("failed to create burrow directory: %w", err)
	// }

	return nil
}

// Handle handles the execution of a command the Burrow receives.
func (b *Burrow) Handle(args []string) (int, error) {
	if len(args) == 0 {
		return GeneralError, fmt.Errorf("no chamber specified")
	}
	chamberName := args[0]
	chamberArgs := args[1:]

	chamber, err := b.architect.GetOrDig(chamberName)
	if err != nil {
		return GeneralError, fmt.Errorf(
			"failed to get or dig chamber: %w",
			err,
		)
	}

	handler := chamber.Handler
	if handler == nil {
		return GeneralError, fmt.Errorf("chamber handler is nil")
	}

	return handler(chamber, chamberArgs)
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
