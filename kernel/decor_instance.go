package kernel

type DecorInstance interface {
	Chamber() *Chamber
	Deps() []string
	RawSpec() RawSpec

	Assemble() error
	Launch() error
	Terminate() error
	Disassemble() error
}
