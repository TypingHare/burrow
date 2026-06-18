package redirector

import (
	"fmt"
	"reflect"

	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/redirector/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

const (
	DecorName               = "redirector"
	SpecKeySilentlyRedirect = "silently_redirect"
)

type Decor struct {
	*kernel.Decor

	// redirector rewrites command arguments after an initial execution failure.
	redirector share.Redirector

	// silentlyRedirect controls whether to suppress output during redirection.
	silentlyRedirect bool
}

func (d *Decor) Dependencies() []string {
	return []string{
		kernel.GetDecorID(core.DecorName, kernel.CartonName),
	}
}

func (d *Decor) UpdateSpec() error {
	d.Spec().SetBool(SpecKeySilentlyRedirect, d.silentlyRedirect)
	return nil
}

func (d *Decor) Redirector() share.Redirector {
	return d.redirector
}

func (d *Decor) SetRedirector(redirector share.Redirector) {
	d.redirector = redirector
}

func (d *Decor) SilentlyRedirect() bool {
	return d.silentlyRedirect
}

func (d *Decor) SetSilentlyRedirect(silentlyRedirect bool) {
	d.silentlyRedirect = silentlyRedirect
}

func RegisterToCarton(carton *kernel.Carton) error {
	return core.CreateAndAddDecorDefToCarton(
		carton,
		DecorName,
		func(chamber *kernel.Chamber, spec kernel.Vars) (*Decor, error) {
			return &Decor{
				Decor:            kernel.NewDecor(chamber, spec),
				silentlyRedirect: spec.GetBool(SpecKeySilentlyRedirect),
			}, nil
		},
		func(chamber *kernel.Chamber, decor *Decor) error {
			coreDecor, err := core.UseDecor(chamber)
			if err != nil {
				return fmt.Errorf("failed to use core decor: %w", err)
			}

			// Replace the command handler with a redirector handler.
			decor.Chamber().CommandHandler = share.GetRedirectorHandler(
				decor,
				coreDecor,
			)

			return nil
		},
	)
}

func UseDecor(chamber *kernel.Chamber) (*Decor, error) {
	decor, err := chamber.Renovator.GetDecorByType(reflect.TypeFor[*Decor]())
	if err != nil {
		return nil, fmt.Errorf(
			"failed to get the %q decor: %w",
			DecorName,
			err,
		)
	}

	return decor.(*Decor), nil
}
