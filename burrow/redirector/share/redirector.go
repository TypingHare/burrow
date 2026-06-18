package share

// Redirector rewrites a failed command invocation into a replacement argument
// list for a retry.
type Redirector func(args []string) ([]string, error)
