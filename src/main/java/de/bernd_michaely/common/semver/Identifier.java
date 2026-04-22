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

import java.util.EnumMap;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.checkerframework.checker.nullness.qual.Nullable;

import static java.util.Objects.requireNonNullElse;

/**
 * Class to describe dot separated version identifier parts.
 */
public final class Identifier implements Comparable<Identifier>
{
	private final String part;
	private final Optional<Integer> optionalNumber;

	/**
	 * Type of Identifier.
	 */
	public enum Type
	{
		/**
		 * Pre-Release Identifier.
		 */
		PRE_RELEASE,
		/**
		 * Build Identifier.
		 */
		BUILD
	}
	private static final EnumMap<Type, Pattern> mapPattern = new EnumMap<>(Type.class);

	Identifier(String part)
	{
		this.part = part;
		Optional<Integer> optional;
		try
		{
			final int num = Integer.parseInt(part);
			optional = Optional.of(num);
		}
		catch (NumberFormatException ex)
		{
			optional = Optional.empty();
		}
		this.optionalNumber = optional;
	}

	private static Matcher getMatcher(String identifier, Type type)
	{
		return mapPattern.computeIfAbsent(type, t -> Pattern.compile(switch (t)
		{
			case PRE_RELEASE ->
				SemanticVersion.SubRegEx.PRE_RELEASE_IDENTIFIER;
			case BUILD ->
				SemanticVersion.SubRegEx.BUILD_IDENTIFIER;
		})).matcher(requireNonNullElse(identifier, ""));
	}

	/**
	 * Creates a new instance of the given String.
	 *
	 * @param identifier a semantic version identifier String
	 * @param type       the identifier type
	 * @return a new instance
	 * @throws InvalidSemanticVersionException if the given identifier String is
	 *                                         invalid (including null)
	 * @since 2.0.0
	 */
	public static Identifier of(String identifier, Type type)
	{
		if (getMatcher(identifier, type).matches())
		{
			return new Identifier(identifier);
		}
		else
		{
			throw new InvalidSemanticVersionException(identifier);
		}
	}

	/**
	 * Returns the substring of this identifier.
	 *
	 * @return the identifier substring
	 */
	public String getPart()
	{
		return part;
	}

	/**
	 * Returns true, if the identifier is numeric.
	 *
	 * @return true, if the identifier is numeric
	 */
	public boolean isNumeric()
	{
		return optionalNumber.isPresent();
	}

	/**
	 * If the identifier is numeric, returns the number value, otherwise an empty
	 * optional.
	 *
	 * @return the number value, if numeric, otherwise an empty optional
	 * @see #isNumeric()
	 */
	public Optional<Integer> getOptionalNumber()
	{
		return optionalNumber;
	}

	@Override
	public int compareTo(Identifier other)
	{
		if (this.isNumeric() && other.isNumeric())
		{
			return Integer.compare(this.getOptionalNumber().get(), other.getOptionalNumber().get());
		}
		else if (!this.isNumeric() && !other.isNumeric())
		{
			return this.part.compareTo(other.part);
		}
		else
		{
			return this.isNumeric() ? -1 : 1;
		}
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof Identifier other)
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
		return part.hashCode();
	}

	/**
	 * Compares two identifiers ignoring case.
	 *
	 * @param other the other Identifier
	 * @return true, iff both identifiers are equal ignoring case
	 */
	public boolean equalsIgnoreCase(Identifier other)
	{
		return this.part.equalsIgnoreCase(other.part);
	}

	/**
	 * Compares two identifiers ignoring case.
	 *
	 * @param strId an identifier String
	 * @param type  the identifier type
	 * @return true, iff both identifiers are equal ignoring case
	 * @since 3.0.0
	 */
	public boolean equalsIgnoreCase(String strId, Type type)
	{
		return equalsIgnoreCase(Identifier.of(strId, type));
	}

	/**
	 * Returns the identifier as String.
	 *
	 * @return the identifier as String
	 */
	@Override
	public String toString()
	{
		return getPart();
	}
}
