
## Build 
### [_optional_] Build egg library (because the complied library has been put in the resources folder)
In `egg-synth/`
```commandline
cargo buid
```
Then move the `egg_synth.dll` file to the resources/win32-x86-64 folder
### Build project
```commandline
mvn clean package
```

## Run
```commandline
java -jar target/synth-1.0-jar-with-dependencies.jar examples.txt
```
Note: maven assembly plugin is used to package all dependencies to the jar file,
so we do not need to specify dependencies/libs in the command line.