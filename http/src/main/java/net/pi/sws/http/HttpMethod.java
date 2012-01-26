
package net.pi.sws.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HttpMethod
{

	static final byte[]				CRLF		= { 0x0D, 0x0A };

	static final Charset			ISO_8859_1;

	static {
		ISO_8859_1 = Charset.forName( "ISO-8859-1" );
	}

	final Map<String, HttpHeader>	requestH	= new HashMap<String, HttpHeader>();

	private final List<HttpHeader>	responseH	= new ArrayList<HttpHeader>();

	private InputStream				is;

	private OutputStream			os;

	private final String			version;

	private HttpCode				status;

	private boolean					flushed;

	private final String			uri;

	protected HttpMethod( String uri, String version )
	{
		this.uri = uri;
		this.version = version;
	}

	public String getURI()
	{
		return this.uri;
	}

	private void flushHead() throws IOException
	{
		this.os.write( this.version.getBytes( ISO_8859_1 ) );
		this.os.write( ' ' );
		this.os.write( this.status.code );
		this.os.write( ' ' );
		this.os.write( this.status.text );
		this.os.write( CRLF );

		for( final HttpHeader h : this.responseH ) {
			this.os.write( h.name.getBytes( ISO_8859_1 ) );
			this.os.write( ':' );
			this.os.write( ' ' );
			this.os.write( h.content.getBytes( ISO_8859_1 ) );
			this.os.write( CRLF );
		}

		this.os.write( CRLF );
		this.os.flush();

		this.flushed = true;
	}

	protected final void addHeader( HttpHeader h ) throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "Header has been already flushed" );
		}

		this.responseH.add( h );
	}

	protected abstract void execute() throws IOException;

	final protected HttpHeader getHeader( String name )
	{
		return this.requestH.get( name );
	}

	protected InputStream getInputStream()
	{
		return this.is;
	}

	protected OutputStream getOutputStream() throws IOException
	{
		flushHead();

		return this.os;
	}

	protected final void setStatus( HttpCode code ) throws IOException
	{
		if( this.flushed ) {
			throw new IllegalStateException( "Header has been already flushed" );
		}

		this.status = code;
	}

	final void add( HttpHeader h )
	{
		this.requestH.put( h.name, h );
	}

	final void forward( InputStream is, OutputStream os ) throws IOException
	{
		this.is = is;
		this.os = os;

		setStatus( HttpCode.S_OK );
		addHeader( new HttpHeader( "Server", "SWS/0.1 Simple Web Server" ) );

		execute();

		if( !this.flushed ) {
			flushHead();
		}
	}
}
