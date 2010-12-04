package org.apache.maven.scm.provider.accurev.util;

import java.io.File;
import java.io.IOException;

public class WorkspaceUtils
{
    public static boolean isSameFile( File file1, String filename2)
    {
        return isSameFile( file1, filename2 == null ? null : new File( filename2 ) );

    }

    //We need to canonicalise the files (if we can) before we compare them..
    public static boolean isSameFile( File file1, File file2 )
    {
        
        if ( file1 == file2 || ( file1 == null && file2 == null ) )
        {
            return true;
        }

        if ( file1 == null || file2 == null )
        {
            return false;
        }

        try
        {
            file1 = file1.getCanonicalFile();
        }
        catch ( IOException ioEx )
        {
            //Oh well, we'll compare the non-canonicalised file then.
        }

        try
        {
            file2 = file2.getCanonicalFile();
        }
        catch ( IOException ioEx )
        {
            //Oh well, we'll compare the non-canonicalised file then.
        }
        return file1.equals( file2 );
    }

}
