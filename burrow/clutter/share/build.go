package share

import (
	"fmt"
	"go/format"
	"os"
	"path/filepath"
	"strings"
	"unicode"

	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

const (
	GoModFileName      = "go.mod"
	MagicGoModFilePath = "magic.go.mod"
	MagicGoFilePath    = "cmd/magic.go"
)

type Builder struct {
	// BurrowSourceDir is the directory that stores Burrow source code.
	BurrowSourceDir string

	// CartonNames is a slice of carton names to build the executable.
	CartonNames []string

	// LocalCartons is a slice of LocalCarton instances.
	LocalCartons []LocalCarton

	// MagicEnv is environment variables to set in the magic file.
	MagicEnv kernel.Vars

	// OutputExecutablePath is the output path of the executable.
	OutputExecutablePath string
}

// NewBuilder creates a new Builder instance with the specified parameters.
func NewBuilder(
	burrowSourceDir string,
	cartonNames []string,
	localCartons []LocalCarton,
	magicEnv kernel.Vars,
	outputExecutablePath string,
) *Builder {
	return &Builder{
		BurrowSourceDir:      burrowSourceDir,
		CartonNames:          cartonNames,
		LocalCartons:         localCartons,
		MagicEnv:             magicEnv,
		OutputExecutablePath: outputExecutablePath,
	}
}

func (b *Builder) GenerateMagicGoModFile() error {
	goModFilePath := filepath.Join(b.BurrowSourceDir, GoModFileName)
	_, err := os.Stat(goModFilePath)
	if os.IsNotExist(err) {
		return fmt.Errorf(
			"Go mod file %q does not exist",
			goModFilePath,
		)
	} else if err != nil {
		return fmt.Errorf(
			"failed to access Go mod file %q: %w",
			goModFilePath,
			err,
		)
	}

	// Remove magic Go mod file if it already exists to ensure a clean state.
	magicGoModFilePath := filepath.Join(b.BurrowSourceDir, MagicGoModFilePath)
	if _, err := os.Stat(magicGoModFilePath); err == nil {
		if err := os.Remove(magicGoModFilePath); err != nil {
			return fmt.Errorf(
				"failed to remove existing magic go mod file %q: %w",
				magicGoModFilePath,
				err,
			)
		}
	}

	// Copy the Go mod file to the magic Go mod file to preserve existing module
	// settings.
	originalGoModContent, err := os.ReadFile(goModFilePath)
	if err != nil {
		return fmt.Errorf("failed to read original %q: %w", goModFilePath, err)
	}
	if err := os.WriteFile(
		magicGoModFilePath,
		originalGoModContent,
		0o644,
	); err != nil {
		return fmt.Errorf(
			"failed to copy magic Go mod file %q from Go mod file: %w",
			magicGoModFilePath,
			err,
		)
	}

	// Build a temporary map that maps carton names to their local paths.
	localPathsByCartonNames := make(map[string]string)
	for _, localCarton := range b.LocalCartons {
		localPathsByCartonNames[localCarton.Name] = localCarton.Path
	}

	majorVersion := kernel.GetBurrowMajorFunction()
	majorMinorVersion := kernel.GetBurrowMajorMinorFunction()

	// For each carton, run "go get" to add it as a dependency in the magic Go
	// mod file if it is a remote carton; otherwise, run "go mod edit -replace"
	// to point to the local path and "go mod edit -require" to add it as a
	// dependency.
	for _, cartonName := range b.CartonNames {
		cartonURL := fmt.Sprintf(
			"%s/v%s@v%s",
			cartonName,
			majorVersion,
			majorMinorVersion,
		)

		localCartonPath, isLocalCarton := localPathsByCartonNames[cartonName]
		if !isLocalCarton || localCartonPath == "" {
			err := b.RunGoGetCommand(cartonName, cartonURL)
			if err != nil {
				return err
			}
		} else {
			modulePath := fmt.Sprintf("%s/v%s", cartonName, majorVersion)
			err := b.RunGoModEditCommand(
				cartonName,
				modulePath,
				localCartonPath,
				cartonURL,
			)
			if err != nil {
				return err
			}
		}
	}

	_, stderr, exitCode, err := share.RunExternalCommand(
		b.BurrowSourceDir,
		[]string{
			"go", "mod", "download", "-modfile=" + MagicGoModFilePath,
			"all",
		},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to run %q: %w",
			"go mod download",
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}

// RunGoGetCommand runs the "go get" command to add the specified carton as a
// dependency in the magic Go mod file.
func (b *Builder) RunGoGetCommand(cartonName string, cartonURL string) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		b.BurrowSourceDir,
		[]string{"go", "get", "-modfile=" + MagicGoModFilePath, cartonURL},
	)

	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to run the %q command for carton %q: %w",
			"go get",
			cartonName,
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}

