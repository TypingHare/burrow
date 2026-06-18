package share

import "github.com/TypingHare/burrow/v2026/kernel"

type IDecor interface {
	kernel.IDecor
	CabinetsByNames() map[string]any
}
