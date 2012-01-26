
package net.pi.sws.util;

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class ValidReference<T>
{

	public interface Check<T>
	{

		boolean isValid( T value );
	}

	public enum Type
	{
		NULL,
		WEAK,
		SOFT
	}

	static class NullCheck<T>
	implements Check<T>
	{

		public boolean isValid( T value )
		{
			return value != null;
		}
	}

	private final Type		type;

	private final Lock		lock;

	private final Condition	cond;

	private final Check<T>	check;

	private volatile Object	value;

	public ValidReference()
	{
		this( Type.NULL );
	}

	public ValidReference( ValidReference.Type type )
	{
		this( type, new NullCheck<T>(), null );
	}

	public ValidReference( ValidReference.Type type, ValidReference.Check<T> check )
	{
		this( type, check, null );
	}

	public ValidReference( ValidReference.Type type, ValidReference.Check<T> check, T value )
	{
		this.type = type;
		this.lock = new ReentrantLock();
		this.cond = this.lock.newCondition();
		this.check = check;

		set( value );
	}

	public T get() throws InterruptedException
	{
		this.lock.lock();

		try {
			while( true ) {
				final T v = getAsIs();

				if( this.check.isValid( v ) ) {
					return v;
				}

				this.cond.await();
			}
		}
		finally {
			this.lock.unlock();
		}
	}

	public T get( Callable<T> create ) throws Exception
	{
		this.lock.lock();

		try {
			T v = getAsIs();

			if( v == null ) {
				v = create.call();
			}

			setValue( v );

			return v;
		}
		finally {
			this.lock.unlock();
		}
	}

	public T get( long time, TimeUnit unit ) throws InterruptedException
	{
		this.lock.lock();

		try {
			while( true ) {
				final T v = getAsIs();

				if( this.check.isValid( v ) ) {
					return v;
				}

				this.cond.await( time, unit );
			}
		}
		finally {
			this.lock.unlock();
		}
	}

	public T getAsIs()
	{
		switch( this.type ) {
			case NULL:
				return (T) this.value;

			default:
				final Reference<T> ref = (Reference<T>) this.value;

				return ref != null ? ref.get() : null;
		}
	}

	public void lock()
	{
		this.lock.lock();
	}

	public void lockInterruptibly() throws InterruptedException
	{
		this.lock.lockInterruptibly();
	}

	public void set( T value )
	{
		this.lock.lock();

		try {
			setValue( value );
		}
		finally {
			this.lock.unlock();
		}
	}

	public boolean tryLock()
	{
		return this.lock.tryLock();
	}

	public boolean tryLock( long time, TimeUnit unit ) throws InterruptedException
	{
		return this.lock.tryLock( time, unit );
	}

	public void unlock()
	{
		this.lock.unlock();
	}

	private void setValue( T value )
	{
		try {
			switch( this.type ) {
				case NULL:
					this.value = value;
				break;

				case SOFT:
					this.value = new SoftReference<T>( value );
				break;

				case WEAK:
					this.value = new WeakReference<T>( value );
				break;
			}
		}
		finally {
			this.cond.signalAll();
		}
	}
}
