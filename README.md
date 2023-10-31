# halban
JavaFX Sokoban app

JavaFX example app targeting "all" platforms using GluonFX with Graalvm


Build and run with

```
mvn clean install
mvn javafx:run -f sokoban-app
```

To build and run a native application for macos, use

```
mvn gluonfx:build -f sokoban-app -Pmacos
./sokoban-app/target/gluonfx/aarch64-darwin/halban
```

To build an installable app use

```
mvn gluonfx:package -f sokoban-app -Pmacos
open ./sokoban-app/target/gluonfx/aarch64-darwin/halban.app
```
