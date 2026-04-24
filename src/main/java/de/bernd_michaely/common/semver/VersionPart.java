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

/**
 * Class to describe the main parts of a SemanticVersion.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @since 3.0.0
 */
public abstract sealed class VersionPart permits NumericIdentifier, DotSeparatedVersionPart
{
	private final String part;

	VersionPart(String versionPart)
	{
		this.part = versionPart;
	}

	/**
	 * Returns the substring of this version part.
	 *
	 * @return the version part substring
	 */
	public final String getPart()
	{
		return part;
	}

	/**
	 * Returns the original constructor parameter.
	 *
	 * @return the original constructor parameter
	 */
	@Override
	public String toString()
	{
		return getPart();
	}
}
