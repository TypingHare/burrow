package share

import (
	"fmt"
	"os"
	"path/filepath"
	"regexp"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// shellFileNamePattern intentionally matches the chamber-name policy so shell
// launcher files stay as flat entries in the Burrow bin directory.
var shellFileNamePattern = regexp.MustCompile(`^[A-Za-z0-9_.]*[A-Za-z0-9_.]$`)

// GetShellFilePath constructs the file path for the shell file based on the
// provided burrow instance and file name.
func GetShellFilePath(burrow *kernel.Burrow, fileName string) string {
	if fileName == "." {
		fileName = "broot"
	}

	return filepath.Join(burrow.GetBinDir(), fileName)
}

// GetShellFileContent generates the wrapper script for the shell decoration.
// `"$@"` preserves the caller's original argument boundaries when forwarding
// them to Burrow.
func GetShellFileContent(shellDecoration ShellDecorationLike) string {
	shebang := shellDecoration.Spec().Shebang
	chamberName := shellDecoration.Chamber().Name()

	return "#!" + shebang + "\n\nburrow " + chamberName + " \"$@\"\n"
}

// CreateShellFile creates a shell file with the specified name and content
// based on the provided burrow instance and shell decoration.
//
// fileName must satisfy shellFileNamePattern so callers cannot create nested
// paths under the Burrow bin directory.
func CreateShellFile(
	d ShellDecorationLike,
	fileName string,
) (string, error) {
	if d.Spec().Shebang == "" {
		return "", fmt.Errorf(
			"shebang is not specified in the shell decoration spec",
		)
	}
	if !shellFileNamePattern.MatchString(fileName) {
		return "", fmt.Errorf("invalid shell file name: %q", fileName)
	}

	burrow := d.Chamber().Burrow()
	shellFilePath := GetShellFilePath(burrow, fileName)
	fileRelPath, err := filepath.Rel(burrow.GetBinDir(), shellFilePath)
	if err != nil {
		return "", fmt.Errorf("failed to get relative file name: %w", err)
	}

	content := GetShellFileContent(d)

	err = os.WriteFile(shellFilePath, []byte(content), 0o755)
	if err != nil {
		return "", fmt.Errorf("failed to create shell file: %w", err)
	}

	return fileRelPath, nil
}
