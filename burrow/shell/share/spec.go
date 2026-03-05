package share

import "github.com/TypingHare/burrow/v2026/kernel"

type ShellSpec struct {
	Shebang          string
	FileName         string
	CreatedFileNames []string
}

func ParseCoreSpec(rawSpec kernel.RawSpec) (ShellSpec, error) {
	shebang, err := kernel.GetRawSpecValueOrDefault(rawSpec, "shebang", "")
	if err != nil {
		return ShellSpec{}, err
	}

	fileName, err := kernel.GetRawSpecValueOrDefault(rawSpec, "fileName", "")
	if err != nil {
		return ShellSpec{}, err
	}

	createdFileNames, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"createdFileNames",
		[]string{},
	)
	if err != nil {
		return ShellSpec{}, err
	}

	return ShellSpec{
		Shebang:          shebang,
		FileName:         fileName,
		CreatedFileNames: createdFileNames,
	}, nil
}
