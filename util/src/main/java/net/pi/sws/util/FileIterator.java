
package net.pi.sws.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

class FileIterator
implements Iterator<File>, FileFilter
{

	final List<File>	stack	= new ArrayList<File>();

	final String[]		exts;

	FileIterator( File root, String... exts )
	{
		this.stack.add( root );
		this.exts = exts;
	}

	public boolean accept( File file )
	{
		if( file.isDirectory() ) {
			return true;
		}

		if( this.exts.length == 0 ) {
			return true;
		}

		final String name = file.getName();

		for( final String x : this.exts ) {
			if( name.endsWith( x ) ) {
				return true;
			}
		}

		return false;
	}

	public boolean hasNext()
	{
		return this.stack.size() > 0;
	}

	public File next()
	{
		final int z = this.stack.size();

		if( z < 1 ) {
			throw new NoSuchElementException();
		}

		final File result = this.stack.remove( z - 1 );

		if( result.isDirectory() ) {
			final File[] files = result.listFiles( this );

			for( final File f : files ) {
				this.stack.add( f );
			}
		}

		return result;
	}

	public void remove()
	{
		throw new UnsupportedOperationException();
	}
}
