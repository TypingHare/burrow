# Burrow

[Burrow-old](/Users/jameschan/Git/public/burrow-new/src/main/java/burrow/core)

## Philosophy

Often, the desire to create a console application for a specific purpose is hindered by the need to develop nearly everything from the ground up. The repetitive tasks involved in building, such as command parsing, data management, and recurring business logic, can quickly drain one's patience. To eliminate these cumbersome obstacles, it's high time to introduce a command application platform that streamlines the process, enabling rapid development of console applications.

Burrow's philosophy is elegantly straightforward: it operates on the principle that each console application should have a singular focus, managing a specific type of data comprised of numerous entries. In this paradigm, each entry is represented as an object with string-based keys and values. Every application governs a collection of these entries and provides a set of accessible commands. To streamline development and minimize code redundancy, Burrow employs a plugin-based architecture where distinct functionalities are encapsulated within individual plugins. Consequently, an application essentially functions as a composite of these plugins, offering a modular and efficient approach to software design.

## Installation

Burrow requires Java 21 as prerequisite.

## Get Started

Let's start by using a simple built-in application.

```bash
# Create an entry [grocery -> 36.45]
burrow budget new grocery 36.45

# Look up the values associated with the key "grocery"
burrow budget key grocery

# Output
[ "36.45" ]
```

In this example, `budget` is the name of the application we are using. The first command creates an entry, where key is "grocery" and value is "36.45". The second command looks up all values associated with the key "grocery", and it outputs a list containing the value of the entry we just created.

You may already discover the pattern of the command in burrow:

~~~bash
burrow <app_name> <command> [...<args>]
~~~

There are a few of commands that work for all apps (if the app does not override them), such as the empty command, which shows the help information of the application, or the `root`, which outputs the root directory of the application. We can try to get the root directory of the `budget` app by:

~~~ bash
burrow budget root
~~~

Usually, the root directory of an application is `~/.burrow/app/<app_name>` on macOS.

## Understand Entries

### Builtin

Within Burrow, four fundamental commands are inherently tied to managing entries: `new`, `entry`, `exist`, and `delete`. These commands are considered "builtin",signifying that they are standard across all applications unless specifically overridden. Notably, Burrow features a special builtin application dubbed `builtin`, which does not alter any existing commands within the Burrow framework.

```bash
# Create an empty entry
burrow builtin new
# >> Entry created: [1] { }
# Here, 1 is the ID of the entry created

# Check if an entry associated with the given key exists
burrow builtin exist 2
# >> False

# Delete an entry associated with the given key
burrow builtin delete 1
# >> Entry deleted: [1] { }
```

### Essential Plugin - KeyValue

`KeyValue` is an essential builtin plugin in Burrow. It overrides the `new` command and allows users to create an entry with a key and a value. The builtin app `Budget` uses the `KeyValue` plugin, enabling us to create an entry like this:

```bash
burrow budget insurance 105.90
# >> Entry created: [1] { key: "insurance", value: "105.90" }
```

## Resources

[picocli](https://github.com/remkop/picocli)
[Picocli documentation](https://picocli.info)
[Exit code](https://hpc-discourse.usc.edu/t/exit-codes-and-their-meanings/414)
