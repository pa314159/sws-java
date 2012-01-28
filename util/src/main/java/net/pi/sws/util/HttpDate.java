
package net.pi.sws.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec3.html#sec3.3.1">HTTP date utility</a>
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public final class HttpDate
{

	static final String						FORMAT1	= "EEE, dd MMM yyyy HH:mm:ss 'GMT'";

	static final String						FORMAT2	= "EEEE, dd MMM yyyy HH:mm:ss 'GMT'";

	static final String						FORMAT3	= "EEE MMM d HH:mm:ss yyyy";

	static final ThreadLocal<DateFormat>	F1		= new ThreadLocal<DateFormat>()
													{

														@Override
														protected DateFormat initialValue()
														{
															return create( FORMAT1 );
														}
													};

	static final ThreadLocal<DateFormat>	F2		= new ThreadLocal<DateFormat>()
													{

														@Override
														protected DateFormat initialValue()
														{
															return create( FORMAT2 );
														}
													};

	static final ThreadLocal<DateFormat>	F3		= new ThreadLocal<DateFormat>()
													{

														@Override
														protected DateFormat initialValue()
														{
															return create( FORMAT3 );
														}
													};

	static public String format( Object o )
	{
		if( o instanceof Calendar ) {
			o = ((Calendar) o).getTime();
		}

		return F1.get().format( o );
	}

	static public Date parse( String source ) throws ParseException
	{
		try {
			return F1.get().parse( source );
		}
		catch( final ParseException e ) {
			;
		}
		try {
			return F2.get().parse( source );
		}
		catch( final ParseException e ) {
			;
		}

		return F3.get().parse( source );
	}

	static DateFormat create( String fmt )
	{
		final DateFormat df = new SimpleDateFormat( fmt, Locale.ENGLISH );

		df.setTimeZone( TimeZone.getTimeZone( "GMT" ) );

		return df;
	}

	private HttpDate()
	{
	}
}
