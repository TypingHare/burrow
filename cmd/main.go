package main

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

// DefaultName is the default name of the Burrow.
const DefaultName = "burrow"

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init(DefaultName); err != nil {
		fmt.Fprintf(os.Stderr, "failed to initialize burrow: %v\n", err)
		os.Exit(kernel.GeneralError)
	}

	registerCartons(burrow.Warehouse())

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		burrow.PrintErrorStack(err)
	}

	os.Exit(exitCode)
}
