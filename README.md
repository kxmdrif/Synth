## Build
Skip test for benchmarks which may consume too much time
```text
# Windows Powershell (need add ` before -)
mvn clean package `-Dmaven.test.skip
# Windows CMD
mvn clean package -Dmaven.test.skip

```
## Run benchmarks
```text
mvn test
```

## Run
```text
java -cp "lib;target/synth-1.0.jar" synth.Main examples.txt
```