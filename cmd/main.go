package main

// DefaultName is the default name of the Burrow.
const DefaultName = "burrow"

func main() {
	// // Initialize the Burrow.
	// burrow := kernel.NewBurrow()
	// if err := burrow.InitEnv(DefaultName); err != nil {
	// 	fmt.Fprintf(
	// 		os.Stderr,
	// 		"failed to initialize environment variables: %v\n",
	// 		err,
	// 	)
	// 	os.Exit(kernel.GeneralError)
	// }
	// burrow.LoadProcessEnv()
	// setEnv(burrow)
	// registerCartons(burrow.Warehouse())
	//
	// // Handle the command and arguments.
	// exitCode, err := burrow.Handle(os.Args[1:])
	// if err != nil {
	// 	burrow.PrintErrorStack(err)
	// }
	//
	// // Bury the burrow instance.
	// if err := burrow.Bury(); err != nil {
	// 	burrow.PrintErrorStack(err)
	// }
	//
	// // Exit with the appropriate code.
	// os.Exit(exitCode)
}
