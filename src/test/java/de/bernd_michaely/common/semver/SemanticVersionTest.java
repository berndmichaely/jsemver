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

import de.bernd_michaely.common.semver.SemanticVersion.SubRegEx;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

import static de.bernd_michaely.common.semver.SemanticVersion.STR_REGEX_SEMANTIC_VERSION;
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

	private static List<SemanticVersion> getVersionListAscending()
	{
		return LIST_SORTED_STRICTLY_ASCENDING.stream()
			.map(SemanticVersion::new)
			.collect(toUnmodifiableList());
	}

	@Test
	public void test_getSupportedVersion()
	{
		assertDoesNotThrow(() -> System.out.println(
			"SemanticVersion::getSupportedVersion : " + SemanticVersion.getSupportedVersion()));
		final var minSupportedVersion = new SemanticVersion(2, 0, 0, null, null);
		assertTrue(SemanticVersion.getSupportedVersion().compareTo(minSupportedVersion) >= 0);
	}

	@Test
	public void test_parse()
	{
		System.out.println("test_parse");
		final SemanticVersion version = new SemanticVersion("1.2.3-rc.1+b17");
		assertEquals(1, version.getMajor());
		assertEquals(2, version.getMinor());
		assertEquals(3, version.getPatch());
		assertEquals("rc.1", version.getPreRelease().toString());
		assertFalse(version.getPreRelease().isBlank());
		assertFalse(version.getBuild().isBlank());
		final SemanticVersion version0 = new SemanticVersion("0.0.0");
		assertTrue(version0.getPreRelease().isBlank());
		assertTrue(new SemanticVersion().getBuild().isBlank());
		assertEquals("b17", version.getBuild().toString());
		assertEquals(new SemanticVersion(1, 2, 3,
			new PreRelease("rc.1"), new Build("b17")), version);
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-a00"));
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-a0a"));
		assertDoesNotThrow(() -> new SemanticVersion("1.0.0-0a0"));
		final SemanticVersion sv1 = new SemanticVersion("1.0.0-Hello-World");
		assertDoesNotThrow(() -> sv1);
		assertEquals(1, sv1.getPreRelease().getIdentifiers().size());
		assertEquals("Hello-World", sv1.getPreRelease().getIdentifiers().get(0).toString());
		final SemanticVersion sv2 = new SemanticVersion("1.0.0+r17-rc.1");
		assertDoesNotThrow(() -> sv2);
		assertTrue(sv2.getPreRelease().getIdentifiers().isEmpty());
		assertEquals("r17-rc.1", sv2.getBuild().toString());
		final SemanticVersion sv3 = new SemanticVersion("1.0.0+-");
		assertDoesNotThrow(() -> sv3);
		assertTrue(sv3.getPreRelease().getIdentifiers().isEmpty());
		assertEquals("-", sv3.getBuild().toString());
		final SemanticVersion sv4 = new SemanticVersion("1.0.0--");
		assertDoesNotThrow(() -> sv4);
		assertEquals(1, sv4.getPreRelease().getIdentifiers().size());
		assertEquals("-", sv4.getPreRelease().getIdentifiers().get(0).toString());
		final SemanticVersion sv5 = new SemanticVersion("1.0.0-0-0");
		assertDoesNotThrow(() -> sv5);
		assertEquals(1, sv5.getPreRelease().getIdentifiers().size());
		assertEquals("0-0", sv5.getPreRelease().getIdentifiers().get(0).toString());
		assertEquals(2, new SemanticVersion("1.0.0-0.0").getPreRelease().getIdentifiers().size());
		assertEquals(new SemanticVersion(), new SemanticVersion("0.0.0-0"));
		assertEquals(new SemanticVersion(0, 0, 0, new PreRelease(),
			new Build()), version0);
		final SemanticVersion sv6 = new SemanticVersion("1.0.0+001");
		assertDoesNotThrow(() -> sv6);
		final Build build6 = sv6.getBuild();
		assertFalse(build6.isBlank());
		assertEquals(1, build6.getIdentifiers().size());
		final Identifier id6 = build6.getIdentifiers().get(0);
		assertTrue(id6.isNumeric());
		assertEquals(1, id6.getNumber());
		assertEquals("001", id6.toString());
		assertTrue(SemanticVersion.check("1.0.0"));
		assertFalse(SemanticVersion.check("x.y.z"));
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
				new SemanticVersion(version);
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
				new SemanticVersion(version, s -> msgTemplate.formatted(s));
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
		assertEquals(5, identifiers.get(0).getNumber());
		assertEquals("5", identifiers.get(0).toString());
		assertFalse(identifiers.get(1).isNumeric());
		assertTrue(identifiers.get(1).getNumber() < 0);
		assertEquals("b", identifiers.get(1).toString());
		assertTrue(identifiers.get(2).isNumeric());
		assertEquals(7, identifiers.get(2).getNumber());
		assertEquals("7", identifiers.get(2).toString());
	}

	private void printlnSubRegEx(SubRegEx subRegEx)
	{
		System.out.println(("%" + subRegEx.endIndex + "s").formatted(subRegEx.toString()));
	}

	@Test
	public void test_Identifier()
	{
		assertEquals(new Identifier("1"), Identifier.of("1", Identifier.Type.PRE_RELEASE));
		assertEquals(new Identifier("abc"), Identifier.of("abc", Identifier.Type.PRE_RELEASE));
		assertEquals(new Identifier("1"), Identifier.of("1", Identifier.Type.BUILD));
		assertEquals(new Identifier("xyz"), Identifier.of("xyz", Identifier.Type.BUILD));
		assertTrue(new Identifier("RC").equalsIgnoreCase(Identifier.of("rc", Identifier.Type.PRE_RELEASE)));
		assertFalse(new Identifier("RC").equalsIgnoreCase(Identifier.of("7", Identifier.Type.BUILD)));
		assertThrows(InvalidSemanticVersionException.class,
			() -> Identifier.of("#", Identifier.Type.PRE_RELEASE, s -> "~" + s), "~#");
		assertThrows(InvalidSemanticVersionException.class,
			() -> Identifier.of("#", Identifier.Type.BUILD, s -> "~" + s), "~#");
	}

	@Test
	public void test_PreRelease()
	{
		System.out.println("RegEx SemanticVersion PreRelease parts:");
		System.out.println(STR_REGEX_SEMANTIC_VERSION);
		printlnSubRegEx(SubRegEx.STR_REGEX_PRE_RELEASE);
		printlnSubRegEx(SubRegEx.STR_REGEX_ID_PRE_RELEASE);
		System.out.println();
		assertTrue(new PreRelease(null).isBlank());
		assertTrue(new PreRelease("").isBlank());
		test_Identifiers(new SemanticVersion("1.2.3-5.b.7").getPreRelease().getIdentifiers());
		assertThrows(InvalidSemanticVersionException.class, () -> PreRelease.of("abc"));
		assertThrows(InvalidSemanticVersionException.class, () -> PreRelease.of("+abc"));
		assertThrows(InvalidSemanticVersionException.class,
			() -> PreRelease.of("abc", s -> "~" + s), "~abc");
		assertEquals(new PreRelease("-abc.2.def"), PreRelease.of("-abc.2.def"));
	}

	@Test
	public void test_Build()
	{
		System.out.println("RegEx SemanticVersion Build parts:");
		System.out.println(STR_REGEX_SEMANTIC_VERSION);
		printlnSubRegEx(SubRegEx.STR_REGEX_BUILD);
		printlnSubRegEx(SubRegEx.STR_REGEX_ID_BUILD);
		System.out.println();
		assertTrue(new Build(null).isBlank());
		assertTrue(new Build("").isBlank());
		test_Identifiers(new SemanticVersion("1.2.3+5.b.7").getBuild().getIdentifiers());
		assertThrows(InvalidSemanticVersionException.class, () -> Build.of("xyz"));
		assertThrows(InvalidSemanticVersionException.class, () -> Build.of("-xyz"));
		assertThrows(InvalidSemanticVersionException.class,
			() -> Build.of("xyz", s -> "~" + s), "~xyz");
		assertEquals(new Build("+uvw.2.xyz"), Build.of("+uvw.2.xyz"));
	}

	@Test
	public void test_equals()
	{
		System.out.println("test_equals");
		final String str1 = "1.2.3+x";
		final SemanticVersion v1 = new SemanticVersion(str1);
		final SemanticVersion v2 = new SemanticVersion("1.2.3+y");
		final SemanticVersion v3 = new SemanticVersion("1.1.1-rc.1");
		assertTrue(v1.equals(v2));
		assertFalse(v1.equals(str1));
		assertFalse(v1.equals(null));
		assertFalse(new SemanticVersion("2.1.1-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.2.1-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.1.2-rc.1").equals(v3));
		assertFalse(new SemanticVersion("1.1.1-rc.2").equals(v3));
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
			final SemanticVersion v3 = new SemanticVersion(v1.toString());
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
		final var sv1 = new SemanticVersion("0.0.0-1+1");
		assertFalse(sv1.getPreRelease().isBlank());
		assertFalse(sv1.getBuild().isBlank());
		assertEquals(sv1.getPreRelease().toString(), sv1.getBuild().toString());
		assertFalse(sv1.getPreRelease().equals(sv1.getBuild()));
		assertFalse(sv1.getBuild().equals(sv1.getPreRelease()));
		assertThrows(NullPointerException.class, () -> new SemanticVersion().compareTo(null));
	}

	@Test
	public void test_hashCode()
	{
		System.out.println("test_hashCode");
		final var sv1 = new SemanticVersion("1.0.0-x+aaa");
		final var sv2 = new SemanticVersion("1.0.0-x+bbb");
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
		assertEquals(version, new SemanticVersion(version).toString());
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
			assertEquals(version, new SemanticVersion(version).toString()));
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
		assertTrue(new SemanticVersion("1.0.0+xy").getDescription().startsWith("1.0.0"));
	}
}
