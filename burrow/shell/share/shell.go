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
	burrow *kernel.Burrow,
	shellDecoration ShellDecorationLike,
	fileName string,
) error {
	filePath := GetShellFilePath(burrow, fileName)
	content := GetShellFileContent(shellDecoration)

	err := os.WriteFile(filePath, []byte(content), 0o644)
	if err != nil {
		return fmt.Errorf("failed to create shell file: %w", err)
	}

	return nil
}
