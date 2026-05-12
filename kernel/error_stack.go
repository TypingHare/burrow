package kernel

import (
	"errors"
	"fmt"
	"os"
	"strings"
)

// PrintErrorStack prints an error and its unwrap chain to stderr from outermost
// to innermost with stable numeric indices.
func PrintErrorStack(err error) {
	stack := []string{}

	currentErr := err
	for currentErr != nil {
		currErrMsg := currentErr.Error()
		nextErr := errors.Unwrap(currentErr)
		if nextErr == nil {
			stack = append(stack, currErrMsg)
		} else {
			nextErrMsg := nextErr.Error()
			if len(nextErrMsg) < len(currErrMsg) &&
				strings.HasSuffix(currErrMsg, nextErrMsg) {
				currErrMsg = strings.TrimSuffix(currErrMsg, nextErrMsg)
				currErrMsg = strings.TrimSuffix(currErrMsg, ": ")
			}
			stack = append(stack, currErrMsg)
		}

		currentErr = nextErr
	}

	i := 0
	for _, errMsg := range stack {
		fmt.Fprintf(os.Stderr, "(%d) %s\n", i, errMsg)
		i++
	}
}
