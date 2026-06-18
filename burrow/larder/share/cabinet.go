package share

import (
	"path/filepath"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// GetLarderDataDir returns the path to the larder data directory for the given
// chamber.
func GetLarderDataDir(chamber *kernel.Chamber) string {
	return filepath.Join(chamber.GetDataDir(), "larder")
}

// GetCabinetFilePath returns the file path for a cabinet with the given name in
// the given chamber.
func GetCabinetFilePath(chamber *kernel.Chamber, name string) string {
	return filepath.Join(GetLarderDataDir(chamber), name+".csv")
}
