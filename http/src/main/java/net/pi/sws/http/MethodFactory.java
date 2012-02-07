
package net.pi.sws.http;

import java.io.IOException;

public interface MethodFactory<F extends HttpServiceFactory>
{

	HttpMethod<F> get( String met, F fact, HttpRequest request, HttpResponse response ) throws IOException;

}
