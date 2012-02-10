
package net.pi.sws.dav;

import java.io.IOException;

import net.pi.sws.http.HttpServiceFactory;
import net.pi.sws.pool.LifeCycle;
import net.pi.sws.util.ExtLog;

import com.bradmcevoy.http.AuthenticationService;
import com.bradmcevoy.http.HttpManager;
import com.bradmcevoy.http.ResourceFactory;

public class DavServiceFactory
extends HttpServiceFactory
{

	static private final ExtLog		L	= ExtLog.get();

	private final DavMethodFactory	mf	= new DavMethodFactory();

	HttpManager						hm;

	public DavServiceFactory()
	{
	}

	DavServiceFactory( HttpManager hm )
	{
		this.hm = hm;
	}

	@Override
	public DavMethodFactory getMethodFactory()
	{
		if( this.hm == null ) {
			throw new IllegalStateException();
		}

		return this.mf;
	}

	public void setManager( HttpManager hm )
	{
		this.hm = hm;
	}

	public void setResourceFactory( ResourceFactory rf )
	{
		final AuthenticationService auth = new AuthenticationService();

		auth.setDisableBasic( true );
		auth.setDisableDigest( true );

		this.hm = new HttpManager( rf, auth );
	}

	@Override
	public void start() throws IOException
	{
		L.info( "Starting DAV" );
	}

	@Override
	public void stop( long timeout ) throws InterruptedException, IOException
	{
		L.info( "Stopping DAV" );

		if( (this.hm != null) && (this.hm.getResourceFactory() instanceof LifeCycle) ) {
			((LifeCycle) this.hm.getResourceFactory()).stop( timeout );
		}
	}
}
