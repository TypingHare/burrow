package share

import (
	"github.com/TypingHare/burrow/v2026/kernel"
)

type IDecor interface {
	kernel.IDecor
	CartonDefs() []*CartonDef
	SetCartonDefs(cartons []*CartonDef)
}
