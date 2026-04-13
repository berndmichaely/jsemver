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

import java.util.function.Function;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Exception indication an invalid argument to a SemanticVersion constructor.
 *
 * @author Bernd Michaely (info@bernd-michaely.de)
 * @since 1.0.1
 */
public final class InvalidSemanticVersionException extends IllegalArgumentException
{
	static final String DEFAULT_MSG_FORMAT =
		"»%s« is not a valid Semantic Version " + SemanticVersion.getSupportedVersion() + " String";
	/**
	 * Holds the invalid SemanticVersion constructor argument.
	 */
	private final String invalidArgument;
	/**
	 * Function from an invalid semanticVersion argument String to a localized
	 * InvalidSemanticVersionException message.
	 */
	private final @Nullable Function<String, String> exceptionMsgFormatter;

	/**
	 * Creates a new instance.
	 *
	 * @param invalidArgument       the invalid SemanticVersion constructor
	 *                              argument
	 * @param exceptionMsgFormatter function from an invalid semanticVersion
	 *                              argument String to a localized
	 *                              InvalidSemanticVersionException message
	 */
	InvalidSemanticVersionException(String invalidArgument,
		@Nullable Function<String, String> exceptionMsgFormatter)
	{
		super(defaultFormat(invalidArgument));
		this.invalidArgument = invalidArgument;
		this.exceptionMsgFormatter = exceptionMsgFormatter;
	}

	private static String defaultFormat(String arg)
	{
		return DEFAULT_MSG_FORMAT.formatted(arg);
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

	@Override
	public String getLocalizedMessage()
	{
		return (exceptionMsgFormatter != null) ?
			exceptionMsgFormatter.apply(invalidArgument) :
			defaultFormat(invalidArgument);
	}
}
