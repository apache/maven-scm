package org.apache.maven.scm;


/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmFile
	implements Comparable
{
    private String path;

    private ScmFileStatus status;

    public ScmFile( String path, ScmFileStatus status )
    {
        this.path = path;

        this.status = status;
    }

    public String getPath()
    {
        return path;
    }

    public ScmFileStatus getStatus()
    {
        return status;
    }

    // ----------------------------------------------------------------------
    // Comparable Implementation
    // ----------------------------------------------------------------------

    public int compareTo( Object other )
    {
        return ( (ScmFile) other).getPath().compareTo( path );
    }

    // ----------------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------------

    public boolean equals( Object other )
    {
        if ( !(other instanceof ScmFile ) )
        {
            return false;
        }

        return ( (ScmFile) other).getPath().equals( path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }
}
