package main

import (
	"fmt"
	"os"
	"runtime/debug"

	"github.com/TypingHare/burrow/v2026/kernel"
)

func main() {
	burrow := kernel.NewBurrow()
	if err := burrow.Init("burrow"); err != nil {
		printErrWithStack(err)
	}

	registerCarton(burrow.Warehouse())

	exitCode, err := burrow.Handle(os.Args[1:])
	if err != nil {
		printErrWithStack(err)
	}

	os.Exit(exitCode)
}

func printErrWithStack(err error) {
	fmt.Fprintf(os.Stderr, "error: %v\n%s\n", err, debug.Stack())
}
