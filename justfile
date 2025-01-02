burrow_root := "~/.burrow"

build:
    gradle clean
    gradle shadowJar --quiet
    mkdir -p {{ burrow_root }}/libs/
    mv build/libs/* {{ burrow_root }}/libs/

move-bin:
    mkdir -p {{ burrow_root }}/bin
    cp src/main/resources/init/bin/* {{ burrow_root }}/bin

start-server:
    burrow . server.start

start-cli:
    @burrow-cli --server=localhost:4710
