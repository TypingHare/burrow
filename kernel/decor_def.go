package kernel

import "fmt"

type DecorDef interface {
	Name() string
	Parse(RawSpec) (any, error)
	Build(*Chamber, any) (Decor, error)
}

type TypedDecorDef[S any] struct {
	DecorName  string
	ParseSpec  func(RawSpec) (*S, error)
	BuildDecor func(*Chamber, *S) (Decor, error)
}

func NewTypedDecorDef[S any](
	name string,
	parse func(RawSpec) (*S, error),
	build func(*Chamber, *S) (Decor, error),
) *TypedDecorDef[S] {
	return &TypedDecorDef[S]{
		DecorName:  name,
		ParseSpec:  parse,
		BuildDecor: build,
	}
}

func (d *TypedDecorDef[S]) Name() string {
	return d.DecorName
}

func (d *TypedDecorDef[S]) Parse(raw RawSpec) (any, error) {
	return d.ParseSpec(raw)
}

func (d *TypedDecorDef[S]) Build(
	chamber *Chamber,
	spec any,
) (Decor, error) {
	typed, ok := spec.(*S)
	if !ok {
		return nil, fmt.Errorf("invalid spec type for decor %q", d.DecorName)
	}

	return d.BuildDecor(chamber, typed)
}
