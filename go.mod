module github.com/TypingHare/burrow

go 1.25.0

require github.com/spf13/cobra v1.10.2 // direct

require (
	github.com/TypingHare/burrow/ext/demo v0.0.0
	github.com/inconshreveable/mousetrap v1.1.0 // indirect
	github.com/spf13/pflag v1.0.9 // indirect
	gonum.org/v1/gonum v0.17.0 // direct
)

replace github.com/TypingHare/burrow/ext/demo => /tmp/burrow-demo-carton
