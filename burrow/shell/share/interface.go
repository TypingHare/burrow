package share

import "github.com/TypingHare/burrow/v2026/kernel"

type ShellDecorationLike interface {
	kernel.DecorationInstance
	Spec() ShellSpec
}
