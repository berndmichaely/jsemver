# jsemver

![doc/shields/version.svg](doc/shields/version.svg)

Java modular library to implement the Semantic Versioning 2.0.0 specification found at [semver.org](https://semver.org/).

### usage example

```java
var semver1 = new SemanticVersion("1.0.0-rc.9+xy");
var semver2 = new SemanticVersion("1.0.0-rc.10");
System.out.println("'%s' is %s than '%s'".formatted(
  semver1,
  (semver1.compareTo(semver2) > 0) ? "greater" : "less",
  semver2
));
```

will output:

`'1.0.0-rc.9+xy' is less than '1.0.0-rc.10'`
