build:
    gradle clean
    gradle shadowJar
    mkdir -p ~/.burrow/libs/
    mv build/libs/* ~/.burrow/libs/

# Move binary files to ~/.burrow/bin
move-bin:
    mkdir -p ~/.burrow/bin
    cp src/main/resources/init/bin/* ~/.burrow/bin

start-server:
    burrow . server.start

start-cli:
    @burrow-cli --server=localhost:4710 default