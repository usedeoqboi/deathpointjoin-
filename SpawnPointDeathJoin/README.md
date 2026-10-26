# SpawnPointDeathJoin
Paper 26.2 plugin: join/death spawn, join welcome, and leave goodbye.

## Build
Requires JDK 25 and Gradle supporting Java 25. Run `gradle build` from this project folder; the JAR appears in `build/libs/`.

## GitHub build
Upload/extract this project at the root of a GitHub repository. The included Actions workflow builds the JAR on push. Download it from the completed workflow run under **Artifacts**.

## Use
Install the JAR in `plugins/`, start the server, then stand at the desired location and run `/spawnpointdeathjoin` as an operator. Edit `plugins/SpawnPointDeathJoin/config.yml` for messages. Use `{player}` for the name and `&` color codes.