// RunGoModEditCommand runs the "go mod edit" command to add the specified local
// carton as a replace directive and a require directive in the magic Go mod
// file.
func (b *Builder) RunGoModEditCommand(
	cartonName string,
	modulePath string,
	localCartonPath string,
	cartonURL string,
) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		b.BurrowSourceDir,
		[]string{
			"go", "mod", "edit", "-modfile=" + MagicGoModFilePath,
			fmt.Sprintf("-replace=%s=%s", modulePath, localCartonPath),
		},
	)

	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to run the %q command for local carton %q: %w",
			"go mod edit",
			cartonName,
			fmt.Errorf("%s", stderr),
		)
	}

	_, stderr, exitCode, err = share.RunExternalCommand(
		b.BurrowSourceDir,
		[]string{
			"go", "mod", "edit", "-modfile=" + MagicGoModFilePath,
			fmt.Sprintf("-require=%s", cartonURL),
		},
	)

	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to run the %q command for local carton %q: %w",
			"go mod edit",
			cartonName,
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}

// Generate a cmd/magic.go file that imports all cartons in the Burrow source
// directory.
func (b *Builder) GenerateMagicGoFile() error {
	majorVersion := kernel.GetBurrowMajorFunction()

	// Resolve package names for the cartons.
	packageNames := make([]string, 0, len(b.CartonNames))
	for _, cartonName := range b.CartonNames {
		lastSegment := filepath.Base(cartonName)
		if !strings.HasSuffix(lastSegment, ".carton") {
			return fmt.Errorf(
				"carton name %q does not end with %q",
				cartonName,
				".carton",
			)
		}
		packageName := strings.Map(func(r rune) rune {
			if unicode.IsLetter(r) {
				return r
			}
			return -1
		}, lastSegment[:len(lastSegment)-len(".carton")])

		packageNames = append(packageNames, packageName)
	}

	// Collect import paths for the Go mod file.
	generateImportPath := func(cartonName string, packageName string) string {
		return strings.TrimSpace(
			fmt.Sprintf("%s/v%s/%s", cartonName, majorVersion, packageName),
		)
	}

	importPaths := make([]string, 0, len(b.CartonNames)+2)
	importPaths = append(
		importPaths,
		generateImportPath(kernel.CartonName, "kernel"),
	)
	importPaths = append(
		importPaths,
		generateImportPath(kernel.CartonName, "burrow"),
	)

	for idx, cartonName := range b.CartonNames {
		importPaths = append(
			importPaths,
			generateImportPath(cartonName, packageNames[idx]),
		)
	}

	// Collect register call statements for the magic Go file.
	generateRegisterCallStmt := func(packageName string) string {
		return fmt.Sprintf("%s.RegisterCartonTo(warehouse)", packageName)
	}

	registerCallStmts := []string{generateRegisterCallStmt("burrow")}
	for _, packageName := range packageNames {
		registerCallStmts = append(
			registerCallStmts,
			generateRegisterCallStmt(packageName),
		)
	}

	// Build the magic Go file content.
	var content strings.Builder
	content.WriteString("package main\n\n")
	content.WriteString("import (\n")
	for _, importPath := range importPaths {
		content.WriteString("\t\"")
		content.WriteString(importPath)
		content.WriteString("\"\n")
	}
	content.WriteString(")\n\n")
	content.WriteString("func registerCartons(warehouse *kernel.Warehouse) {\n")
	for _, registerCall := range registerCallStmts {
		content.WriteString("\t")
		content.WriteString(registerCall)
		content.WriteString("\n")
	}
	content.WriteString("}\n")

	// Set the magic environment variables in the magic Go file.
	content.WriteString("\nfunc setEnv(burrow *kernel.Burrow) {\n")
	for key, value := range b.MagicEnv {
		fmt.Fprintf(&content, "\tburrow.Env.Set(%q, %q)\n", key, value)
	}
	content.WriteString("}\n")

	formattedSource, err := format.Source([]byte(content.String()))
	if err != nil {
		return fmt.Errorf("failed to format the magic Go file: %w", err)
	}

	filePath := filepath.Join(b.BurrowSourceDir, "cmd/magic.go")
	if err := os.WriteFile(filePath, formattedSource, 0o644); err != nil {
		return fmt.Errorf("failed to write the magic Go file: %w", err)
	}

	return nil
}

