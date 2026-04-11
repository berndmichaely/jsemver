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
 * Exception indication an invalid argument to a SemanticVersion constructor.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 */
public class InvalidSemanticVersionException extends IllegalArgumentException
{
	/**
	 * Holds the invalid SemanticVersion constructor argument.
	 */
	private final String invalidArgument;

	static String formatMessage(String invalidArgument)
	{
		return "»%s« is not a valid Semantic Version %s String"
			.formatted(invalidArgument, SemanticVersion.getSupportedVersion());
	}

	/**
	 * Creates a new instance.
	 *
	 * @param invalidArgument the invalid SemanticVersion constructor argument
	 */
	InvalidSemanticVersionException(String invalidArgument)
	{
		super(formatMessage(invalidArgument));
		this.invalidArgument = invalidArgument;
	}

	/**
	 * Returns the invalid argument which caused the exception.
	 *
	 * @return the invalid argument
	 */
	public String getInvalidArgument()
	{
		return invalidArgument;
	}
}
