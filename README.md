# JSemVer

![doc/shields/version.svg](doc/shields/version.svg)

**JSemVer** is a modular Java library to implement the Semantic Versioning 2.0.0 specification found at [semver.org](https://semver.org/).

It is available at maven coordinates:

```
de.bernd-michaely:jsemver:${version}
```

#### Dependencies

```java
module de.bernd_michaely.common.semver
{
  requires org.checkerframework.checker.qual;
  exports de.bernd_michaely.common.semver;
}
```

#### Usage

##### example:

```java
import de.bernd_michaely.common.semver.*;

final var sv = new SemanticVersion("1.0.0-rc.3+xyz");

System.out.println (sv.toString());
System.out.println (sv.getDescription());
```

##### output:

```
1.0.0-rc.3+xyz
1.0.0 pre-release »rc.3« build »xyz«
```

##### example:


```java
final List<Identifier> ids = sv.getPreRelease().getIdentifiers();

if (ids.size() >= 2 && ids.get(0).equalsIgnoreCase(Identifier.of("RC", Identifier.Type.PRE_RELEASE)) && ids.get(1).isNumeric())
  System.out.println ("%s is release candidate %d.".formatted(sv, ids.get(1).getNumber()));
else
  System.out.println ("%s is not a release candidate.".formatted(sv));
```

##### output:

```
1.0.0-rc.3+xyz is release candidate 3.
```

##### example:

```java
void compareSemanticVersions(SemanticVersion sv1, SemanticVersion sv2)
{
  final int c = sv1.compareTo(sv2);
  final String s = c > 0 ? "greater than" : (c < 0 ? "less than" : "equal to");
  System.out.println ("'%s' is %s '%s'".formatted(sv1, s, sv2));
}

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

##### output:

```
'1.0.0-rc.9' is less than '1.0.0-rc.10'

'1.0.0-rc.3' is less than '1.0.0'

'1.0.0' is equal to '1.0.0+build.id'
```