// BuildWithModFile builds the Burrow executable using the specified Go mod
// file.
func (b *Builder) BuildWithModFile(modFile string) error {
	_, stderr, exitCode, err := share.RunExternalCommand(
		b.BurrowSourceDir,
		[]string{
			"go", "build", "-o", b.OutputExecutablePath,
			"-modfile=" + modFile, "./cmd",
		},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf(
			"failed to build Burrow executable: %w",
			fmt.Errorf("%s", stderr),
		)
	}

	return nil
}

// BuildMinimalBurrow builds the minimal Burrow executable.
func (b *Builder) BuildMinimalBurrow() error {
	if err := b.GenerateMagicGoFile(); err != nil {
		return err
	}

	return b.BuildWithModFile(GoModFileName)
}

// Build builds the Burrow executable with the specified cartons.
func (b *Builder) Build() error {
	if err := b.GenerateMagicGoModFile(); err != nil {
		return fmt.Errorf("failed to generate magic Go mod file: %w", err)
	}

	if err := b.GenerateMagicGoFile(); err != nil {
		return fmt.Errorf("failed to generate magic Go file: %w", err)
	}

	return b.BuildWithModFile(MagicGoModFilePath)
}

// BuildBurrow builds the Burrow executable with the specified cartons
// using the standard source and output paths defined in the Burrow environment.
func BuildBurrow(
	burrow *kernel.Burrow,
	cartonNames []string,
	localCartons []LocalCarton,
	magicEnv kernel.Vars,
) error {
	burrowSourceDir := filepath.Join(
		burrow.GetSourceDir(),
		kernel.CartonName,
	)
	outputExecutablePath := filepath.Join(
		burrow.GetBinDir(),
		burrow.Env.Get(kernel.EnvExecutablePath),
	)

	return NewBuilder(
		burrowSourceDir,
		cartonNames,
		localCartons,
		magicEnv,
		outputExecutablePath,
	).Build()
}

// BuildMinimalBurrow builds the minimal Burrow executable that does not include
// any cartons other than the kernel carton using the standard source and output
// paths defined in the Burrow environment.
func BuildMinimalBurrow(burrow *kernel.Burrow) error {
	burrowSourceDir := filepath.Join(
		burrow.GetSourceDir(),
		kernel.CartonName,
	)
	outputExecutablePath := filepath.Join(
		burrow.GetBinDir(),
		burrow.Env.Get(kernel.EnvMinimalExecutablePath),
	)

	return NewBuilder(
		burrowSourceDir,
		[]string{},
		[]LocalCarton{},
		kernel.NewVars(),
		outputExecutablePath,
	).BuildMinimalBurrow()
}
