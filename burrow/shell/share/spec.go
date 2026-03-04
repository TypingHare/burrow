package share

import "github.com/TypingHare/burrow/v2026/kernel"

type ShellSpec struct {
	Shebang string
}

func ParseCoreSpec(rawSpec kernel.RawSpec) (ShellSpec, error) {
	shebang, err := kernel.GetRawSpecValueOrDefault(rawSpec, "shebang", "")
	if err != nil {
		return ShellSpec{}, err
	}

	return ShellSpec{
		Shebang: shebang,
	}, nil
}
