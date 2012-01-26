
package net.pi.sws.util;

import java.util.logging.Logger;

final class JDK14
extends ALOG
{

	static final java.util.logging.Level[]	LEVELS	= new java.util.logging.Level[] {
													java.util.logging.Level.ALL,
													java.util.logging.Level.FINEST,
													java.util.logging.Level.FINE,
													java.util.logging.Level.INFO,
													java.util.logging.Level.WARNING,
													java.util.logging.Level.SEVERE,
													java.util.logging.Level.SEVERE,
													java.util.logging.Level.OFF };

	static ALOG G( String category )
	{
		return new JDK14( category );
	}

	final Logger	delegate;

	JDK14( Class category )
	{
		this.delegate = Logger.getLogger( category.getName() );
	}

	JDK14( String category )
	{
		this.delegate = Logger.getLogger( category );
	}

	@Override
	boolean E( ExtLog.Level level )
	{
		return this.delegate.isLoggable( LEVELS[level.ordinal()] );
	}

	@Override
	void L( String target, ExtLog.Level level, String text, Throwable throwable )
	{
		final Throwable tt = new Throwable();
		final StackTraceElement st[] = tt.getStackTrace();

		String cn = "unknown";
		String mn = "unknown";

		for( final StackTraceElement e : st ) {
			if( e.getClassName().equals( target ) ) {
				cn = e.getClassName();
				mn = e.getMethodName();
				break;
			}
		}

		if( throwable == null ) {
			this.delegate.logp( LEVELS[level.ordinal()], cn, mn, text );
		}
		else {
			this.delegate.logp( LEVELS[level.ordinal()], cn, mn, text, throwable );
		}
	}

	@Override
	void S( ExtLog.Level level )
	{
		this.delegate.setLevel( LEVELS[level.ordinal()] );
	}

}
