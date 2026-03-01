package share

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/kernel"
)

type ClutterSpec struct {
	CartonNames      []string
	LocalCartonNames []string
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

	localCartonNames, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"localCartonNames",
		[]string{},
	)
	if err != nil {
		return ClutterSpec{}, fmt.Errorf(
			"error parsing localCartonNames: %w",
			err,
		)
	}

	return ClutterSpec{
		CartonNames:      cartonNames,
		LocalCartonNames: localCartonNames,
	}, nil
}
