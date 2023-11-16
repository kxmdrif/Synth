```commandline
java -cp "lib/*:target/synth-1.0.jar" synth.Main examples.txt
```

packaging with all dependencies makes it can be 
run in a single jar without specifying dependencies in command line, like:
```commandline
java -jar target/synth-1.0-jar-with-dependencies.jar  examples.txt
```
use maven assembly plugin