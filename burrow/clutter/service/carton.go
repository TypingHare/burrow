package service

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter/share"
)

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
