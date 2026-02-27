package kernel

import (
	"fmt"
	"go/format"
	"os"
	"os/exec"
	"path/filepath"
	"slices"
	"strings"

	"golang.org/x/mod/semver"
)

// Version is the version of Burrow.
const Version = "2026.1.0"

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

// CartonsFileName is the name of the file that contains the installed carton
// URLs.
const CartonsFileName = "cartons"

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

	// Allow overrides from process environment variables prefixed with
	// "BURROW_". For example, BURROW_ROOT_CHAMBER overrides ROOT_CHAMBER.
	for _, envVar := range os.Environ() {
		key, value, hasSep := strings.Cut(envVar, "=")
		if !hasSep || !strings.HasPrefix(key, "BURROW_") {
			continue
		}

		burrowKey := strings.TrimPrefix(key, "BURROW_")
		if burrowKey == "" {
			continue
		}

		b.Env.Set(burrowKey, value)
	}

	// Ensure the config directory exists so follow-up reads/writes can happen
	// without additional setup steps.
	if err := os.MkdirAll(b.GetConfigDir(), 0o755); err != nil {
		return fmt.Errorf("failed to create burrow directory: %w", err)
	}

	return nil
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

// GetCartonsFilePath returns the file path of the cartons file in the burrow.
func (b *Burrow) GetCartonsFilePath() string {
	return filepath.Join(b.GetDataDir(), CartonsFileName)
}

// GetCartonURLs reads the cartons file in the burrow and returns the list of
// carton URLs. If the cartons file does not exist, it returns an empty list.
func (b *Burrow) GetCartonURLs() ([]string, error) {
	cartonsFilePath := b.GetCartonsFilePath()
	data, err := os.ReadFile(cartonsFilePath)
	if err != nil {
		if os.IsNotExist(err) {
			return []string{}, nil
		}
		return nil, fmt.Errorf("failed to read cartons file: %w", err)
	}

	lines := string(data)
	cartonURLs := []string{}
	for line := range strings.SplitSeq(lines, "\n") {
		line = strings.TrimSpace(line)
		if line != "" {
			cartonURLs = append(cartonURLs, line)
		}
	}

	return cartonURLs, nil
}

// AddCartonsToCartonsFile adds the given carton URLs to the cartons file in
// the burrow.
func (b *Burrow) AddCartonsToCartonsFile(incomingURLs []string) error {
	currentURLs, err := b.GetCartonURLs()
	if err != nil {
		return fmt.Errorf("failed to get carton URLs: %w", err)
	}

	seenURLs := make(
		map[string]struct{},
		len(currentURLs)+len(incomingURLs),
	)
	for _, url := range currentURLs {
		url = strings.TrimSpace(url)
		if url == "" {
			continue
		}
		seenURLs[url] = struct{}{}
	}

	for _, url := range incomingURLs {
		url = strings.TrimSpace(url)
		if url == "" {
			continue
		}

		if _, exists := seenURLs[url]; exists {
			continue
		}

		currentURLs = append(currentURLs, url)
		seenURLs[url] = struct{}{}
	}

	cartonsFilePath := b.GetCartonsFilePath()
	cartonsDirPath := filepath.Dir(cartonsFilePath)
	if err := os.MkdirAll(cartonsDirPath, 0o755); err != nil {
		return fmt.Errorf("failed to create cartons directory: %w", err)
	}

	content := strings.Join(currentURLs, "\n")
	if content != "" {
		content += "\n"
	}
	if err := os.WriteFile(
		cartonsFilePath,
		[]byte(content),
		0o644,
	); err != nil {
		return fmt.Errorf("failed to write cartons file: %w", err)
	}

	return nil
}

// RemoveCartonsFromCartonsFile removes the given carton URLs from the cartons
// file in the burrow.
func (b *Burrow) RemoveCartonsFromCartonsFile(incomingURLs []string) error {
	currentURLs, err := b.GetCartonURLs()
	if err != nil {
		return fmt.Errorf("failed to get carton URLs: %w", err)
	}

	removedURLs := make(map[string]struct{}, len(incomingURLs))
	for _, url := range incomingURLs {
		url = strings.TrimSpace(url)
		if url == "" {
			continue
		}
		removedURLs[url] = struct{}{}
	}

	if len(removedURLs) == 0 {
		return nil
	}

	nextURLs := make([]string, 0, len(currentURLs))
	changed := false
	for _, url := range currentURLs {
		url = strings.TrimSpace(url)
		if url == "" {
			changed = true
			continue
		}

		if _, remove := removedURLs[url]; remove {
			changed = true
			continue
		}

		nextURLs = append(nextURLs, url)
	}

	if !changed {
		return nil
	}

	cartonsFilePath := b.GetCartonsFilePath()
	cartonsDirPath := filepath.Dir(cartonsFilePath)
	if err := os.MkdirAll(cartonsDirPath, 0o755); err != nil {
		return fmt.Errorf("failed to create cartons directory: %w", err)
	}

	content := strings.Join(nextURLs, "\n")
	if content != "" {
		content += "\n"
	}
	if err := os.WriteFile(
		cartonsFilePath,
		[]byte(content),
		0o644,
	); err != nil {
		return fmt.Errorf("failed to write cartons file: %w", err)
	}

	return nil
}

