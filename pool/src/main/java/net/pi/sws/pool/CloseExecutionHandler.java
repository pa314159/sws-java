
package net.pi.sws.pool;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;

class CloseExecutionHandler
implements RejectedExecutionHandler
{

	@Override
	public void rejectedExecution( Runnable r, ThreadPoolExecutor executor )
	{
	}
}
