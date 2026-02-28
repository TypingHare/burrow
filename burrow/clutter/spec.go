package clutter

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

type ClutterSpec struct {
	cartonNames []string
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

	return ClutterSpec{
		cartonNames: cartonNames,
	}, nil
}
