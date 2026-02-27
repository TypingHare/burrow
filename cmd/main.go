package main

import (
	"fmt"
	"os"

	"github.com/TypingHare/burrow/v2026/kernel"
)

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init("burrow"); err != nil {
		fmt.Fprintln(os.Stderr, err.Error())
	}

	registerCarton(burrow.Warehouse())

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		fmt.Fprintln(os.Stderr, err.Error())
	}

	os.Exit(exitCode)
}
