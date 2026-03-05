package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

type LocalCarton struct {
	Name string
	Path string
}

type ClutterSpec struct {
	CartonNames  []string
	LocalCartons []LocalCarton
	MagicEnv     kernel.Vars
}

func ParseClutterSpec(rawSpec kernel.RawSpec) (ClutterSpec, error) {
	cartonNames, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"cartonNames",
		[]string{},
	)
	if err != nil {
		return ClutterSpec{}, fmt.Errorf("error parsing cartonNames: %w", err)
	}

	localCartons, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"localCartons",
		[]LocalCarton{},
	)
	if err != nil {
		return ClutterSpec{}, fmt.Errorf(
			"error parsing localCartons: %w",
			err,
		)
	}

	maigcEnv, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"magicEnv",
		map[string]string{},
	)
	if err != nil {
		return ClutterSpec{}, fmt.Errorf(
			"error parsing magicEnv: %w",
			err,
		)
	}

	return ClutterSpec{
		CartonNames:  cartonNames,
		LocalCartons: localCartons,
		MagicEnv:     maigcEnv,
	}, nil
}
