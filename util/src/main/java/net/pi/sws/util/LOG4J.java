
package net.pi.sws.util;

import org.apache.log4j.Logger;

final class LOG4J
extends ALOG
{

	static final org.apache.log4j.Level[]	LEVELS	= new org.apache.log4j.Level[] {
													org.apache.log4j.Level.ALL,
													org.apache.log4j.Level.TRACE,
													org.apache.log4j.Level.DEBUG,
													org.apache.log4j.Level.INFO,
													org.apache.log4j.Level.WARN,
													org.apache.log4j.Level.ERROR,
													org.apache.log4j.Level.FATAL,
													org.apache.log4j.Level.OFF
													};

	static ALOG G( String category )
	{
		return new LOG4J( category );
	}

	/**  */
	final Logger	delegate;

	/**
	 * Creates a new SmLog object.
	 * 
	 * @param category
	 */
	LOG4J( Class category )
	{
		this.delegate = Logger.getLogger( category );
	}

	/**
	 * Creates a new SmLog object.
	 * 
	 * @param category
	 */
	LOG4J( String category )
	{
		this.delegate = Logger.getLogger( category );
	}

	@Override
	boolean E( ExtLog.Level level )
	{
		return this.delegate.isEnabledFor( LEVELS[level.ordinal()] );
	}

	@Override
	void L( String source, ExtLog.Level level, String text, Throwable throwable )
	{
		final StackTraceElement[] stack = new Throwable().getStackTrace();
		String actualSource = null;

		for( final StackTraceElement e : stack ) {
			if( source.equals( e.getClassName() ) ) {
				break;
			}

			actualSource = e.getClassName();
		}

		this.delegate.log( actualSource, LEVELS[level.ordinal()], text, throwable );
	}

	@Override
	void S( ExtLog.Level level )
	{
		this.delegate.setLevel( LEVELS[level.ordinal()] );
	}

}
