burrow_root := "~/.burrow"

build:
    gradle clean
    gradle shadowJar --quiet

clean:
    rm -rf build

move-bin:
    mkdir -p {{ burrow_root }}/bin
    cp src/main/resources/init/bin/* {{ burrow_root }}/bin

move-jar:
    just build
    mkdir -p {{ burrow_root }}/libs/
    mv build/libs/* {{ burrow_root }}/libs/

start-server:
    burrow-server

start-cli:
    @burrow-cli

release version:
    just clean
    just build
    rm -rf dist
    mkdir -p dist/burrow-{{ version }}
    cp build/libs/burrow.jar dist/burrow-{{ version }}/
    cp -r bin dist/burrow-{{ version }}/
    cp README.md dist/burrow-{{ version }}/
    cp LICENSE dist/burrow-{{ version }}/
    cd dist && tar -czf burrow-{{ version }}.tar.gz burrow-{{ version }}/
    shasum -a 256 dist/burrow-{{ version }}.tar.gz > dist/burrow-{{ version }}.tar.gz.sha256
    git tag v{{ version }}
    git push origin v{{ version }}
    cat dist/burrow-{{ version }}.tar.gz.sha256
