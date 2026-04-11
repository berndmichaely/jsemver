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

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Class to describe dot separated version identifier parts.
 */
public class Identifier implements Comparable<Identifier>
{
	private static final int NO_NUMBER = -1;
	private final String part;
	private final boolean isNumeric;
	private final int number;

	Identifier(String part)
	{
		this.part = part;
		boolean _numeric;
		int _number;
		try
		{
			_number = Integer.parseInt(part);
			_numeric = true;
		}
		catch (NumberFormatException ex)
		{
			_number = NO_NUMBER;
			_numeric = false;
		}
		this.isNumeric = _numeric;
		this.number = _number;
	}

	/**
	 * Returns true, if the identifier is numeric.
	 *
	 * @return true, if the identifier is numeric
	 */
	public boolean isNumeric()
	{
		return isNumeric;
	}

	/**
	 * If the identifier is numeric, returns it as a number. Otherwise the return
	 * value is meaningless.
	 *
	 * @return the identifier as number, if it is numeric, otherwise a negative
	 *         number
	 * @see #isNumeric()
	 */
	public int getNumber()
	{
		return number;
	}

	@Override
	public int compareTo(Identifier other)
	{
		if (this.isNumeric && other.isNumeric)
		{
			return Integer.compare(this.number, other.number);
		}
		else if (!this.isNumeric && !other.isNumeric)
		{
			return this.part.compareTo(other.part);
		}
		else
		{
			return this.isNumeric ? -1 : 1;
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
	 * Returns the identifier as String.
	 *
	 * @return the identifier as String
	 */
	@Override
	public String toString()
	{
		return part;
	}
}
