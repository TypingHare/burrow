package kernel

import "fmt"

// ChamberError reports a failure related to a Chamber.
type ChamberError struct {
	// chamberName identifies the Chamber involved in the failure.
	chamberName string

	// msg describes the failed operation.
	msg string

	// err is the underlying error, if any.
	err error
}

// NewChamberError returns a ChamberError for chamberName, msg, and err.
func NewChamberError(chamberName, msg string, err error) *ChamberError {
	return &ChamberError{
		chamberName: chamberName,
		msg:         msg,
		err:         err,
	}
}

// ChamberName returns the name of the Chamber involved in the error.
func (c *ChamberError) ChamberName() string {
	return c.chamberName
}

// Msg returns the error message.
func (c *ChamberError) Msg() string {
	return c.msg
}

// Error returns the formatted error string.
func (e *ChamberError) Error() string {
	if e.err == nil {
		return e.chamberName + ": " + e.msg
	}

	return fmt.Sprintf("[%s] %s: %v", e.chamberName, e.msg, e.err)
}

// Unwrap returns the underlying error.
func (e *ChamberError) Unwrap() error { return e.err }
