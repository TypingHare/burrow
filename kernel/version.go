package kernel

import (
	"fmt"
	"strings"
)

// splitVersion splits version into major, minor, and patch components.
//
// version must be in major.minor.patch form.
func splitVersion(version string) ([3]string, error) {
	parts := strings.Split(version, ".")
	if len(parts) != 3 {
		return [3]string{}, fmt.Errorf(
			"invalid version %q: expected major.minor.patch",
			version,
		)
	}

	return [3]string{parts[0], parts[1], parts[2]}, nil
}

// GetMajorVersion returns the major version component.
func GetMajorVersion(version string) (string, error) {
	parts, err := splitVersion(version)
	if err != nil {
		return "", err
	}

	return parts[0], nil
}

// GetMinorVersion returns the minor version component.
func GetMinorVersion(version string) (string, error) {
	parts, err := splitVersion(version)
	if err != nil {
		return "", err
	}

	return parts[1], nil
}

// GetMajorMinorVersion returns the major.minor version components.
func GetMajorMinorVersion(version string) (string, error) {
	parts, err := splitVersion(version)
	if err != nil {
		return "", err
	}

	return parts[0] + "." + parts[1], nil
}

// GetPatchVersion returns the patch version component.
func GetPatchVersion(version string) (string, error) {
	parts, err := splitVersion(version)
	if err != nil {
		return "", err
	}

	return parts[2], nil
}
