package share

import (
	"fmt"
	"os"
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetShellFilePath constructs the file path for the shell file based on the
// provided burrow instance and file name.
func GetShellFilePath(burrow *kernel.Burrow, fileName string) string {
	if fileName == "." {
		fileName = "broot"
	}

	return filepath.Join(burrow.GetBinDir(), fileName)
}

// GetShellFileContent generates the content of the shell file based on the
// provided shell decoration.
func GetShellFileContent(shellDecoration ShellDecorationLike) string {
	shebang := shellDecoration.Spec().Shebang
	chamberName := shellDecoration.Chamber().Name()

	return "#!" + shebang + "\n\nburrow " + chamberName + " $@\n"
}

// CreateShellFile creates a shell file with the specified name and content
// based on the provided burrow instance and shell decoration.
func CreateShellFile(
	d ShellDecorationLike,
	fileName string,
) (string, error) {
	if d.Spec().Shebang == "" {
		return "", fmt.Errorf(
			"shebang is not specified in the shell decoration spec",
		)
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
