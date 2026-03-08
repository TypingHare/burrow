package share

import "github.com/TypingHare/burrow/v2026/kernel"

type ServerDecorationLike interface {
	kernel.DecorationInstance
	Spec() *ServerSpec
	Start() error
}

type ServerSpec struct {
	ListenAddress   string
	CommandPath     string
	MaxBodyBytes    int64
	ReadTimeoutMS   int
	WriteTimeoutMS  int
	IdleTimeoutMS   int
	ShutdownGraceMS int
}

func ParseServerSpec(rawSpec kernel.RawSpec) (*ServerSpec, error) {
	listenAddress, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"listenAddress",
		":8080",
	)
	if err != nil {
		return nil, err
	}

	commandPath, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"commandPath",
		"/",
	)
	if err != nil {
		return nil, err
	}

	maxBodyBytes, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"maxBodyBytes",
		int64(1<<20),
	)
	if err != nil {
		return nil, err
	}

	readTimeoutMS, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"readTimeoutMS",
		5000,
	)
	if err != nil {
		return nil, err
	}

	writeTimeoutMS, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"writeTimeoutMS",
		5000,
	)
	if err != nil {
		return nil, err
	}

	idleTimeoutMS, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"idleTimeoutMS",
		60000,
	)
	if err != nil {
		return nil, err
	}

	shutdownGraceMS, err := kernel.GetRawSpecValueOrDefault(
		rawSpec,
		"shutdownGraceMS",
		5000,
	)
	if err != nil {
		return nil, err
	}

	return &ServerSpec{
		ListenAddress:   listenAddress,
		CommandPath:     commandPath,
		MaxBodyBytes:    maxBodyBytes,
		ReadTimeoutMS:   readTimeoutMS,
		WriteTimeoutMS:  writeTimeoutMS,
		IdleTimeoutMS:   idleTimeoutMS,
		ShutdownGraceMS: shutdownGraceMS,
	}, nil
}
