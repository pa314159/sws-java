
package net.pi.sws.dav;

import java.io.FileNotFoundException;
import java.io.IOException;

import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.exceptions.BadRequestException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.http.exceptions.NotFoundException;

import net.pi.sws.http.HttpMethod;
import net.pi.sws.http.HttpRequest;
import net.pi.sws.http.HttpResponse;

class DavMethod
extends HttpMethod<DavServiceFactory>
{

	final String	name;

	DavMethod( String method, DavServiceFactory fact, HttpRequest request, HttpResponse response )
	{
		super( fact, request, response );

		this.name = method;
	}

	@Override
	protected void respond()
	throws IOException
	{
		final DavRequest davRequest = new DavRequest( this );
		final DavResponse davResponse = new DavResponse( this );

		if( "GET".equals( this.name ) ) {
			final Resource res = this.fact.hm.getResourceFactory().getResource( null, davRequest.getAbsolutePath() );

			if( res == null ) {
				davResponse.setStatus( Status.SC_NOT_FOUND );
			}
			else if( res instanceof GetableResource ) {
				try {
					final GetableResource getable = (GetableResource) res;

					davResponse.setContentTypeHeader( getable.getContentType( null ) );

					getable.sendContent( davResponse.getOutputStream(), null, null, null );
				}
				catch( final NotAuthorizedException e ) {
					davResponse.setStatus( Status.SC_UNAUTHORIZED );
				}
				catch( final BadRequestException e ) {
					davResponse.setStatus( Status.SC_BAD_REQUEST );
				}
				catch( final NotFoundException e ) {
					davResponse.setStatus( Status.SC_NOT_FOUND );
				}
				catch( final FileNotFoundException e ) {
					davResponse.setStatus( Status.SC_NOT_FOUND );
				}
			}
		}
		else {
			this.fact.hm.process( davRequest, davResponse );
		}
	}
}
