
package net.pi.sws.http;

import java.io.IOException;

/**
 * Base of all HTTP methods.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public abstract class HttpMethod
{

	private final HttpRequest	request;

	private final HttpResponse	response;

	protected HttpMethod( HttpRequest request, HttpResponse response )
	{
		this.request = request;
		this.response = response;
	}

	protected HttpRequest getRequest()
	{
		return this.request;
	}

	protected HttpResponse getResponse()
	{
		return this.response;
	}

	protected abstract void respond() throws IOException;

	void flush() throws IOException
	{
		this.response.flush();
	}

	//	final boolean forward( ReadableByteChannel ic, WritableByteChannel oc ) throws IOException
	//	{
	//		this.ic = ic;
	//		this.oc = oc;
	//
	//		while( true ) {
	//			String head = IO.readLINE( ic );
	//
	//			if( head == null ) {
	//				throw new IOException(); // say something here
	//			}
	//
	//			head = head.trim();
	//
	//			if( head.isEmpty() ) {
	//				break;
	//			}
	//
	//			final HttpHeader h = new HttpHeader( head );
	//
	//			L.trace( "I -> %s", h );
	//
	//			this.requestH.put( h.name.toLowerCase(), h );
	//
	//			if( isKeepAlive() ) {
	//				// respond immediately
	//				switch( this.specification ) {
	//					case RFC_2068:
	//					break;
	//
	//					case RFC_2616:
	//						setStatus( HttpCode.CONTINUE );
	//					break;
	//				}
	//			}
	//		}
	//
	//		if( this.status == null ) {
	//			setStatus( HttpCode.OK );
	//		}
	//
	//		addResponseHeader( new HttpHeader( HttpHeader.Response.SERVER, SIGNATURE ) );
	//
	//		respond();
	//
	//		if( !this.flushed ) {
	//			flushHead();
	//		}
	//
	//		if( this.os != null ) {
	//			this.os.flush();
	//		}
	//
	//		if( isRequestHeader( HttpHeader.General.CONNECTION, "close" ) ) {
	//			return false;
	//		}
	//		else {
	//			return isKeepAlive();
	//		}
	//	}
}
