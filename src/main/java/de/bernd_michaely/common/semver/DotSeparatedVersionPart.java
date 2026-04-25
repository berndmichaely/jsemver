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

import java.util.Arrays;
import java.util.List;
import org.checkerframework.checker.nullness.qual.MonotonicNonNull;

import static java.util.stream.Collectors.joining;

/**
 * Base class to handle a dot separated semantic version part.
 */
public abstract sealed class DotSeparatedVersionPart extends VersionPart
	permits PreRelease, Build
{
	private static final String DELIMITER = "\\.";
	private static final Identifier[] NULL = new Identifier[0];
	private Identifier[] identifiers = NULL;
	private @MonotonicNonNull List<Identifier> listIdentifiers;

	DotSeparatedVersionPart(String versionPart)
	{
		super(versionPart);
	}

	private Identifier[] identifiers()
	{
		if (identifiers == NULL)
		{
			identifiers = Arrays.stream(getPart().split(DELIMITER))
				.map(Identifier::new).toArray(Identifier[]::new);
		}
		return identifiers;
	}

	/**
	 * Returns an unmodifiable list of the identifiers of this version part
	 * (PreRelease or Build).
	 *
	 * @return an unmodifiable list of identifiers. For an empty version part, an
	 *         empty list is returned.
	 * @since 1.0.1
	 */
	public List<Identifier> getIdentifiers()
	{
		if (listIdentifiers == null)
		{
			listIdentifiers = List.of(identifiers());
		}
		return listIdentifiers;
	}

	int compareTo(DotSeparatedVersionPart other)
	{
		return Arrays.compare(this.identifiers(), other.identifiers());
	}

	@Override
	public String toString()
	{
		return "%s[%s]".formatted(getClass().getSimpleName(),
			getIdentifiers().stream().map(Identifier::toString).collect(joining("/")));
	}
}
