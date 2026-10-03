# Claim Quest — Fabric 1.21.1

Fabric startup and FTB integration live in this cell. The two platform-independent task classes are byte-identical copies of the NeoForge implementation. Language files and the logo are read from the root resources. FTB Quests, FTB Chunks, FTB Teams, and FTB Library are optional at runtime; compile-only host dependencies keep the shipped jar free of their classes.

Build with Java 21 and Gradle 8.13:

```sh
./gradlew build
```

`./gradlew runClient` includes the pinned FTB host mods. `./gradlew -PwithoutFtb runClient` checks the no-host-mod startup path. Runtime checks remain pending until the parent task assigns the shared Gradle/game slot.

`./gradlew -PwithoutFtbChunks runClient` retains Quests, Teams, and Library while omitting Chunks, covering the task-registered-but-not-progressing path.
