package main

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init("burrow"); err != nil {
		fmt.Fprintf(os.Stderr, "failed to initialize burrow: %v\n", err)
	}

	registerCartons(burrow.Warehouse())

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		fmt.Fprintf(os.Stderr, "error: %v\n", err)
	}

	os.Exit(exitCode)
}