// GetBurrowSourceDir returns the source directory of the burrow.
func (b *Burrow) GetBurrowSourceDir() string {
	return filepath.Join(b.GetDataDir(), "source")
}

func getMajorAndMinorVersionPrefix() (string, string, error) {
	version := Version
	if !strings.HasPrefix(version, "v") {
		version = "v" + version
	}
	if !semver.IsValid(version) {
		return "", "", fmt.Errorf("invalid version %q", Version)
	}

	majorVersion := semver.Major(version)
	if majorVersion == "" {
		return "", "", fmt.Errorf(
			"invalid version %q: empty major version",
			Version,
		)
	}

	return majorVersion, semver.MajorMinor(version), nil
}

func normalizeCartonModulePaths(
	cartonURLs []string,
	majorVersion string,
) []string {
	cleanedCartonURLs := make([]string, 0, len(cartonURLs))
	seenURLs := make(map[string]struct{}, len(cartonURLs))
	majorSuffix := "/" + majorVersion

	for _, cartonURL := range cartonURLs {
		cartonURL = strings.TrimSpace(cartonURL)
		if cartonURL == "" {
			continue
		}

		versionedCartonURL := cartonURL
		if !strings.HasSuffix(versionedCartonURL, majorSuffix) {
			versionedCartonURL += majorSuffix
		}

		if _, exists := seenURLs[versionedCartonURL]; exists {
			continue
		}

		seenURLs[versionedCartonURL] = struct{}{}
		cleanedCartonURLs = append(cleanedCartonURLs, versionedCartonURL)
	}

	slices.Sort(cleanedCartonURLs)

	return cleanedCartonURLs
}

func runGoCommand(dir string, args ...string) error {
	cmd := exec.Command("go", args...)
	if dir != "" {
		cmd.Dir = dir
	}

	output, err := cmd.CombinedOutput()
	if err != nil {
		return fmt.Errorf(
			"failed to run go %s: %w: %s",
			strings.Join(args, " "),
			err,
			strings.TrimSpace(string(output)),
		)
	}

	return nil
}

// GenerateMagicGoModFile generates a magic.go.mod file in the burrow's source
// directory that includes the carton URLs as dependencies. This allows the
// burrow to use the cartons as Go modules when building chambers.
func (b *Burrow) GenerateMagicGoModFile() error {
	cartonURLs, err := b.GetCartonURLs()
	if err != nil {
		return fmt.Errorf("failed to get carton URLs: %w", err)
	}

	// Create a magic.go.mod file.
	burrowSourceDir := b.GetBurrowSourceDir()
	if err := os.MkdirAll(burrowSourceDir, 0o755); err != nil {
		return fmt.Errorf("failed to create burrow source directory: %w", err)
	}

	sourceGoModPath := filepath.Join(burrowSourceDir, "go.mod")
	sourceGoModData, err := os.ReadFile(sourceGoModPath)
	if err != nil {
		return fmt.Errorf("failed to read source go.mod: %w", err)
	}

	majorVersion, versionPrefix, err := getMajorAndMinorVersionPrefix()
	if err != nil {
		return err
	}

	cleanedCartonURLs := normalizeCartonModulePaths(cartonURLs, majorVersion)

	magicGoModPath := filepath.Join(burrowSourceDir, "magic.go.mod")
	if err := os.WriteFile(
		magicGoModPath,
		sourceGoModData,
		0o644,
	); err != nil {
		return fmt.Errorf("failed to write magic.go.mod: %w", err)
	}

	for _, versionedCartonURL := range cleanedCartonURLs {
		query := versionedCartonURL + "@" + versionPrefix
		if err := runGoCommand(
			burrowSourceDir,
			"get",
			"-modfile=magic.go.mod",
			query,
		); err != nil {
			return err
		}
	}

	return nil
}

