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

import java.util.Comparator;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNullElse;

/**
 * Utility for semantic versioning.
 *
 * @see <a href="https://semver.org">semver.org</a>
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class SemanticVersion implements Comparable<SemanticVersion>
{
	private static @MonotonicNonNull SemanticVersion VERSION_SEMVER;

	/**
	 * Official regular expression for semantic versioning.
	 *
	 * @see <a href="https://semver.org">semver.org</a>
	 */
	public static final String STR_REGEX_SEMANTIC_VERSION =
		"^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)(?:-((?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*)(?:\\.(?:0|[1-9]\\d*|\\d*[a-zA-Z-][0-9a-zA-Z-]*))*))?(?:\\+([0-9a-zA-Z-]+(?:\\.[0-9a-zA-Z-]+)*))?$";
	private static @MonotonicNonNull Pattern patternSemVer;

	enum SubRegEx
	{
		STR_REGEX_PRE_RELEASE(44, 134),
		STR_REGEX_ID_PRE_RELEASE(49, 85),
		STR_REGEX_BUILD(139, 176),
		STR_REGEX_ID_BUILD(142, 155);

		final int beginIndex, endIndex;

		SubRegEx(int startIndex, int endIndex)
		{
			this.beginIndex = startIndex;
			this.endIndex = endIndex;
		}

		@Override
		public String toString()
		{
			return STR_REGEX_SEMANTIC_VERSION.substring(beginIndex, endIndex);
		}
	}

	private final int major;
	private final int minor;
	private final int patch;
	private final PreRelease preRelease;
	private final Build build;

	/**
	 * Creates a default instance with a canonical semantic version. The value is
	 * the smallest possible semantic version.
	 */
	public SemanticVersion()
	{
		this("0.0.0-0");
	}

	/**
	 * Creates a new instance for a given semantic version string.
	 *
	 * @param semanticVersion a String containing a semantic version
	 * @throws InvalidSemanticVersionException if the given semantic version
	 *                                         String is invalid (including null)
	 */
	public SemanticVersion(String semanticVersion)
	{
		this(semanticVersion, null);
	}

	/**
	 * Creates a new instance.
	 *
	 * @param semanticVersion       a String containing a semantic version
	 * @param exceptionMsgFormatter function from an invalid semanticVersion
	 *                              argument String to a localized
	 *                              InvalidSemanticVersionException message. Can
	 *                              be {@code null} to use the default formatting
	 * @throws InvalidSemanticVersionException if the given semantic version
	 *                                         String is invalid (including null)
	 * @since 1.0.2
	 */
	public SemanticVersion(String semanticVersion,
		@Nullable Function<String, String> exceptionMsgFormatter)
	{
		final var matcher = getMatcher(semanticVersion);
		if (matcher.matches())
		{
			this.major = parseInt(matcher.group(1));
			this.minor = parseInt(matcher.group(2));
			this.patch = parseInt(matcher.group(3));
			this.preRelease = new PreRelease(matcher.group(4));
			this.build = new Build(matcher.group(5));
		}
		else
		{
			throw new InvalidSemanticVersionException(semanticVersion, exceptionMsgFormatter);
		}
	}

	SemanticVersion(int major, int minor, int patch, PreRelease preRelease, Build build)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preRelease != null ? preRelease : new PreRelease();
		this.build = build != null ? build : new Build();
	}

	@SuppressWarnings("argument")
	private static int parseInt(@Nullable String s)
	{
		// a null argument will throw a NumberFormatException, not a NPE
		return Integer.parseInt(s);
	}

	/**
	 * Returns the supported version of the semantic version standard.
	 *
	 * @return the supported version of the semantic version standard
	 */
	public static SemanticVersion getSupportedVersion()
	{
		if (VERSION_SEMVER == null)
		{
			VERSION_SEMVER = new SemanticVersion("2.0.0");
		}
		return VERSION_SEMVER;
	}

	/**
	 * Returns the major version.
	 *
	 * @return the major version
	 */
	public int getMajor()
	{
		return major;
	}

	/**
	 * Returns the minor version.
	 *
	 * @return the minor version
	 */
	public int getMinor()
	{
		return minor;
	}

	/**
	 * Returns the patch version.
	 *
	 * @return the patch version
	 */
	public int getPatch()
	{
		return patch;
	}

	/**
	 * Returns the pre-release version.
	 *
	 * @return the pre-release version
	 */
	public PreRelease getPreRelease()
	{
		return preRelease;
	}

	/**
	 * Returns the build version.
	 *
	 * @return the build version
	 */
	public Build getBuild()
	{
		return build;
	}

	private static Matcher getMatcher(String semanticVersion)
	{
		if (patternSemVer == null)
		{
			patternSemVer = Pattern.compile(STR_REGEX_SEMANTIC_VERSION);
		}
		return patternSemVer.matcher(requireNonNullElse(semanticVersion, ""));
	}

	/**
	 * Returns true, iff the argument is a valid semantic version.
	 *
	 * @param semanticVersion the argument to check
	 * @return true, iff the argument is a valid semantic version
	 * @since 2.0.0
	 */
	public static boolean check(String semanticVersion)
	{
		return getMatcher(semanticVersion).matches();
	}

	private static final Comparator<SemanticVersion> semanticVersionComparator =
		Comparator.comparingInt(SemanticVersion::getMajor)
			.thenComparingInt(SemanticVersion::getMinor)
			.thenComparingInt(SemanticVersion::getPatch)
			.thenComparing(SemanticVersion::getPreRelease);

	/**
	 * Returns a Comparator for SemanticVersion instances.
	 *
	 * @return a Comparator for SemanticVersion instances
	 */
	public static final Comparator<SemanticVersion> getComparator()
	{
		return semanticVersionComparator;
	}

	@Override
	public int compareTo(SemanticVersion other)
	{
		return semanticVersionComparator.compare(this, other);
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof SemanticVersion other)
		{
			return this.compareTo(other) == 0;
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		// Note:
		// 'build' must not go into hashCode,
		// since it is not partof the linear ordering
		return Objects.hash(major, minor, patch, preRelease);
	}

	/**
	 * Returns a more verbose string than the canonical form.
	 *
	 * @return a more verbose string than the canonical form
	 * @see #toString()
	 */
	public String getDescription()
	{
		return "%d.%d.%d%s%s".formatted(major, minor, patch,
			preRelease.isBlank() ? "" : " pre-release »" + preRelease + "«",
			build.isBlank() ? "" : " build »" + build + "«");
	}

	/**
	 * Returns the semantic version in its canonical form.
	 *
	 * @return the canonical form of the semantic version
	 * @see #getDescription()
	 */
	@Override
	public String toString()
	{
		return "%d.%d.%d%s%s".formatted(major, minor, patch,
			preRelease.isBlank() ? "" : "-" + preRelease,
			build.isBlank() ? "" : "+" + build);
	}
}
