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

import java.math.BigInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNullElse;

/**
 * Interface to describe a numeric identifier.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @since 3.0.0
 */
public final class NumericIdentifier extends VersionPart implements Comparable<NumericIdentifier>
{
	static final int NUM_OVERFLOW = -1;
	private static @MonotonicNonNull Pattern patternNumericIdentifier;
	private final BigInteger number;

	NumericIdentifier(String versionPart)
	{
		super(versionPart);
		try
		{
			number = new BigInteger(versionPart);
		}
		catch (NumberFormatException ex)
		{
			throw new IllegalStateException(ex);
		}
	}

	private static Matcher getMatcher(String preRelease)
	{
		if (patternNumericIdentifier == null)
		{
			patternNumericIdentifier = Pattern.compile(SemanticVersion.SubRegEx.NUMERIC_IDENTIFIER);
		}
		return patternNumericIdentifier.matcher(requireNonNullElse(preRelease, ""));
	}

	/**
	 * Creates a new instance of the given String.
	 *
	 * @param numericString a semantic version numeric String
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given numeric String is
	 *                                         invalid (including null)
	 */
	public static NumericIdentifier of(String numericString)
	{
		if (getMatcher(numericString).matches())
		{
			return new NumericIdentifier(numericString);
		}
		else
		{
			throw new InvalidSemanticVersionException(numericString);
		}
	}

	/**
	 * Returns the numeric value of this identifier. If the correct value is
	 * greater than {@code 2^31-1}, returns a negative value.
	 *
	 * @return the numeric value
	 * @see #getNumberValue()
	 */
	int getNumber()
	{
		try
		{
			return number.intValueExact();
		}
		catch (ArithmeticException ex)
		{
			return NUM_OVERFLOW;
		}
	}

	/**
	 * Returns the numeric value of this identifier.
	 *
	 * @return the numeric value
	 * @see #getNumber()
	 * @since 3.0.0
	 */
	BigInteger getNumberValue()
	{
		return number;
	}

	@Override
	public int compareTo(NumericIdentifier other)
	{
		return this.getNumberValue().compareTo(other.getNumberValue());
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof NumericIdentifier other)
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
		return getNumberValue().hashCode();
	}

	@Override
	public String toString()
	{
		return "NumId(%s)".formatted(getPart());
	}
}
