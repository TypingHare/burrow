package share

import "github.com/TypingHare/burrow/v2026/kernel"

// ShellDecorationLike describes the shell-decoration surface used by shell
// helpers and commands.
type ShellDecorationLike interface {
	kernel.DecorationInstance
	Spec() *ShellSpec
}

// ShellSpec stores shell-script generation settings persisted in the
// blueprint.
type ShellSpec struct {
	Shebang          string
	FileName         string
	CreatedFileNames []string
}

// ParseShellSpec parses raw blueprint data into a ShellSpec.
func ParseShellSpec(rawSpec kernel.RawSpec) (*ShellSpec, error) {
	shebang, err := kernel.GetRawSpecValueOrDefault(rawSpec, "shebang", "")
	if err != nil {
		return nil, err
	}

	fileName, err := kernel.GetRawSpecValueOrDefault(rawSpec, "fileName", "")
	if err != nil {
		return nil, err
	}

	createdFileNames, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"createdFileNames",
		[]string{},
	)
	if err != nil {
		return nil, err
	}

	return &ShellSpec{
		Shebang:          shebang,
		FileName:         fileName,
		CreatedFileNames: createdFileNames,
	}, nil
}
