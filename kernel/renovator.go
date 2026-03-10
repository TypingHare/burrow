package kernel

type Renovator struct {
	Chamber *Chamber
}

func NewRenovator(chamber *Chamber) *Renovator {
	return &Renovator{
		Chamber: chamber,
	}
}

func (r *Renovator) ResolveDirectDeps(directDeps []string) error {
	return nil
}
