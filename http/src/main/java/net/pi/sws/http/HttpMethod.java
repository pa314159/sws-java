
package net.pi.sws.http;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class HttpMethod
{

	static final byte[]				CRLF		= { 0x0D, 0x0A };

	static final byte[]				COLON		= { 0x3A, 0x20 };

	static final byte[]				SPACE		= { 0x20 };

	static final Charset			ISO_8859_1;

	static {
		ISO_8859_1 = Charset.forName( "ISO-8859-1" );
	}

	final Map<String, HttpHeader>	requestH	= new HashMap<String, HttpHeader>();

	private final List<HttpHeader>	responseH	= new ArrayList<HttpHeader>();

	private ReadableByteChannel		ic;

	private InputStream				is;

	private boolean					gotI;

	private WritableByteChannel		oc;

	private OutputStream			os;

	private boolean					gotO;

	private final String			version;

	private HttpCode				status;

	private boolean					flushed;

	private final String			uri;

	protected final File			root;

	protected HttpMethod( File root, String uri, String version ) throws IOException
	{
		this.root = root.getCanonicalFile();
		this.uri = uri;
		this.version = version;
	}

	public String getURI()
	{
		return this.uri;
	}

	private void flushHead() throws IOException
	{
		final BufferedOutputStream os = new BufferedOutputStream( new ChannelOutputStream( this.oc ) );

		os.write( this.version.getBytes( ISO_8859_1 ) );
		os.write( SPACE );
		os.write( this.status.code );
		os.write( SPACE );
		os.write( this.status.text );
		os.write( CRLF );

		for( final HttpHeader h : this.responseH ) {
			os.write( h.name.getBytes( ISO_8859_1 ) );
			os.write( COLON );
			os.write( h.content.getBytes( ISO_8859_1 ) );
			os.write( CRLF );
		}

		os.write( CRLF );
		os.flush();

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

	protected ReadableByteChannel getInputChannel() throws IOException
	{
		if( this.is != null ) {
			throw new IllegalStateException( "Already called getInputStream() " );
		}

		this.gotI = true;

		return this.ic;
	}

	protected InputStream getInputStream() throws IOException
	{
		if( this.gotI ) {
			throw new IllegalStateException( "Already called getInputChannel() " );
		}

		if( this.is != null ) {
			return this.is;
		}

		return this.is = new ChannelInputStream( this.ic );
	}

	protected WritableByteChannel getOutputChannel() throws IOException
	{
		if( this.os != null ) {
			throw new IllegalStateException( "Already called getOutputStream() " );
		}

		this.gotO = true;

		flushHead();

		return this.oc;
	}

	protected OutputStream getOutputStream() throws IOException
	{
		if( this.gotO ) {
			throw new IllegalStateException( "Already called getOutputChannel() " );
		}

		flushHead();

		if( this.os != null ) {
			return this.os;
		}

		return this.os = new ChannelOutputStream( this.oc );
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

	final void forward( ReadableByteChannel ic, WritableByteChannel oc ) throws IOException
	{
		this.ic = ic;
		this.oc = oc;

		setStatus( HttpCode.S_OK );
		addHeader( new HttpHeader( "Server", "SWS/0.1 Simple Web Server" ) );

		execute();

		if( !this.flushed ) {
			flushHead();
		}
	}
}
