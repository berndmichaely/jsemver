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

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNullElse;

/**
 * Class to handle a semantic version build.
 */
public final class Build extends DotSeparatedVersionPart
{
	private static @MonotonicNonNull Pattern patternBuild;

	Build(String build)
	{
		super(build);
	}

	private static Matcher getMatcher(String build)
	{
		if (patternBuild == null)
		{
			patternBuild = Pattern.compile(SemanticVersion.SubRegEx.BUILD);
		}
		return patternBuild.matcher(requireNonNullElse(build, ""));
	}

	/**
	 * Creates a new instance of the given String.
	 *
	 * @param build a semantic version build String
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given build String is
	 *                                         invalid (including null)
	 * @since 2.0.0
	 */
	public static Build of(String build)
	{
		if (getMatcher(build).matches())
		{
			return new Build(build);
		}
		else
		{
			throw new InvalidSemanticVersionException(build);
		}
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof Build other)
		{
			return this.getPart().equals(other.getPart());
		}
		else
		{
			return false;
		}
	}

	@Override
	public int hashCode()
	{
		return getPart().hashCode();
	}
}
