package org.apache.maven.scm;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author dtran
 */
public class ScmFileSetTest
    extends TestCase
{
    private static String basedirPath;

    public static String getBasedir()
    {
        if ( basedirPath != null )
        {
            return basedirPath;
        }

        basedirPath = System.getProperty( "basedir" );

        if ( basedirPath == null )
        {
            basedirPath = new File( "" ).getAbsolutePath();
        }

        return basedirPath;
    }

    public void testExcludes()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "**/**", "**/target/**" );

        File[] files = fileSet.getFiles();

        for ( int i = 0; i < files.length; ++i )
        {
            if ( files[i].getAbsolutePath().indexOf( "target" ) != -1 )
            {
                fail( "Found excludes in file set: " + files[i] );
            }
        }
    }

    public void testFilesListExcludes()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "**/**", "**/target/**" );

        List files = fileSet.getFileList();

        Iterator it = files.iterator();
        while ( it.hasNext() )
        {
            File file = (File) it.next();
            if ( file.getAbsolutePath().indexOf( "target" ) != -1 )
            {
                fail( "Found excludes in file set: " + file );
            }
        }
    }

    public void testExcludes2()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "**/scmfileset/**", "**/target/**" );

        assertEquals( 2, fileSet.getFiles().length );
    }

    public void testFilesListExcludes2()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "**/scmfileset/**", "**/target/**" );

        assertEquals( 2, fileSet.getFileList().size() );
    }

    public void testNoExcludes()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "src/**/scmfileset/**" );

        assertEquals( 2, fileSet.getFiles().length );
    }

    public void testFilesListNoExcludes()
        throws IOException
    {
        ScmFileSet fileSet = new ScmFileSet( new File( getBasedir() ), "src/**/scmfileset/**" );

        assertEquals( 2, fileSet.getFileList().size() );
    }

}
