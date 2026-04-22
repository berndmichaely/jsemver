/*
 * Copyright 2026 Bernd Michaely (info@bernd-michaely.de).
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;

/**
 * Default implementation of the SemanticVersion interface.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
final class DefaultSemanticVersion implements SemanticVersion
{
	static final String SUPPORTED_VERSION = "2.0.0";
	static final String RESOURCE_SEM_VER_LIB = "semantic_version.txt";
	static @MonotonicNonNull SemanticVersion VERSION_SEMVER;
	static @MonotonicNonNull SemanticVersion VERSION_LIB;
	private static @MonotonicNonNull Pattern patternSemVer;

	private final NumericIdentifier major;
	private final NumericIdentifier minor;
	private final NumericIdentifier patch;
	private final PreRelease preRelease;
	private final Build build;

	DefaultSemanticVersion(NumericIdentifier major, NumericIdentifier minor,
		NumericIdentifier patch, PreRelease preRelease, Build build)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preRelease != null ? preRelease : new PreRelease();
		this.build = build != null ? build : new Build();
	}

	static SemanticVersion of(String semanticVersion)
	{
		final var matcher = DefaultSemanticVersion.getMatcher(semanticVersion);
		if (matcher.matches())
		{
			return new DefaultSemanticVersion(
				new NumericIdentifier(matcher.group(1)),
				new NumericIdentifier(matcher.group(2)),
				new NumericIdentifier(matcher.group(3)),
				new PreRelease(matcher.group(4)),
				new Build(matcher.group(5)));
		}
		else
		{
			throw new InvalidSemanticVersionException(semanticVersion);
		}
	}

	static SemanticVersion of(InputStream inputStream) throws IOException
	{
		try (var reader = new BufferedReader(new InputStreamReader(requireNonNull(inputStream))))
		{
			return reader.lines().map(String::strip)
				.<SemanticVersion>mapMulti((line, consumer) ->
				{
					try
					{
						consumer.accept(SemanticVersion.of(line));
					}
					catch (InvalidSemanticVersionException ex)
					{
						// ignore invalid line
					}
				}).findFirst().orElseThrow();
		}
	}

	static SemanticVersion getSupportedVersion()
	{
		if (VERSION_SEMVER == null)
		{
			VERSION_SEMVER = SemanticVersion.of(SUPPORTED_VERSION);
		}
		return VERSION_SEMVER;
	}

	static SemanticVersion getLibVersion()
	{
		if (VERSION_LIB == null)
		{
			try (var inputStream = SemanticVersion.class.getResourceAsStream(RESOURCE_SEM_VER_LIB))
			{
				if (inputStream != null)
				{
					VERSION_LIB = SemanticVersion.of(inputStream);
				}
				else
				{
					throw new IllegalStateException("resource %s not found".formatted(RESOURCE_SEM_VER_LIB));
				}
			}
			catch (IOException ex)
			{
				throw new IllegalStateException(ex);
			}
		}
		return VERSION_LIB;
	}

	@Override
	public int getMajor()
	{
		return major.getNumber();
	}

	@Override
	public int getMinor()
	{
		return minor.getNumber();
	}

	@Override
	public int getPatch()
	{
		return patch.getNumber();
	}

	@Override
	public PreRelease getPreRelease()
	{
		return preRelease;
	}

	@Override
	public Build getBuild()
	{
		return build;
	}

	static Matcher getMatcher(String semanticVersion)
	{
		if (patternSemVer == null)
		{
			patternSemVer = Pattern.compile(SemanticVersion.SubRegEx.FULL_SEMANTIC_VERSION);
		}
		return patternSemVer.matcher(requireNonNullElse(semanticVersion, ""));
	}

	private static final Comparator<SemanticVersion> semanticVersionComparator =
		Comparator.comparingInt(SemanticVersion::getMajor)
			.thenComparingInt(SemanticVersion::getMinor)
			.thenComparingInt(SemanticVersion::getPatch)
			.thenComparing(SemanticVersion::getPreRelease);

	static final Comparator<SemanticVersion> getComparator()
	{
		return semanticVersionComparator;
	}

	@Override
	public int compareTo(SemanticVersion other)
	{
		return getComparator().compare(this, other);
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

	@Override
	public List<VersionPart> getVersionParts()
	{
		return List.of(major, minor, patch, preRelease, build);
	}

	@Override
	public String getDescription()
	{
		return "%d.%d.%d%s%s".formatted(getMajor(), getMinor(), getPatch(),
			getPreRelease().isPresent() ? " pre-release »" + getPreRelease() + "«" : "",
			getBuild().isPresent() ? " build »" + getBuild() + "«" : "");
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
		return "%d.%d.%d%s%s".formatted(getMajor(), getMinor(), getPatch(),
			getPreRelease().isPresent() ? "-" + getPreRelease() : "",
			getBuild().isPresent() ? "+" + getBuild() : "");
	}
}
