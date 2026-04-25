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
import java.math.BigInteger;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNull;
import static java.util.Objects.requireNonNullElse;
import static java.util.stream.Collectors.joining;

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
	@SuppressWarnings("optional.field")
	private final Optional<PreRelease> preRelease;
	@SuppressWarnings("optional.field")
	private final Optional<Build> build;

	DefaultSemanticVersion(
		NumericIdentifier major, NumericIdentifier minor, NumericIdentifier patch,
		@Nullable PreRelease preRelease, @Nullable Build build)
	{
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = Optional.ofNullable(preRelease);
		this.build = Optional.ofNullable(build);
	}

	static SemanticVersion of(String semanticVersion)
	{
		final var matcher = DefaultSemanticVersion.getMatcher(semanticVersion);
		if (matcher.matches())
		{
			if (matcher.groupCount() >= 5)
			{
				final String mg1 = matcher.group(1);
				final String mg2 = matcher.group(2);
				final String mg3 = matcher.group(3);
				final String mg4 = matcher.group(4);
				final String mg5 = matcher.group(5);
				if (mg1 != null && mg2 != null && mg3 != null)
				{
					return new DefaultSemanticVersion(
						new NumericIdentifier(mg1),
						new NumericIdentifier(mg2),
						new NumericIdentifier(mg3),
						mg4 != null ? new PreRelease(mg4) : null,
						mg5 != null ? new Build(mg5) : null);
				}
				else
				{
					throw new IllegalStateException("Inconsistent regex check");
				}
			}
			else
			{
				throw new IllegalStateException("Inconsistent number of groups");
			}
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
				}).findFirst().orElseThrow(
				() -> new InvalidSemanticVersionException("<resource not found>"));
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
	public BigInteger getMajorValue()
	{
		return major.getNumberValue();
	}

	@Override
	public int getMinor()
	{
		return minor.getNumber();
	}

	@Override
	public BigInteger getMinorValue()
	{
		return minor.getNumberValue();
	}

	@Override
	public int getPatch()
	{
		return patch.getNumber();
	}

	@Override
	public BigInteger getPatchValue()
	{
		return patch.getNumberValue();
	}

	@Override
	public Optional<PreRelease> getPreRelease()
	{
		return preRelease;
	}

	@Override
	public Optional<Build> getBuild()
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
		Comparator.comparing(SemanticVersion::getMajorValue)
			.thenComparing(SemanticVersion::getMinorValue)
			.thenComparing(SemanticVersion::getPatchValue)
			.thenComparing((sv1, sv2) ->
			{
				final Optional<PreRelease> opt1 = sv1.getPreRelease();
				final Optional<PreRelease> opt2 = sv2.getPreRelease();
				if (opt1.isPresent())
				{
					return opt2.isPresent() ? opt1.get().compareTo(opt2.get()) : -1;
				}
				else
				{
					return opt2.isPresent() ? 1 : 0;
				}
			});

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
		if (preRelease.isPresent())
		{
			return build.isPresent() ?
				List.of(major, minor, patch, preRelease.get(), build.get()) :
				List.of(major, minor, patch, preRelease.get());
		}
		else
		{
			return build.isPresent() ?
				List.of(major, minor, patch, build.get()) :
				List.of(major, minor, patch);
		}
	}

	@Override
	public String getCanonicalForm()
	{
		return "%s.%s.%s%s%s".formatted(
			major.getPart(), minor.getPart(), patch.getPart(),
			preRelease.map(p -> "-" + p.getPart()).orElse(""),
			build.map(b -> "+" + b.getPart()).orElse(""));
	}

	@Override
	public String getDescription()
	{
		return "%s.%s.%s%s%s".formatted(
			major.getPart(), minor.getPart(), patch.getPart(),
			preRelease.map(p -> " pre-release »%s«".formatted(p.getPart())).orElse(""),
			build.map(b -> " build »%s«".formatted(b.getPart())).orElse(""));
	}

	@Override
	public String toString()
	{
		return "SemVer_{%s}".formatted(
			getVersionParts().stream().map(VersionPart::toString)
				.collect(joining("_")));
	}
}
