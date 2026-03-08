package share

import "github.com/TypingHare/burrow/v2026/kernel"

// ClutterDecorationLike describes the clutter-decoration surface used by
// command builders and helpers.
type ClutterDecorationLike interface {
	kernel.DecorationInstance
	Spec() *ClutterSpec
}

// LocalCarton maps a carton name to a local checkout used during builds.
type LocalCarton struct {
	Name string
	Path string
}

// ClutterSpec stores Burrow build inputs managed by the clutter decoration.
type ClutterSpec struct {
	CartonNames  []string
	LocalCartons []LocalCarton
	MagicEnv     kernel.Vars
}

// ParseClutterSpec parses raw blueprint data into a ClutterSpec.
func ParseClutterSpec(rawSpec kernel.RawSpec) (*ClutterSpec, error) {
	cartonNames, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"cartonNames",
		[]string{},
	)
	if err != nil {
		return nil, err
	}

	localCartons, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"localCartons",
		[]LocalCarton{},
	)
	if err != nil {
		return nil, err
	}

	maigcEnv, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"magicEnv",
		map[string]string{},
	)
	if err != nil {
		return nil, err
	}

	return &ClutterSpec{
		CartonNames:  cartonNames,
		LocalCartons: localCartons,
		MagicEnv:     maigcEnv,
	}, nil
}
