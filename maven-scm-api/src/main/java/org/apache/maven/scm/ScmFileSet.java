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

import org.codehaus.plexus.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @version $Id$
 */
public class ScmFileSet
{
    private static final String DEFAULT_EXCLUDES = "**/CVS/**,**/.svn/**";

    private File basedir;

    /**
     * List of files, all relative to the basedir.
     */
    private File[] files;

    private static final File[] EMPTY_FILE_ARRAY = new File[0];

    public ScmFileSet( File basedir )
    {
        this( basedir, EMPTY_FILE_ARRAY );
    }

    public ScmFileSet( File basedir, File file )
    {
        this( basedir, new File[]{file} );
    }

    public ScmFileSet( File basedir, String includes, String excludes )
        throws IOException
    {
        this.basedir = basedir;

        if ( excludes != null && excludes.length() > 0 )
        {
            excludes += "," + DEFAULT_EXCLUDES;
        }
        else
        {
            excludes = DEFAULT_EXCLUDES;
        }

        // TODO: just use a list instead?
        files = (File[]) FileUtils.getFiles( basedir, includes, excludes, false ).toArray( EMPTY_FILE_ARRAY );
    }

    public ScmFileSet( File basedir, File[] files )
    {
        if ( basedir == null )
        {
            throw new NullPointerException( "basedir must not be null" );
        }

        if ( files == null )
        {
            throw new NullPointerException( "files must not be null" );
        }

        this.basedir = basedir;
        this.files = files;
    }

    public File getBasedir()
    {
        return basedir;
    }

    public File[] getFiles()
    {
        return this.files;
    }

    public String toString()
    {
        return "basedir = " + basedir + "; files = " + Arrays.asList( files );
    }
}
