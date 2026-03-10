package kernel

import "fmt"

// ChamberError reports a failure related to a Chamber.
type ChamberError struct {
	// ChamberName identifies the Chamber involved in the failure.
	ChamberName string

	// Message describes the failed operation.
	Message string

	// err is the underlying error, if any.
	err error
}

// NewChamberError returns a ChamberError for chamberName, msg, and err.
func NewChamberError(chamberName, message string, err error) *ChamberError {
	return &ChamberError{
		ChamberName: chamberName,
		Message:     message,
		err:         err,
	}
}

func (e *ChamberError) Error() string {
	if e.err == nil {
		return fmt.Sprintf("[%s] %s", e.ChamberName, e.Message)
	}

	return fmt.Sprintf("[%s] %s: %w", e.ChamberName, e.Message, e.err)
}

// Unwrap returns the underlying error.
func (e *ChamberError) Unwrap() error { return e.err }
