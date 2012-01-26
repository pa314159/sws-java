
package net.pi.sws.util;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory
implements ThreadFactory
{

	private final String				format;

	private final boolean				daemon;

	private int							count;

	private int							priority	= Thread.NORM_PRIORITY;

	private UncaughtExceptionHandler	eh			= Thread.getDefaultUncaughtExceptionHandler();

	public NamedThreadFactory( String format, boolean daemon )
	{
		this.format = format;
		this.daemon = daemon;
	}

	@Override
	public Thread newThread( Runnable r )
	{
		final Thread thread = new Thread( r );

		thread.setName( String.format( this.format, ++this.count ) );
		thread.setDaemon( this.daemon );
		thread.setPriority( this.priority );
		thread.setUncaughtExceptionHandler( this.eh );

		return thread;
	}

	public void setPriority( int priority )
	{
		this.priority = priority;
	}

	public void setUncaughtExceptionHandler( UncaughtExceptionHandler eh )
	{
		this.eh = eh;
	}
}
