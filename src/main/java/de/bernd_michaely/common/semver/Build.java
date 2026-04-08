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
 * Class to handle a semantic version build.
 */
public final class Build extends DotSeparatedVersionPart
{
	Build()
	{
		super();
	}

	Build(String build)
	{
		super(build);
	}

	@Override
	public boolean equals(@Nullable Object object)
	{
		if (object instanceof Build other)
		{
			return this.getVersionPart().equals(other.getVersionPart());
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
