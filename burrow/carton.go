package burrow

import (
	"fmt"

	"github.com/TypingHare/burrow/v2026/burrow/clutter"
	"github.com/TypingHare/burrow/v2026/burrow/core"
	"github.com/TypingHare/burrow/v2026/burrow/core/share"
	"github.com/TypingHare/burrow/v2026/kernel"
)

func RegisterCartonTo(warehouse *kernel.Warehouse) error {
	carton := kernel.NewCarton()

	carton.Metadata.Set(kernel.MetadataName, "github.com/TypingHare/burrow")
	carton.Metadata.Set(kernel.MetadataVersion, kernel.Version)
	carton.Metadata.Set(kernel.MetadataAuthor, "James Chen")
	carton.Metadata.Set(kernel.MetadataEmail, "jameschan312.cn@gmail.com")

	if err := kernel.AddTypedDecorationFactory(
		carton,
		"core",
		share.ParseCoreSpec,
		core.BuildCoreDecoration,
	); err != nil {
		return fmt.Errorf("error adding core decoration factory: %w", err)
	}

	if err := kernel.AddTypedDecorationFactory(
		carton,
		"clutter",
		clutter.ParseClutterSpec,
		clutter.BuildClutterDecoration,
	); err != nil {
		return fmt.Errorf("error adding clutter decoration factory: %w", err)
	}

	return warehouse.RegisterCarton(carton)
}
