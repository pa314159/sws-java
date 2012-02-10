
package net.pi.sws.dav;

import java.net.HttpCookie;

import com.bradmcevoy.http.Cookie;

class MiltonCookie
implements Cookie
{

	final HttpCookie	cookie;

	MiltonCookie( Cookie cookie )
	{
		this.cookie = new HttpCookie( cookie.getName(), cookie.getValue() );

		setDomain( cookie.getDomain() );
		setExpiry( cookie.getExpiry() );
		setPath( cookie.getPath() );
		setSecure( cookie.getSecure() );
		setValue( cookie.getValue() );
		setVersion( cookie.getVersion() );
	}

	MiltonCookie( HttpCookie cookie )
	{
		this.cookie = cookie;
	}

	MiltonCookie( String name, String value )
	{
		this.cookie = new HttpCookie( name, value );

		this.cookie.setVersion( 0 );
		this.cookie.setPath( "/" );
	}

	@Override
	public String getDomain()
	{
		return this.cookie.getDomain();
	}

	@Override
	public int getExpiry()
	{
		return (int) this.cookie.getMaxAge();
	}

	@Override
	public String getName()
	{
		return this.cookie.getName();
	}

	@Override
	public String getPath()
	{
		return this.cookie.getPath();
	}

	@Override
	public boolean getSecure()
	{
		return this.cookie.getSecure();
	}

	@Override
	public String getValue()
	{
		return this.cookie.getValue();
	}

	@Override
	public int getVersion()
	{
		return this.cookie.getVersion();
	}

	@Override
	public void setDomain( String domain )
	{
		this.cookie.setDomain( domain );
	}

	@Override
	public void setExpiry( int expiry )
	{
		this.cookie.setMaxAge( expiry );
	}

	@Override
	public void setPath( String path )
	{
		this.cookie.setPath( path );
	}

	@Override
	public void setSecure( boolean secure )
	{
		this.cookie.setSecure( secure );
	}

	@Override
	public void setValue( String value )
	{
		this.cookie.setValue( value );
	}

	@Override
	public void setVersion( int version )
	{
		this.cookie.setVersion( version );
	}

}
