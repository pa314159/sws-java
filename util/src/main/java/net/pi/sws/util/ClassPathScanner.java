
package net.pi.sws.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

import org.cojen.classfile.Attribute;
import org.cojen.classfile.ClassFile;
import org.cojen.classfile.TypeDesc;
import org.cojen.classfile.attribute.Annotation;
import org.cojen.classfile.attribute.AnnotationsAttr;

/**
 * Scans the ClassLoader and the class path.
 * 
 * @author PAPPY <a href="mailto:pa314159&#64;gmail.com">&lt;pa314159&#64;gmail.com&gt;</a>
 */
public class ClassPathScanner
{

	public interface Visitor
	{

		void visitClass( ClassLoader cld, String clsName ) throws IOException;
	}

	private final List<Pattern>		includes	= new ArrayList<Pattern>();

	private final List<Pattern>		excludes	= new ArrayList<Pattern>();

	private final List<TypeDesc>	annotations	= new ArrayList<TypeDesc>();

	private final List<String>		packages	= new ArrayList<String>();

	private final ClassLoader		cld;

	private final Visitor			vis;

	public ClassPathScanner( Visitor vis )
	{
		this( vis, Thread.currentThread().getContextClassLoader() );
	}

	public ClassPathScanner( Visitor vis, ClassLoader cld )
	{
		this.vis = vis;
		this.cld = cld;
	}

	public void addAnnotation( Class<? extends java.lang.annotation.Annotation> cls )
	{
		this.annotations.add( TypeDesc.forClass( cls ) );
	}

	public void addExclude( String regex )
	{
		this.excludes.add( Pattern.compile( regex ) );
	}

	public void addInclude( String regex )
	{
		this.includes.add( Pattern.compile( regex ) );
	}

	public void addPackage( String packageName )
	{
		this.packages.add( packageName );
	}

	public void scan() throws IOException
	{
		final Set<URL> classPath = new HashSet<URL>();

		for( ClassLoader cld = this.cld; cld != null; cld = cld.getParent() ) {
			if( cld instanceof URLClassLoader ) {
				classPath.addAll( Arrays.asList( ((URLClassLoader) cld).getURLs() ) );
			}
		}

		// check the classpath as well
		final String sysClassPath = System.getProperty( "java.class.path", "" );
		final String[] elements = sysClassPath.split( "(:|;)" );

		for( final String element : elements ) {
			final File file = new File( element );

			if( file.exists() ) {
				classPath.add( file.toURI().toURL() );
			}
		}

		for( final URL u : classPath ) {
			scan( u );
		}
	}

	private void scan( URL u ) throws IOException
	{
		if( !u.getProtocol().equals( "file" ) ) {
			return;
		}

		final File f = new File( u.getFile() );

		if( !f.exists() ) {
			return;
		}

		if( f.isDirectory() ) {
			final int fz = f.getAbsolutePath().length() + 1;
			final FileIterator fit = new FileIterator( f, ".class" );

			while( fit.hasNext() ) {
				final File cf = fit.next();

				if( cf.isDirectory() ) {
					continue;
				}

				final String cn = cf.getAbsolutePath().substring( fz );

				final InputStream is = new FileInputStream( cf );

				try {
					visit( cn, is );
				}
				finally {
					is.close();
				}
			}
		}
		else {
			final JarFile j = new JarFile( f );
			final Enumeration<JarEntry> e = j.entries();

			while( e.hasMoreElements() ) {
				final JarEntry x = e.nextElement();
				final String cn = x.getName();

				if( cn.endsWith( "/" ) ) {
					continue;
				}

				final InputStream s = j.getInputStream( x );

				try {
					visit( cn, s );
				}
				finally {
					s.close();
				}
			}
		}
	}

	private void visit( String path, final InputStream is ) throws IOException
	{
		path = path.replace( '\\', '/' );

		if( (path.indexOf( '/' ) < 0) || path.endsWith( "package-info.class" ) || !path.endsWith( ".class" ) ) {
			return;
		}

		String clsName = path.replace( '/', '.' );

		clsName = clsName.substring( 0, clsName.length() - 6 );

		if( this.packages.size() > 0 ) {
			final String pkgName = clsName.substring( 0, clsName.lastIndexOf( '.' ) );

			boolean matched = false;

			for( final String pkg : this.packages ) {
				if( matched = pkgName.startsWith( pkg ) ) {
					break;
				}
			}

			if( !matched ) {
				return;
			}
		}

		if( this.includes.size() > 0 ) {
			boolean matched = false;

			for( final Pattern inc : this.includes ) {
				if( matched = inc.matcher( clsName ).matches() ) {

					break;
				}
			}

			if( !matched ) {
				return;
			}
		}

		for( final Pattern exc : this.excludes ) {
			if( exc.matcher( clsName ).matches() ) {
				return;
			}
		}

		if( this.annotations.size() > 0 ) {
			final ClassFile cf = ClassFile.readFrom( is );
			final List<TypeDesc> annotations = new ArrayList<TypeDesc>();

			for( final Attribute cfa : cf.getAttributes() ) {
				final String n = cfa.getName();

				if( !n.equals( Attribute.RUNTIME_VISIBLE_ANNOTATIONS )
					&& !n.equals( Attribute.RUNTIME_INVISIBLE_ANNOTATIONS ) ) {
					continue;
				}

				final AnnotationsAttr cfaa = (AnnotationsAttr) cfa;

				for( final Annotation an : cfaa.getAnnotations() ) {
					annotations.add( an.getType() );
				}
			}

			boolean included = false;

			for( final TypeDesc a : annotations ) {
				if( included = this.annotations.contains( a ) ) {
					break;
				}
			}

			if( !included ) {
				return;
			}
		}

		this.vis.visitClass( this.cld, clsName );
	}
}
