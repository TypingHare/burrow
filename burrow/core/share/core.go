package share

import (
	"github.com/TypingHare/burrow/v2026/kernel"
)

type CoreDecorationLike interface {
	kernel.DecorationInstance
	Spec() CoreSpec
}
