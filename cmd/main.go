package main

import (
	"os"

	"github.com/TypingHare/burrow/builtin"
	"github.com/TypingHare/burrow/internal/kernel"
)

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init("burrow-go"); err != nil {
		println("Failed to initialize burrow:", err)
	}

	builtin.RegisterCarton(burrow.Warehouse())

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		println("Error handling command:", err.Error())
	}

	os.Exit(exitCode)
}
