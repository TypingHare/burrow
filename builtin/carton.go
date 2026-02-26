package builtin

import (
	"github.com/TypingHare/burrow/v2026/builtin/core"
	"github.com/TypingHare/burrow/v2026/internal/kernel"
)

func RegisterCarton(warehouse *kernel.Warehouse) error {
	carton := kernel.NewCarton()
	carton.Metadata.Set(kernel.MetadataGoModule, "github.com/TypingHare/burrow/v2026")
	carton.Metadata.Set(kernel.MetadataName, "burrow")
	carton.Metadata.Set(kernel.MetadataVersion, "2026.1.0")
	carton.Metadata.Set(kernel.MetadataAuthor, "James Chen")
	carton.Metadata.Set(kernel.MetadataEmail, "jameschan312.cn@gmail.com")

	// carton.AddDecorationFactory("core", core.)
	kernel.AddTypedDecorationFactory(
		carton,
		"core",
		core.ParseCoreSpec,
		core.BuildCoreDecoration,
	)
	warehouse.RegisterCarton(carton)

	return nil
}
