build:
    gradle clean
    gradle shadowJar
    mkdir -p ~/.burrow/libs/
    mv build/libs/* ~/.burrow/libs/

move-bin:
    mkdir -p ~/.burrow/bin
    cp src/main/bin/* ~/.burrow/bin