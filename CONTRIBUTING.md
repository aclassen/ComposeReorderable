# Contributing to ComposeReorderable

Thank you for your interest in contributing!

## Prerequisites

- JDK 11 or later
- Android SDK (for building the `:android` sample app)

## Building the project

Clone the repository and run:

```bash
./gradlew :reorderable:compileKotlinJvm
./gradlew :android:assembleDebug
./gradlew :desktop:run
```

To run the full check suite (includes JS targets):

```bash
./gradlew check
```

> **Note:** Publishing the `reorderable` library requires `ossrh.Username` and `ossrh.Password` credentials in `gradle.properties`. Local development does not need these.

## Making changes

1. Fork the repository and create a feature branch from `main`.
2. Make your changes in the `reorderable` module or sample apps.
3. Verify your changes compile:

   ```bash
   ./gradlew :reorderable:compileKotlinJvm :android:assembleDebug
   ```

4. Open a pull request against `main` with a clear description of the change and how to test it.

## Code style

- Follow [Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- Match the existing style and structure of the module you are editing.
- Keep changes focused — one logical improvement per pull request.

## Reporting issues

Please use the [GitHub issue tracker](https://github.com/aclassen/ComposeReorderable/issues) to report bugs or request features. Include steps to reproduce, expected behavior, and your Compose/Kotlin versions when reporting bugs.
