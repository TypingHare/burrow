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

// GenerateMagicGoModFile generates a go.mod file that includes the specified
// cartons as dependencies.
func GenerateMagicGoModFile(
	burrowSourceDir string,
	cartonNames []string,
	localCartons []LocalCarton,
	fileName string,
) error {
	if len(cartonNames) != len(localCartons) {
		return fmt.Errorf(
			"the length of cartonNames and localCartons must be the same",
		)
	}

	goModPath := filepath.Join(burrowSourceDir, "go.mod")
	_, err := os.Stat(goModPath)
	if os.IsNotExist(err) {
		return fmt.Errorf("go.mod file does not exist at path: %s", goModPath)
	} else if err != nil {
		return fmt.Errorf("failed to access go.mod file at path: %s", goModPath)
	}

	// Build a temporary map that maps carton names to their local paths.
	localPathsByCartonNames := make(map[string]string)
	for _, localCarton := range localCartons {
		localPathsByCartonNames[localCarton.Name] = localCarton.Path
	}

	// Remove magic.go.mod if it already exists to ensure a clean state.
	magicGoModPath := filepath.Join(burrowSourceDir, fileName)
	if _, err := os.Stat(magicGoModPath); err == nil {
		if err := os.Remove(magicGoModPath); err != nil {
			return fmt.Errorf("failed to remove existing %s: %w", fileName, err)
		}
	}

	majorVersion, _ := kernel.GetMajorVersion(kernel.Version)
	majorMinorVersion, _ := kernel.GetMajorMinorVersion(kernel.Version)
	for _, cartonName := range cartonNames {
		cartonURL := fmt.Sprintf(
			"%s/%s/@v%s",
			cartonName,
			majorVersion,
			majorMinorVersion,
		)

		localCartonPath, isLocalCarton := localPathsByCartonNames[cartonName]
		if isLocalCarton && localCartonPath != "" {
			_, _, exitCode, err := share.RunExternalCommand(
				burrowSourceDir,
				[]string{
					"go",
					"mod",
					"edit",
					"-modfile=magic.go.mod",
					fmt.Sprintf("-replace=%s", cartonURL),
				},
			)

			if err != nil || exitCode != 0 {
				return fmt.Errorf(
					"failed to run 'go get' for local carton %q: %w",
					cartonName,
					err,
				)
			}

			_, _, exitCode, err = share.RunExternalCommand(
				burrowSourceDir,
				[]string{
					"go",
					"mod",
					"edit",
					"-modfile=magic.go.mod",
					fmt.Sprintf("-require=%s", localCartonPath),
				},
			)

			if err != nil || exitCode != 0 {
				return fmt.Errorf(
					"failed to run 'go get' for local carton %q: %w",
					cartonName,
					err,
				)
			}
		} else {
			_, _, exitCode, err := share.RunExternalCommand(
				burrowSourceDir,
				[]string{
					"go",
					"get",
					"-modfile=magic.go.mod",
					cartonURL,
				},
			)

			if err != nil || exitCode != 0 {
				return fmt.Errorf(
					"failed to run 'go get' for carton '%s': %w",
					cartonName,
					err,
				)
			}
		}
	}

	return nil
}

// Generate a cmd/magic.go file that imports all cartons in the Burrow source
// directory.
func GenerateMagicGoFile(
	burrowSourceDir string,
	cartonNames []string,
) error {
	// Resolve package names for the cartons.
	packageNames := make([]string, 0, len(cartonNames))
	for _, cartonName := range cartonNames {
		lastSegment := filepath.Base(cartonName)
		if !strings.HasSuffix(lastSegment, ".carton") {
			return fmt.Errorf(
				"carton name '%s' does not end with %q",
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

	// Collect import paths for the go.mod file.
	majorVersion, err := kernel.GetMajorVersion(kernel.Version)
	if err != nil {
		return err
	}
	generateImportPath := func(cartonName string, packageName string) string {
		return strings.TrimSpace(
			fmt.Sprintf("%s/v%s/%s", cartonName, majorVersion, packageName),
		)
	}

	importPaths := make([]string, 0, len(cartonNames)+2)
	importPaths = append(
		importPaths,
		generateImportPath(kernel.CartonName, "kernel"),
	)
	importPaths = append(
		importPaths,
		generateImportPath(kernel.CartonName, "burrow"),
	)

	for idx, cartonName := range cartonNames {
		importPaths = append(
			importPaths,
			generateImportPath(cartonName, packageNames[idx]),
		)
	}

	// Collect register call statements for the magic.go file.
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

	// Build the magic.go file content.
	var content strings.Builder
	content.WriteString("package main\n\n")
	content.WriteString("import (\n")
	for _, importPath := range importPaths {
		content.WriteString("\t")
		content.WriteString(importPath)
		content.WriteString("\n")
	}
	content.WriteString(")\n\n")
	content.WriteString("func registerCartons(warehouse *kernel.Warehouse) {\n")
	for _, registerCall := range registerCallStmts {
		content.WriteString("\t")
		content.WriteString(registerCall)
		content.WriteString("\n")
	}
	content.WriteString("}\n")

	formattedSource, err := format.Source([]byte(content.String()))
	if err != nil {
		return fmt.Errorf("failed to format magic.go: %w", err)
	}

	filePath := filepath.Join(burrowSourceDir, "cmd/magic.go")
	if err := os.WriteFile(filePath, formattedSource, 0o644); err != nil {
		return fmt.Errorf("failed to write magic.go: %w", err)
	}

	return nil
}

// BuildMinimalBurrow builds the minimal Burrow executable.
func BuildMinimalBurrow(
	burrowSourceDir string,
	outputExecutablePath string,
) error {
	if err := GenerateMagicGoFile(burrowSourceDir, []string{}); err != nil {
		return err
	}

	return Build(burrowSourceDir, "go.mod", outputExecutablePath)
}

// BuildBurrow builds the Burrow executable with the specified cartons.
func BuildBurrow(
	burrowSourceDir string,
	cartonNames []string,
	localCartons []LocalCarton,
	outputExecutablePath string,
) error {
	GoModFileName := "magic.go.mod"
	if err := GenerateMagicGoModFile(
		burrowSourceDir,
		cartonNames,
		localCartons,
		GoModFileName,
	); err != nil {
		return fmt.Errorf("failed to generate magic.go.mod file: %w", err)
	}

	if err := GenerateMagicGoFile(
		burrowSourceDir,
		cartonNames,
	); err != nil {
		return fmt.Errorf("failed to generate magic.go file: %w", err)
	}

	return Build(burrowSourceDir, GoModFileName, outputExecutablePath)
}

// Build builds the Burrow executable using the specified go.mod file.
func Build(
	burrowSourceDir string,
	modFile string,
	outputExecutablePath string,
) error {
	_, _, exitCode, err := share.RunExternalCommand(
		burrowSourceDir,
		[]string{
			"mod",
			"tidy",
			"-modfile=" + modFile,
		},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf("failed to run 'go mod tidy': %w", err)
	}

	_, _, exitCode, err = share.RunExternalCommand(
		burrowSourceDir,
		[]string{
			"go",
			"build",
			"-o",
			outputExecutablePath,
			"-modfile=" + modFile,
			"./cmd",
		},
	)
	if err != nil || exitCode != 0 {
		return fmt.Errorf("failed to build Burrow executable: %w", err)
	}

	return nil
}
