package share

import "github.com/TypingHare/burrow/v2026/kernel"

func GetBurrowMajorVersion() string {
	parts, _ := kernel.SplitVersion(kernel.Version)
	return parts[0]
}
