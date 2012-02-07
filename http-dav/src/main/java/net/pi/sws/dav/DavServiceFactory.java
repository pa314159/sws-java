
package net.pi.sws.dav;

import net.pi.sws.http.HttpServiceFactory;
import com.bradmcevoy.http.HttpManager;

public class DavServiceFactory
extends HttpServiceFactory
{

	private final DavMethodFactory	mf	= new DavMethodFactory();

	final HttpManager				hm;

	public DavServiceFactory( HttpManager hm )
	{
		this.hm = hm;
	}

	@Override
	public DavMethodFactory getMethodFactory()
	{
		return this.mf;
	}
}
