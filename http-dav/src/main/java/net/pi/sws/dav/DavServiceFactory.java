
package net.pi.sws.dav;

import com.bradmcevoy.http.HttpManager;
import net.pi.sws.http.HttpServiceFactory;

public class DavServiceFactory
extends HttpServiceFactory
{

	final HttpManager	hm;

	public DavServiceFactory( HttpManager hm )
	{
		this.hm = hm;
	}
}
