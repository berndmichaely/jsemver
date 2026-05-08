# ChangeLog for JSemVer library

## v3.1.0

  * overloaded methods `SemanticVersion::getDescription` and `SemanticVersion::getCanonicalForm` to optionally exlude build info

## v3.0.0

  * replaced `SemanticVersion` class and public constructors by interface and factory methods
  * added factory method to read a `SemanticVersion` from a plain text resource (via `InputStream`)
  * reworked handling of blank *pre-release* and *build* parts by using `Optional`
  * added `SemanticVersion::getVersionParts`
  * added `NumericIdentifier` type
  * changed `int Identifier::getNumber` to `Optional<BigInteger> Identifier::getOptionalNumber`
  * added interface `SemanticVersion.SubRegEx` to describe structural parts of the official regular expression

## v2.0.0

  * added methods:
     + `Build::of`
     + `Identifier::of`
     + `PreRelease::of`
     + `SemanticVersion::check`
  * removed deprecated methods

## v1.0.2

  * improved exception handling (i18n)

## v1.0.1

  * deprecated method `DotSeparatedVersionPart::getListIdentifiers`
  * detail improvements (non-user-visible)

## v1.0.0

initial version
