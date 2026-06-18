package share

import (
	"github.com/TypingHare/burrow/v2026/kernel"
)

// IDecor represents the ID of a decor definition or an installed decor.
type IDecor interface {
	kernel.IDecor
	Redirector() Redirector
	SilentlyRedirect() bool
}
