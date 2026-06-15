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

import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.checker.regex.qual.Regex;

import static de.bernd_michaely.common.semver.SemanticVersion.Format.*;

/**
 * Utility for semantic versioning.
 *
 * @see <a href="https://semver.org">semver.org</a>
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public sealed interface SemanticVersion extends Comparable<SemanticVersion>
	permits DefaultSemanticVersion
{
	/**
	 * Controls the format of String representations of semantic versions.
	 *
	 * @since 4.0.0
	 */
	enum Format
	{
		/**
		 * A String representation in the canonical form, that is, according to the
		 * official specification.
		 */
		CANONICAL,
		/**
		 * A String representation in a deliberate descriptive form. (Don't expect
		 * the output to be stable over different library versions.)
		 */
		VERBOSE,
		/**
		 * A String representation showing the structure, in particular of the dot
		 * separated version parts. (This is mainly intended for testing and
		 * debugging. Don't expect the output to be stable over different library
		 * versions.)
		 */
		STRUCTURED;

		/**
		 * Returns the default format for semantic versions.
		 *
		 * @return {@link #CANONICAL}
		 */
		public static Format getDefaultFormat()
		{
			return CANONICAL;
		}

		/**
		 * Returns the given format, if not {@code null}, otherwise the default
		 * format.
		 *
		 * @param format the given format
		 * @return the given format, if not {@code null}, otherwise
		 *         {@link #getDefaultFormat()}
		 */
		public static Format getFormatOrDefault(@Nullable Format format)
		{
			return format != null ? format : getDefaultFormat();
		}
	}

	/**
	 * Interface to describe structural parts of the official regular expression
	 * for semantic versioning.
	 *
	 * @since 3.0.0
	 */
	interface SubRegEx
	{
		/**
		 * Regular expression for dot separator.
		 */
		@Regex
		String DOT_SEPARATOR = "\\.";
		/**
		 * Regular expression for numeric identifiers without leading zeros.
		 */
		@Regex
		String NUMERIC_IDENTIFIER = "0|[1-9]\\d*";
		/**
		 * Regular expression for pre-release identifier.
		 */
		@Regex
		String PRE_RELEASE_IDENTIFIER = NUMERIC_IDENTIFIER + "|\\d*[a-zA-Z-][0-9a-zA-Z-]*";
		/**
		 * Regular expression for build identifier.
		 */
		@Regex
		String BUILD_IDENTIFIER = "[0-9a-zA-Z-]+";

		/**
		 * Regular expression for version core.
		 */
		@Regex
		String VERSION_CORE =
			"(" + NUMERIC_IDENTIFIER + ")" + DOT_SEPARATOR +
			"(" + NUMERIC_IDENTIFIER + ")" + DOT_SEPARATOR +
			"(" + NUMERIC_IDENTIFIER + ")";
		/**
		 * Regular expression for pre-release.
		 */
		@Regex
		String PRE_RELEASE = "(?:-((?:" +
			PRE_RELEASE_IDENTIFIER + ")(?:\\.(?:" +
			PRE_RELEASE_IDENTIFIER + "))*))?";
		/**
		 * Regular expression for build.
		 */
		@Regex
		String BUILD = "(?:\\+(" + BUILD_IDENTIFIER + "(?:\\." + BUILD_IDENTIFIER + ")*))?";

		/**
		 * Regular expression to match a full String as substring.
		 */
		@Regex
		String SEMANTIC_VERSION = VERSION_CORE + PRE_RELEASE + BUILD;
		/**
		 * Regular expression to match a full String as semantic version.
		 */
		@Regex
		String FULL_SEMANTIC_VERSION = "^" + SEMANTIC_VERSION + "$";
	}

	/**
	 * Official regular expression for semantic versioning.
	 *
	 * @see <a href="https://semver.org">semver.org</a>
	 */
	String STR_REGEX_SEMANTIC_VERSION = SubRegEx.FULL_SEMANTIC_VERSION;

	/**
	 * Returns the major version. If the correct value is greater than
	 * {@code 2^31-1}, returns a negative value.
	 *
	 * @return the major version
	 * @see #getMajorValue()
	 */
	int getMajor();

	/**
	 * Returns the major version.
	 *
	 * @return the major version
	 * @see #getMajor()
	 * @since 3.0.0
	 */
	BigInteger getMajorValue();

	/**
	 * Returns the minor version. If the correct value is greater than
	 * {@code 2^31-1}, returns a negative value.
	 *
	 * @return the minor version
	 * @see #getMinorValue()
	 */
	int getMinor();

	/**
	 * Returns the minor version.
	 *
	 * @return the minor version
	 * @see #getMinor()
	 * @since 3.0.0
	 */
	BigInteger getMinorValue();

	/**
	 * Returns the patch version. If the correct value is greater than
	 * {@code 2^31-1}, returns a negative value.
	 *
	 * @return the patch version
	 * @see #getPatchValue()
	 */
	int getPatch();

	/**
	 * Returns the patch version.
	 *
	 * @return the patch version
	 * @see #getPatch()
	 * @since 3.0.0
	 */
	BigInteger getPatchValue();

	/**
	 * Returns the optional pre-release version.
	 *
	 * @return the optional pre-release version
	 */
	Optional<PreRelease> getPreRelease();

	/**
	 * Returns the optional build version.
	 *
	 * @return the optional build version
	 */
	Optional<Build> getBuild();

	/**
	 * Creates a default instance with a canonical semantic version. The value is
	 * the smallest possible semantic version.
	 *
	 * @return a new instance
	 * @since 3.0.0
	 */
	public static SemanticVersion of()
	{
		return of("0.0.0-0");
	}

	/**
	 * Creates a new instance for a given semantic version string.
	 *
	 * @param semanticVersion a String containing a semantic version
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given semantic version
	 *                                         String is invalid (including null)
	 * @since 3.0.0
	 */
	public static SemanticVersion of(String semanticVersion)
	{
		return DefaultSemanticVersion.of(semanticVersion);
	}

	/**
	 * Reads a SemanticVersion from a resource.
	 *
	 * @param inputStream an InputStream providing a plain text resource. Invalid
	 *                    lines (e.g. empty, or commented out with leading '#')
	 *                    will be skipped silently, the first line containing a
	 *                    valid semantic version string will be used.
	 * @return a new instance
	 * @see Class#getResourceAsStream(String)
	 * @throws InvalidSemanticVersionException if no valid SemanticVersion is
	 *                                         found in the input
	 * @throws IOException                     on error on stream close
	 * @since 3.0.0
	 */
	static SemanticVersion of(InputStream inputStream) throws IOException
	{
		return DefaultSemanticVersion.of(inputStream);
	}

	/**
	 * Returns the supported version of the semantic version standard.
	 *
	 * @return the supported version of the semantic version standard
	 */
	public static SemanticVersion getSupportedVersion()
	{
		return DefaultSemanticVersion.getSupportedVersion();
	}

	/**
	 * Returns the version of this library.
	 *
	 * @return the version of this library
	 * @since 3.0.0
	 */
	public static SemanticVersion getLibVersion()
	{
		return DefaultSemanticVersion.getLibVersion();
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
		return DefaultSemanticVersion.getMatcher(semanticVersion).matches();
	}

	/**
	 * Returns a Comparator for SemanticVersion instances.
	 *
	 * @return a Comparator for SemanticVersion instances
	 */
	public static Comparator<SemanticVersion> getComparator()
	{
		return DefaultSemanticVersion.getComparator();
	}

	/**
	 * Returns a list of the present version parts. That is, the list size varies
	 * between 3 and 5.
	 *
	 * @return a list of the present version parts
	 */
	List<VersionPart> getVersionParts();

	/**
	 * Returns the semantic version in its canonical form. Same as
	 * {@code getCanonicalForm(false)}.
	 *
	 * @return the canonical form of the semantic version
	 * @see #getCanonicalForm(boolean)
	 * @deprecated same as {@code toString(CANONICAL)}
	 */
	@Deprecated
	default String getCanonicalForm()
	{
		return getCanonicalForm(false);
	}

	/**
	 * Returns the semantic version in its canonical form.
	 *
	 * @param excludeBuild if true, don't include the build info in the result
	 * @return the canonical form of the semantic version
	 * @see #getDescription()
	 * @deprecated same as {@code toString(CANONICAL, excludeBuild)}
	 */
	@Deprecated
	default String getCanonicalForm(boolean excludeBuild)
	{
		return toString(CANONICAL, excludeBuild);
	}

	/**
	 * Returns a more verbose string than the canonical form. Same as
	 * {@code getDescription(false)}.
	 *
	 * @return a more verbose string than the canonical form
	 * @see #getDescription(boolean)
	 * @deprecated same as {@code toString(VERBOSE)}
	 */
	@Deprecated
	default String getDescription()
	{
		return getDescription(false);
	}

	/**
	 * Returns a more verbose string than the canonical form.
	 *
	 * @param excludeBuild if true, don't include the build info in the result
	 * @return a more verbose string than the canonical form
	 * @see #getCanonicalForm()
	 * @deprecated same as {@code toString(VERBOSE, excludeBuild)}
	 */
	@Deprecated
	default String getDescription(boolean excludeBuild)
	{
		return toString(VERBOSE, excludeBuild);
	}

	/**
	 * Returns the canonical form of the semantic version. Same as
	 * {@code toString(CANONICAL, false)}.
	 *
	 * @return a String equal to the given initial argument
	 */
	@Override
	public String toString();

	/**
	 * Returns a String representation of the semantic version in the requested
	 * format. Same as {@code toString(format, false)}.
	 *
	 * @param format the format to use for the generated String ({@code null} for
	 *               {@link Format#CANONICAL})
	 *
	 * @return a String representation of the semantic version in the requested
	 *         format
	 * @since 4.0.0
	 */
	default String toString(@Nullable Format format)
	{
		return toString(format, false);
	}

	/**
	 * Returns a String representation of the semantic version in the requested
	 * format. Same as {@code toString(format, false)}.
	 *
	 * @param excludeBuild if true, don't include the build info in the result
	 *
	 * @return a String representation of the semantic version in the requested
	 *         format
	 * @since 4.0.0
	 */
	default String toString(boolean excludeBuild)
	{
		return toString(getDefaultFormat(), excludeBuild);
	}

	/**
	 * Returns a String representation of the semantic version in the requested
	 * format.
	 *
	 * @param format       the format to use for the generated String
	 *                     ({@code null} for {@link Format#CANONICAL})
	 * @param excludeBuild if true, don't include the build info in the result
	 *
	 * @return a String representation of the semantic version in the requested
	 *         format
	 * @since 4.0.0
	 */
	String toString(@Nullable Format format, boolean excludeBuild);
}
