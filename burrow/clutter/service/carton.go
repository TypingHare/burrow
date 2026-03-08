package service

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
)

// InstallCarton adds cartonName to the clutter spec and optionally records a
// local checkout path for it.
func InstallCarton(
	d share.ClutterDecorationLike,
	cartonName string,
	localPath string,
) error {
	err := share.InstallCarton(d.Chamber(), d.Spec(), cartonName, localPath)
	if err != nil {
		return fmt.Errorf("failed to install carton: %w", err)
	}

	return nil
}

// UninstallCarton removes cartonName from the clutter spec and clears any
// associated local checkout path.
func UninstallCarton(
	d share.ClutterDecorationLike,
	cartonName string,
) error {
	err := share.UninstallCarton(d.Chamber(), d.Spec(), cartonName)
	if err != nil {
		return fmt.Errorf("failed to uninstall carton: %w", err)
	}

	return nil
}

// SetLocal associates cartonName with a local checkout path in the clutter
// spec.
func SetLocal(
	d share.ClutterDecorationLike,
	cartonName string,
	localPath string,
) error {
	err := share.SetLocalCartonPath(
		d.Chamber(),
		d.Spec(),
		cartonName,
		localPath,
	)
	if err != nil {
		return fmt.Errorf("failed to set local carton path: %w", err)
	}

	return nil
}

// UnsetLocal removes any local checkout path associated with cartonName.
func UnsetLocal(
	d share.ClutterDecorationLike,
	cartonName string,
) error {
	err := share.UnsetLocalCartonPath(d.Chamber(), d.Spec(), cartonName)
	if err != nil {
		return fmt.Errorf("failed to unset local carton path: %w", err)
	}

	return nil
}
