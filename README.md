# SpawnPointDeathJoin

Paper 26.2 plugin with shared join/death spawn, chat greetings, automatic dropped-item cleanup, and restricted item removal.

The GitHub Actions workflow is at the repository root. The project is inside `SpawnPointDeathJoin/` to match this ZIP's layout. Upload the ZIP's extracted contents to the top level of the GitHub repository. A successful Actions run will contain the built JAR as an artifact.

Stand at the desired location and run `/spawnpointdeathjoin` as an operator. `cleanup.ground-items-interval-seconds` controls how often all dropped ground items are removed. `cleanup.illegal-item-check-interval-seconds` controls how often inventories are checked. Non-OP players have spawners and all spawn eggs removed from their inventory and ender chest. OP players are exempt.
