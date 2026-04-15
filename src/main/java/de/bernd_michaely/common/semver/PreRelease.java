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

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNullElse;

/**
 * Class to handle a semantic version pre-release.
 */
public final class PreRelease extends DotSeparatedVersionPart implements Comparable<PreRelease>
{
	private static @MonotonicNonNull Pattern patternPreRelease;

	PreRelease()
	{
		super();
	}

	PreRelease(@Nullable String preRelease)
	{
		super(preRelease);
	}

	private static Matcher getMatcher(String preRelease)
	{
		if (patternPreRelease == null)
		{
			patternPreRelease = Pattern.compile(
				SemanticVersion.SubRegEx.STR_REGEX_PRE_RELEASE.toString());
		}
		return patternPreRelease.matcher(requireNonNullElse(preRelease, ""));
	}

	/**
	 * Creates a new instance of the given String.
	 *
	 * @param preRelease a semantic version pre-release String
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given preRelease String is
	 *                                         invalid (including null)
	 * @since 2.0.0
	 */
	public static PreRelease of(String preRelease)
	{
		return of(preRelease, null);
	}

	/**
	 * Creates a new instance of the given String.
	 *
	 * @param preRelease            a semantic version pre-release String
	 * @param exceptionMsgFormatter function from an invalid preRelease argument
	 *                              String to a localized
	 *                              InvalidSemanticVersionException message. Can
	 *                              be {@code null} to use the default formatting
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given preRelease String is
	 *                                         invalid (including null)
	 * @since 2.0.0
	 */
	public static PreRelease of(String preRelease,
		@Nullable Function<String, String> exceptionMsgFormatter)
	{
		if (getMatcher(preRelease).matches())
		{
			return new PreRelease(preRelease);
		}
		else
		{
			throw new InvalidSemanticVersionException(preRelease, exceptionMsgFormatter);
		}
	}

	@Override
	public int compareTo(PreRelease other)
	{
		return super.compareTo(other);
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof PreRelease other)
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
		return getVersionPart().hashCode();
	}
}
