
package net.pi.sws.http;

import java.util.regex.Pattern;

/**
 * Supported compression types.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public enum CompressionType
{
	gzip,
	deflate,

	// compress,
	;

	final Pattern	pattern;

	private CompressionType()
	{
		this.pattern = Pattern.compile( name(), Pattern.CASE_INSENSITIVE );
	}
}
