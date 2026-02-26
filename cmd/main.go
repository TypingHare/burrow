package main

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/internal/kernel"
)

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init("burrow-go"); err != nil {
		println("Failed to initialize burrow:", err)
	}

	registerCarton(burrow)

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		fmt.Fprintln(os.Stderr, err.Error())
	}

	os.Exit(exitCode)
}
