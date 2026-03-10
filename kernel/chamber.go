package kernel

type Chamber struct {
	Burrow         *Burrow
	Name           string
	Blueprint      Blueprint
	Renovator      *Renovator
	CommandHandler CommandHandler
	ErrorHandler   ErrorHandler
}

func NewChamber(burrow *Burrow, name string, blueprint Blueprint) *Chamber {
	chamber := &Chamber{
		Burrow:         burrow,
		Name:           name,
		Blueprint:      blueprint,
		CommandHandler: DefaultCommandHandler,
		ErrorHandler:   DefaultErrorHandler,
	}

	chamber.Renovator = NewRenovator(chamber)

	return chamber
}

func (c *Chamber) InstallDecors() error {
	return nil
}

func (c *Chamber) UninstallDecors() error {
	return nil
}