func (b *Burrow) GenerateMagicGoFile(withCartons bool) error {
	cartonURLs := []string{}
	if withCartons {
		var err error
		cartonURLs, err = b.GetCartonURLs()
		if err != nil {
			return fmt.Errorf("failed to get carton URLs: %w", err)
		}
	}

	majorVersion, _, err := getMajorAndMinorVersionPrefix()
	if err != nil {
		return err
	}

	imports := []string{
		`"github.com/TypingHare/burrow/v2026/burrow"`,
		`"github.com/TypingHare/burrow/v2026/kernel"`,
	}
	registerCalls := []string{"\tburrow.RegisterCarton(warehouse)"}

	if withCartons {
		type cartonSpec struct {
			packageName string
			importPath  string
		}

		cartons := make([]cartonSpec, 0, len(cartonURLs))
		seenImports := make(map[string]struct{}, len(cartonURLs))

		for _, versionedCartonURL := range normalizeCartonModulePaths(
			cartonURLs,
			majorVersion,
		) {
			cartonURL := strings.TrimSuffix(
				versionedCartonURL,
				"/"+majorVersion,
			)
			base := filepath.Base(cartonURL)
			packageName := strings.TrimSuffix(base, ".carton")
			packageName = strings.TrimSpace(packageName)
			if packageName == "" {
				return fmt.Errorf("invalid carton URL %q", cartonURL)
			}

			importPath := versionedCartonURL + "/" + packageName
			if _, exists := seenImports[importPath]; exists {
				continue
			}

			seenImports[importPath] = struct{}{}
			cartons = append(cartons, cartonSpec{
				packageName: packageName,
				importPath:  importPath,
			})
		}

		slices.SortFunc(cartons, func(a, b cartonSpec) int {
			return strings.Compare(a.importPath, b.importPath)
		})

		for _, carton := range cartons {
			imports = append(imports, fmt.Sprintf(`"%s"`, carton.importPath))
			registerCalls = append(
				registerCalls,
				fmt.Sprintf(
					"\t%s.RegisterCarton(warehouse)",
					carton.packageName,
				),
			)
		}
	}

	var content strings.Builder
	content.WriteString("package main\n\n")
	content.WriteString("import (\n")
	for _, importPath := range imports {
		content.WriteString("\t")
		content.WriteString(importPath)
		content.WriteString("\n")
	}
	content.WriteString(")\n\n")
	content.WriteString("func registerCarton(warehouse *kernel.Warehouse) {\n")
	for _, registerCall := range registerCalls {
		content.WriteString(registerCall)
		content.WriteString("\n")
	}
	content.WriteString("}\n")

	formattedSource, err := format.Source([]byte(content.String()))
	if err != nil {
		return fmt.Errorf("failed to format magic.go: %w", err)
	}

	magicGoPath := filepath.Join("cmd", "magic.go")
	if err := os.WriteFile(magicGoPath, formattedSource, 0o644); err != nil {
		return fmt.Errorf("failed to write magic.go: %w", err)
	}

	return nil
}

// Build the burrow executable using proper command.
func (b *Burrow) Build(withCartons bool, outputExecutablePath string) error {
	if err := b.GenerateMagicGoFile(withCartons); err != nil {
		return fmt.Errorf("failed to generate magic.go: %w", err)
	}

	if err := os.MkdirAll("build", 0o755); err != nil {
		return fmt.Errorf("failed to create build directory: %w", err)
	}

	buildArgs := []string{"build", "-o", outputExecutablePath}
	if withCartons {
		if err := b.GenerateMagicGoModFile(); err != nil {
			return fmt.Errorf("failed to generate magic.go.mod: %w", err)
		}

		magicGoModPath := filepath.Join(b.GetBurrowSourceDir(), "magic.go.mod")
		if err := runGoCommand(
			"",
			"mod",
			"tidy",
			"-modfile="+magicGoModPath,
		); err != nil {
			return err
		}

		buildArgs = append(buildArgs, "-modfile="+magicGoModPath)
	}

	buildArgs = append(buildArgs, "./cmd")
	if err := runGoCommand("", buildArgs...); err != nil {
		return err
	}

	return nil
}

// GetBin returns the path to the burrow executable in the bin directory of the
// burrow. If the executable does not exist, it returns an error.
func (b *Burrow) GetBinDir() string {
	return filepath.Join(b.GetDataDir(), b.Env.Get(EnvBinDir))
}
