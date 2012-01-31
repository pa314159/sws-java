
package net.pi.sws.util;

/**
 * Yet another logging wrapper, supporting formatted logging in the <code>printf</code> style. Currently supports only
 * JDK logging and LOG4J.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public final class ExtLog
{

	public static enum Level
	{
		ALL,
		TRACE,
		DEBUG,
		INFO,
		WARN,
		ERROR,
		FATAL,
		OFF,
	}

	public static enum System
	{
		JDK14,
		LOG4J,
	}

	static private final String	callerFQCN	= ExtLog.class.getName();

	private static final Class	CLASS		= ExtLog.class;

	private static final System	logSystem	= inferLogSystem();

	static public final ExtLog get()
	{
		return get( new Throwable().getStackTrace()[1].getClassName() );
	}

	static public final ExtLog get( final Class cat )
	{
		return get( cat.getName() );
	}

	static public final ExtLog get( final String cat )
	{
		switch( logSystem ) {
			case LOG4J:
				return new ExtLog( LOG4J.G( cat ) );
			case JDK14:
				return new ExtLog( JDK14.G( cat ) );
		}

		throw new AssertionError();
	}

	static System inferLogSystem()
	{
		try {
			CLASS.getClassLoader().loadClass( "org.apache.log4j.Logger" );

			return System.LOG4J;
		}
		catch( final Throwable t ) {
		}

		return System.JDK14;
	}

	final ALOG	delegate;

	ExtLog( final ALOG delegate )
	{
		this.delegate = delegate;
	}

	public void debug( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.DEBUG ) ) {
			delegate( null, Level.DEBUG, format, objects, null );
		}
	}

	public void debug( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.DEBUG ) ) {
			delegate( null, Level.DEBUG, format, objects, throwable );
		}
	}

	public void error( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.ERROR ) ) {
			delegate( null, Level.ERROR, format, objects, null );
		}
	}

	public void error( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.ERROR ) ) {
			delegate( null, Level.ERROR, format, objects, throwable );
		}
	}

	public void fatal( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.FATAL ) ) {
			delegate( null, Level.FATAL, format, objects, null );
		}
	}

	public void fatal( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.FATAL ) ) {
			delegate( null, Level.FATAL, format, objects, throwable );
		}
	}

	public void info( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.INFO ) ) {
			delegate( null, Level.INFO, format, objects, null );
		}
	}

	public void info( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.INFO ) ) {
			delegate( null, Level.INFO, format, objects, throwable );
		}
	}

	public boolean isEnabled( final Level level )
	{
		return this.delegate.E( level );
	}

	public final void log( Class source, Level level, String format, Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( source.getName(), level, format, objects, null );
		}
	}

	public final void log( Class source, Level level, String format, Throwable throwable, Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( source.getName(), level, format, objects, throwable );
		}
	}

	public final void log( Level level, String format, Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( null, level, format, objects, null );
		}
	}

	public final void log( Level level, String format, Throwable throwable, Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( null, level, format, objects, throwable );
		}
	}

	public final void log( String className, Level level, String format, Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( className, level, format, objects, null );
		}
	}

	public void log( final String className, final Level level, final String format, final Throwable throwable,
		final Object... objects )
	{
		if( this.delegate.E( level ) ) {
			delegate( className, level, format, objects, throwable );
		}
	}

	public void setLevel( Level level )
	{
		this.delegate.S( level );
	}

	public void trace( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.TRACE ) ) {
			delegate( null, Level.TRACE, format, objects, null );
		}
	}

	public void trace( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.TRACE ) ) {
			delegate( null, Level.TRACE, format, objects, throwable );
		}
	}

	public void warn( final String format, final Object... objects )
	{
		if( this.delegate.E( Level.WARN ) ) {
			delegate( null, Level.WARN, format, objects, null );
		}
	}

	public void warn( final String format, final Throwable throwable, final Object... objects )
	{
		if( this.delegate.E( Level.WARN ) ) {
			delegate( null, Level.WARN, format, objects, throwable );
		}
	}

	void delegate( String source, final Level level, final String format, final Object[] objects,
		final Throwable throwable )
	{
		if( source == null ) {
			source = new Throwable().getStackTrace()[2].getClassName();
		}

		try {
			this.delegate.L( source, level, String.format( format, objects ), throwable );
		}
		catch( final Throwable x ) {
			x.printStackTrace();

			java.lang.System.err.printf( "cannot log %s/%s: %s\n", format, throwable, x );
		}
	}
}
