package org.apache.maven.scm;

/*
 * LICENSE
 */

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmFileStatus
{
    public final static ScmFileStatus UPDATED = new ScmFileStatus( "updated" );
    public final static ScmFileStatus PATCHED = new ScmFileStatus( "patched" );
    public final static ScmFileStatus CONFLICT = new ScmFileStatus( "conflict" );
    public final static ScmFileStatus CHECKED_IN = new ScmFileStatus( "checked-in" );
    public final static ScmFileStatus CHECKED_OUT = new ScmFileStatus( "checked-out" );

    private String name;

    private ScmFileStatus( String name )
    {
        this.name = name;
    }

    public boolean equals( Object o )
    {
        if ( !(o instanceof ScmFileStatus ) )
        {
            return false;
        }

        return ((ScmFileStatus) o).name.equals( name );
    }

    public String toString()
    {
        return name;
    }
}
