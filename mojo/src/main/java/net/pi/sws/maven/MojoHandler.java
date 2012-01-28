
package net.pi.sws.maven;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import org.apache.maven.plugin.logging.Log;

class MojoHandler
extends Handler
{

	private final Log	log;

	MojoHandler( Log log )
	{
		this.log = log;
	}

	@Override
	public void close() throws SecurityException
	{
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void publish( LogRecord record )
	{
		final int level = record.getLevel().intValue();
		final String text = String.format( "[%s] %s", Thread.currentThread().getName(), record.getMessage() );

		if( level < Level.INFO.intValue() ) {
			this.log.debug( text, record.getThrown() );
		}
		else if( level == Level.INFO.intValue() ) {
			this.log.info( text, record.getThrown() );
		}
		else if( level == Level.WARNING.intValue() ) {
			this.log.warn( text, record.getThrown() );
		}
		else if( level > Level.WARNING.intValue() ) {
			this.log.error( text, record.getThrown() );
		}
	}
}
