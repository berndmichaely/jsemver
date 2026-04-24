/*
 * Copyright 2024 Bernd Michaely (info@bernd-michaely.de).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.bernd_michaely.common.semver;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Stream;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static java.util.stream.Collectors.toCollection;
import static java.util.stream.Collectors.toUnmodifiableList;
import static org.junit.jupiter.api.Assertions.*;

/**
 * SemanticVersion Test.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SemanticVersionTest
{
	/**
	 * Official regular expression for semantic versioning.
	 *
	 * @see <a href="https://semver.org">semver.org</a>
	 */
	private static final String STR_REGEX_SEMANTIC_VERSION =
		"^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";

	private static final List<String> LIST_SORTED_STRICTLY_ASCENDING = List.of(
		"0.0.0-0",
		"0.0.0-0.0",
		"0.0.0-0.9",
		"0.0.0-0.10",
		"0.0.0-0.a",
		"0.0.0-1.0",
		"0.0.0--",
		"0.0.0-0-",
		"0.0.0-0-0",
		"0.0.0-alpha",
		"0.0.0",
		"0.9.9",
		"1.0.0-alpha",
		"1.0.0-alpha.1",
		"1.0.0-alpha.beta",
		"1.0.0-beta",
		"1.0.0-beta.2",
		"1.0.0-beta.11",
		"1.0.0-rc.1",
		"1.0.0",
		"2.0.0",
		"2.1.0",
		"2.1.1",
		"9.9.9",
		"9.9.10",
		"9.10.9",
		"10.9.9",
		"10.10.10"
	);

	@BeforeEach
	public void setUp()
	{
		InvalidSemanticVersionException.setExceptionMessageFormatter(null);
	}

	private static List<SemanticVersion> getVersionListAscending()
	{
		return LIST_SORTED_STRICTLY_ASCENDING.stream()
			.map(SemanticVersion::of)
			.collect(toUnmodifiableList());
	}

	@Test
	public void test_SubRegEx()
	{
		final String reconstructed = SemanticVersion.SubRegEx.FULL_SEMANTIC_VERSION;
		System.out.println("test_SubRegEx");
		System.out.println("· official      : »" + STR_REGEX_SEMANTIC_VERSION + "«");
		System.out.println("· reconstructed : »" + reconstructed + "«");
		assertEquals(STR_REGEX_SEMANTIC_VERSION, reconstructed);
	}

	@Test
	public void test_getSupportedVersion()
	{
		assertDoesNotThrow(() -> System.out.println(
			"SemanticVersion::getSupportedVersion : " + SemanticVersion.getSupportedVersion()));
		final var minSupportedVersion = SemanticVersion.of("2.0.0");
		assertTrue(SemanticVersion.getSupportedVersion().compareTo(minSupportedVersion) >= 0);
	}

	@Test
	public void test_getLibVersion()
	{
		assertDoesNotThrow(() -> System.out.println(
			"SemanticVersion::getSupportedVersion : " + SemanticVersion.getLibVersion()));
		final var minSupportedVersion = SemanticVersion.of("2.1.0");
		assertTrue(SemanticVersion.getLibVersion().compareTo(minSupportedVersion) >= 0);
	}

	@Test
	public void test_parse()
	{
		System.out.println("test_parse");
		final SemanticVersion version = SemanticVersion.of("1.2.3-rc.1+b17");
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getPatch());
		assertEquals("rc.1", version.getPreRelease().get().toString());
		assertTrue(version.getPreRelease().isPresent());
		assertTrue(version.getBuild().isPresent());
		final SemanticVersion version0 = SemanticVersion.of("0.0.0");
		assertFalse(version0.getPreRelease().isPresent());
		assertFalse(SemanticVersion.of().getBuild().isPresent());
		assertEquals("b17", version.getBuild().get().toString());
		assertEquals(new DefaultSemanticVersion(
			new NumericIdentifier("1"), new NumericIdentifier("2"), new NumericIdentifier("3"),
			new PreRelease("rc.1"), new Build("b17")), version);
		assertDoesNotThrow(() -> SemanticVersion.of("1.0.0-a00"));
		assertDoesNotThrow(() -> SemanticVersion.of("1.0.0-a0a"));
		assertDoesNotThrow(() -> SemanticVersion.of("1.0.0-0a0"));
		final SemanticVersion sv1 = SemanticVersion.of("1.0.0-Hello-World");
		assertDoesNotThrow(() -> sv1);
		assertEquals(1, sv1.getPreRelease().get().getIdentifiers().size());
		assertEquals("Hello-World", sv1.getPreRelease().get().getIdentifiers().get(0).toString());
		final SemanticVersion sv2 = SemanticVersion.of("1.0.0+r17-rc.1");
		assertDoesNotThrow(() -> sv2);
		assertTrue(sv2.getPreRelease().isEmpty());
		assertEquals("r17-rc.1", sv2.getBuild().get().toString());
		final SemanticVersion sv3 = SemanticVersion.of("1.0.0+-");
		assertDoesNotThrow(() -> sv3);
		assertTrue(sv3.getPreRelease().isEmpty());
		assertEquals("-", sv3.getBuild().get().toString());
		final SemanticVersion sv4 = SemanticVersion.of("1.0.0--");
		assertDoesNotThrow(() -> sv4);
		assertEquals(1, sv4.getPreRelease().get().getIdentifiers().size());
		assertEquals("-", sv4.getPreRelease().get().getIdentifiers().get(0).toString());
		final SemanticVersion sv5 = SemanticVersion.of("1.0.0-0-0");
		assertDoesNotThrow(() -> sv5);
		assertEquals(1, sv5.getPreRelease().get().getIdentifiers().size());
		assertEquals("0-0", sv5.getPreRelease().get().getIdentifiers().get(0).toString());
		assertEquals(2, SemanticVersion.of("1.0.0-0.0").getPreRelease().get().getIdentifiers().size());
		assertEquals(SemanticVersion.of(), SemanticVersion.of("0.0.0-0"));
		assertEquals(new DefaultSemanticVersion(
			new NumericIdentifier("0"), new NumericIdentifier("0"), new NumericIdentifier("0"),
			null, null), version0);
		final SemanticVersion sv6 = SemanticVersion.of("1.0.0+001");
		assertDoesNotThrow(() -> sv6);
		assertTrue(sv6.getBuild().isPresent());
		final Build build6 = sv6.getBuild().get();
		assertEquals(1, build6.getIdentifiers().size());
		final Identifier id6 = build6.getIdentifiers().get(0);
		assertTrue(id6.isNumeric());
		assertEquals(1, id6.getOptionalNumber().get());
		assertEquals("001", id6.toString());
		assertTrue(SemanticVersion.check("1.0.0"));
		assertFalse(SemanticVersion.check("x.y.z"));
	}

	@Test
	public void test_ofResource() throws IOException
	{
		try (var stream = new BufferedInputStream(getClass().getResourceAsStream("missing-file.txt")))
		{
			assertThrows(Exception.class, () -> SemanticVersion.of(stream));
		}
		try (var stream = new BufferedInputStream(getClass().getResourceAsStream("invalid-semver.txt")))
		{
			assertThrows(NoSuchElementException.class, () -> SemanticVersion.of(stream));
		}
		try (var stream = new BufferedInputStream(getClass().getResourceAsStream("valid-semver.txt")))
		{
			assertEquals(SemanticVersion.of("1.0.0-rc.3"), SemanticVersion.of(stream));
		}
	}

	@Test
	public void test_invalid_version_strings()
	{
		Stream.of(
			null,
			"",
			"1",
			"1.",
			"1.0",
			"1.0.",
			"-1.0.0",
			"1.x.y",
			" 1.0.0",
			"1.0.0 ",
			"\t1.0.0",
			"1.0.0\t",
			"1.0.0- 0",
			"1.0.0 -0",
			"1.0.0-00",
			"1.0.0-01",
			"1.0.0-a.00",
			"1.0.0-a.01",
			"x1.0.0",
			"1.0.0y",
			"1.0.0-.",
			"1.0.0+.",
			"1.0.0-a..b",
			"1.0.0+a..b"
		).forEach(version -> assertThrowsExactly(InvalidSemanticVersionException.class, () ->
		{
			try
			{
				SemanticVersion.of(version);
			}
			catch (InvalidSemanticVersionException ex)
			{
				assertEquals(version, ex.getInvalidArgument());
				final String msg = InvalidSemanticVersionException.DEFAULT_MSG_FORMAT.formatted(version);
				System.out.println("· " + msg);
				assertEquals(msg, ex.getMessage());
				assertEquals(msg, ex.getLocalizedMessage());
				throw ex;
			}
		}));
	}

	@Test
	public void test_invalid_version_strings_localized()
	{
		final String msgTemplate = "Invalid SemVer : »%s«";
		InvalidSemanticVersionException.setExceptionMessageFormatter(s -> msgTemplate.formatted(s));
		Stream.of(
			null,
			"",
			"1",
			"1.",
			"1.0",
			"1.0.",
			"-1.0.0",
			"1.x.y",
			"1.0.0- 0",
			"1.0.0 -0",
			"1.0.0-00",
			"1.0.0-01",
			"1.0.0-a.00",
			"1.0.0-a.01",
			"x1.0.0",
			"1.0.0y",
			"1.0.0-.",
			"1.0.0+.",
			"1.0.0-a..b",
			"1.0.0+a..b"
		).forEach(version -> assertThrowsExactly(InvalidSemanticVersionException.class, () ->
		{
			try
			{
				SemanticVersion.of(version);
			}
			catch (InvalidSemanticVersionException ex)
			{
				assertEquals(version, ex.getInvalidArgument());
				assertEquals(InvalidSemanticVersionException.DEFAULT_MSG_FORMAT.formatted(version),
					ex.getMessage());
				assertEquals(msgTemplate.formatted(version), ex.getLocalizedMessage());
				throw ex;
			}
		}));
	}

	private void test_Identifiers(List<Identifier> identifiers)
	{
		assertEquals(3, identifiers.size());
		assertTrue(identifiers.get(0).isNumeric());
		assertEquals(5, identifiers.get(0).getOptionalNumber().get());
		assertEquals("5", identifiers.get(0).toString());
		assertFalse(identifiers.get(1).isNumeric());
		assertFalse(identifiers.get(1).getOptionalNumber().isPresent());
		assertEquals("b", identifiers.get(1).toString());
		assertTrue(identifiers.get(2).isNumeric());
		assertEquals(7, identifiers.get(2).getOptionalNumber().get());
		assertEquals("7", identifiers.get(2).toString());
	}

	@Test
	public void test_Identifier()
	{
		assertEquals(new Identifier("1"), Identifier.of("1", Identifier.Type.PRE_RELEASE));
		assertEquals(new Identifier("abc"), Identifier.of("abc", Identifier.Type.PRE_RELEASE));
		assertEquals(new Identifier("1"), Identifier.of("1", Identifier.Type.BUILD));
		assertEquals(new Identifier("xyz"), Identifier.of("xyz", Identifier.Type.BUILD));
		assertTrue(new Identifier("RC").equalsIgnoreCase(Identifier.of("rc", Identifier.Type.PRE_RELEASE)));
		assertTrue(new Identifier("RC").equalsIgnoreCase("rc", Identifier.Type.PRE_RELEASE));
		assertFalse(new Identifier("RC").equalsIgnoreCase(Identifier.of("7", Identifier.Type.BUILD)));
		assertFalse(new Identifier("RC").equalsIgnoreCase("7", Identifier.Type.BUILD));
		final Function<String, String> msgFormatter = s -> "~" + s;
		InvalidSemanticVersionException.setExceptionMessageFormatter(msgFormatter);
		assertSame(msgFormatter, InvalidSemanticVersionException.getExceptionMessageFormatter());
		assertThrows(InvalidSemanticVersionException.class,
			() -> Identifier.of("#", Identifier.Type.PRE_RELEASE), "~#");
		assertThrows(InvalidSemanticVersionException.class,
			() -> Identifier.of("#", Identifier.Type.BUILD), "~#");
	}

	@Test
	public void test_NumericIdentifier()
	{
		System.out.println("RegEx SemanticVersion NumericIdentifier parts:");
		assertThrows(IllegalStateException.class, () -> new NumericIdentifier(null));
		assertThrows(IllegalStateException.class, () -> new NumericIdentifier(""));
		assertThrows(IllegalStateException.class, () -> new NumericIdentifier("abc"));
		assertEquals(17, new NumericIdentifier("17").getNumber());
		InvalidSemanticVersionException.setExceptionMessageFormatter(s -> "~" + s);
		assertThrows(InvalidSemanticVersionException.class, () -> NumericIdentifier.of("abc"), "~17");
		assertEquals(17, NumericIdentifier.of("17").getNumber());
		assertNotEquals(NumericIdentifier.of("17"), PreRelease.of("-abc.2.def"));
		assertEquals(NumericIdentifier.of("17"), new NumericIdentifier("17"));
		assertNotEquals(NumericIdentifier.of("17"), new NumericIdentifier("18"));
	}

	@Test
	public void test_PreRelease()
	{
		System.out.println("RegEx SemanticVersion PreRelease parts:");
		test_Identifiers(SemanticVersion.of("1.2.3-5.b.7").getPreRelease().get().getIdentifiers());
		assertThrows(InvalidSemanticVersionException.class, () -> PreRelease.of("abc"));
		assertThrows(InvalidSemanticVersionException.class, () -> PreRelease.of("+abc"));
		InvalidSemanticVersionException.setExceptionMessageFormatter(s -> "~" + s);
		assertThrows(InvalidSemanticVersionException.class, () -> PreRelease.of("abc"), "~abc");
		assertEquals(new PreRelease("-abc.2.def"), PreRelease.of("-abc.2.def"));
	}

	@Test
	public void test_Build()
	{
		System.out.println("RegEx SemanticVersion Build parts:");
		test_Identifiers(SemanticVersion.of("1.2.3+5.b.7").getBuild().get().getIdentifiers());
		assertThrows(InvalidSemanticVersionException.class, () -> Build.of("xyz"));
		assertThrows(InvalidSemanticVersionException.class, () -> Build.of("-xyz"));
		InvalidSemanticVersionException.setExceptionMessageFormatter(s -> "~" + s);
		assertThrows(InvalidSemanticVersionException.class, () -> Build.of("xyz"), "~xyz");
		assertEquals(new Build("+uvw.2.xyz"), Build.of("+uvw.2.xyz"));
	}

	@Test
	public void test_equals()
	{
		System.out.println("test_equals");
		final String str1 = "1.2.3+x";
		final SemanticVersion v1 = SemanticVersion.of(str1);
		final SemanticVersion v2 = SemanticVersion.of("1.2.3+y");
		final SemanticVersion v3 = SemanticVersion.of("1.1.1-rc.1");
		assertTrue(v1.equals(v2));
		assertFalse(v1.equals(str1));
		assertFalse(v1.equals(null));
		assertFalse(SemanticVersion.of("2.1.1-rc.1").equals(v3));
		assertFalse(SemanticVersion.of("1.2.1-rc.1").equals(v3));
		assertFalse(SemanticVersion.of("1.1.2-rc.1").equals(v3));
		assertFalse(SemanticVersion.of("1.1.1-rc.2").equals(v3));
		assertEquals(new PreRelease("rc.1"), new PreRelease("rc.1"));
		assertNotEquals(new PreRelease("rc.1"), new PreRelease("rc.2"));
		assertTrue(new Identifier("a").equals(new Identifier("a")));
		assertFalse(new Identifier("a").equals("a"));
		assertFalse(new Identifier("a").equals(new Identifier("b")));
		assertTrue(new Build("a").equals(new Build("a")));
		assertFalse(new Build("a").equals("a"));
		assertFalse(new Build("a").equals(new Build("b")));
	}

	@Test
	public void test_compareTo()
	{
		System.out.println("test_compareTo");
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		final Comparator<SemanticVersion> comparator = SemanticVersion.getComparator();
		for (int i = 1; i < listAscending.size(); i++)
		{
			final SemanticVersion v1 = listAscending.get(i - 1);
			final SemanticVersion v2 = listAscending.get(i);
			final SemanticVersion v3 = SemanticVersion.of(v1.toString());
			System.out.println(("· %" + maxLength + "s < %s").formatted(v1, v2));
			assertTrue(v1.compareTo(v2) < 0);
			assertTrue(v1.compareTo(v3) == 0);
			assertTrue(v2.compareTo(v1) > 0);
			assertTrue(comparator.compare(v1, v2) < 0);
			assertTrue(comparator.compare(v1, v3) == 0);
			assertTrue(comparator.compare(v2, v1) > 0);
		}
		assertIterableEquals(listAscending,
			listAscending.stream().collect(toCollection(TreeSet::new)));
		final var sv1 = SemanticVersion.of("0.0.0-1+1");
		assertTrue(sv1.getPreRelease().isPresent());
		assertTrue(sv1.getBuild().isPresent());
		assertEquals(sv1.getPreRelease().get().toString(), sv1.getBuild().get().toString());
		assertFalse(sv1.getPreRelease().get().equals(sv1.getBuild().get()));
		assertFalse(sv1.getBuild().get().equals(sv1.getPreRelease().get()));
		assertThrows(NullPointerException.class, () -> SemanticVersion.of().compareTo(null));
	}

	@Test
	public void test_hashCode()
	{
		System.out.println("test_hashCode");
		final var sv1 = SemanticVersion.of("1.0.0-x+aaa");
		final var sv2 = SemanticVersion.of("1.0.0-x+bbb");
		assertEquals(sv1, sv2);
		assertEquals(sv1.hashCode(), sv2.hashCode());
		// just list hashcodes:
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		listAscending.stream()
			.map(sv -> ("· %" + maxLength + "s : # %+11d").formatted(sv, sv.hashCode()))
			.forEach(System.out::println);
		assertEquals(new Identifier("abcxyz").hashCode(), new Identifier("abcxyz").hashCode());
		assertEquals(new Build("abcxyz").hashCode(), new Build("abcxyz").hashCode());
	}

	private void _test_toString(String version)
	{
		assertEquals(version, SemanticVersion.of(version).toString());
	}

	@Test
	public void test_toString()
	{
		System.out.println("test_toString");
		_test_toString("1.0.0-rc.1");
		_test_toString("1.0.0+r17");
		_test_toString("1.0.0-rc.1+r17");
		_test_toString("1.0.0");
		_test_toString("2.3.4-rc.1+b17");
		LIST_SORTED_STRICTLY_ASCENDING.forEach(version ->
			assertEquals(version, SemanticVersion.of(version).toString()));
	}

	@Test
	public void test_getDescription()
	{
		System.out.println("test_getDescription");
		final List<SemanticVersion> listAscending = getVersionListAscending();
		final int maxLength = listAscending.stream()
			.map(SemanticVersion::toString).mapToInt(String::length)
			.max().getAsInt();
		listAscending.stream()
			.map(sv -> ("· %" + maxLength + "s -> %s").formatted(sv, sv.getDescription()))
			.forEach(System.out::println);
		assertTrue(SemanticVersion.of("1.0.0+xy").getDescription().startsWith("1.0.0"));
	}

	@Test
	public void test_getVersionParts()
	{
		System.out.println("test_getVersionParts");
		final List<VersionPart> list1 = SemanticVersion.of("1.2.3-rc.4+b.5.6").getVersionParts();
		assertEquals(5, list1.size());
		assertInstanceOf(NumericIdentifier.class, list1.get(0));
		assertEquals(new NumericIdentifier("1"), list1.get(0));
		assertEquals(1, ((NumericIdentifier) list1.get(0)).getNumber());
		assertInstanceOf(NumericIdentifier.class, list1.get(1));
		assertEquals(new NumericIdentifier("2"), list1.get(1));
		assertEquals(2, ((NumericIdentifier) list1.get(1)).getNumber());
		assertInstanceOf(NumericIdentifier.class, list1.get(2));
		assertEquals(new NumericIdentifier("3"), list1.get(2));
		assertEquals(3, ((NumericIdentifier) list1.get(2)).getNumber());
		assertInstanceOf(PreRelease.class, list1.get(3));
		assertEquals(new PreRelease("rc.4"), list1.get(3));
		assertInstanceOf(Build.class, list1.get(4));
		assertEquals(new Build("b.5.6"), list1.get(4));
		final List<VersionPart> list2 = SemanticVersion.of("7.8.9-beta").getVersionParts();
		assertEquals(4, list2.size());
		assertEquals("beta", list2.get(3).getPart());
		final List<VersionPart> list3 = SemanticVersion.of("7.8.9+b17").getVersionParts();
		assertEquals(4, list3.size());
		assertEquals("b17", list3.get(3).getPart());
		final List<VersionPart> list4 = SemanticVersion.of("7.8.9").getVersionParts();
		assertEquals(3, list4.size());
	}
}
