package standard

import "github.com/TypingHare/burrow/v2026/kernel"

func Use[T kernel.Decor](chamber *kernel.Chamber) (*T, error) {
	//    decorationType := reflect.TypeFor[T]()
	// decoration, ok := chamber.Renovator().GetDecorationByType(decorationType)
	// if !ok {
	// 	return zero, chamber.Error(
	// 		fmt.Sprintf("decoration %v is not installed", decorationType),
	// 		nil,
	// 	)
	// }
	//
	// typed, ok := decoration.(T)
	// if !ok {
	// 	return zero, chamber.Error(
	// 		fmt.Sprintf(
	// 			"cast decoration expected %v but got %T",
	// 			decorationType,
	// 			decoration,
	// 		),
	// 		nil,
	// 	)
	// }
	//
	// return typed, nil

	return nil, nil
}
