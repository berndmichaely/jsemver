# jsemver

![doc/shields/version.svg](doc/shields/version.svg)

**jsemver** is a modular Java library to implement the Semantic Versioning 2.0.0 specification found at [semver.org](https://semver.org/).

#### usage example

```java
void compareSemanticVersions(SemanticVersion sv1, SemanticVersion sv2)
{
  final int c = sv1.compareTo(sv2);
  final String s = c > 0 ? "greater than" : (c < 0 ? "less than" : "equal to");
  System.out.println ("'%s' is %s '%s'".formatted(sv1, s, sv2));
}

final var sv = new SemanticVersion("1.0.0-rc.1+xyz");
System.out.println ("%s -> %s".formatted(sv, sv.getDescription()));

compareSemanticVersions(
  new SemanticVersion("1.0.0-rc.9"),
  new SemanticVersion("1.0.0-rc.10"));

compareSemanticVersions(
  new SemanticVersion("1.0.0-rc.3"),
  new SemanticVersion("1.0.0"));

compareSemanticVersions(
  new SemanticVersion("1.0.0"),
  new SemanticVersion("1.0.0+build.id"));
```

will output:

```
1.0.0-rc.1+xyz -> 1.0.0 pre-release »rc.1« build »xyz«

'1.0.0-rc.9' is less than '1.0.0-rc.10'

'1.0.0-rc.3' is less than '1.0.0'

'1.0.0' is equal to '1.0.0+build.id'
```
