package org.apache.maven.scm;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.codehaus.plexus.util.DirectoryScanner;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * Set of files used for SCM operations.
 * Consists of the base directory of the files and a list of files relative to that directory.
 *
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 *
 */
public class ScmFileSet
    implements Serializable
{
    private static final long serialVersionUID = -5978597349974797556L;

    private static final String DELIMITER = ",";
    
    /** @see DirectoryScanner#DEFAULTEXCLUDES */
    private static final String DEFAULT_EXCLUDES = StringUtils.join( DirectoryScanner.DEFAULTEXCLUDES, DELIMITER );

    private final File basedir;

    private String includes;

    private String excludes;

    /**
     * List of File objects, all relative to the basedir.
     */
    private final List<File> files;

    /**
     * Create a file set with no files, only the base directory.
     *
     * @param basedir directory files in the set are relative to
     */
    public ScmFileSet( File basedir )
    {
        this( basedir, new ArrayList<File>( 0 ) );
    }

    /**
     * Create a file set with only the file provided, relative to basedir.
     *
     * @param basedir directory file is relative to
     * @param file    file that the set will contain, has to be relative to basedir
     */
    public ScmFileSet( File basedir, File file )
    {
        this( basedir, new File[]{file} );
    }

    /**
     * Create a file set with only files (not directories) from basefile,
     * using includes and excludes provided.
     *
     * @param basedir  directory files are relative to
     * @param includes Ant pattern for files to include
     * @param excludes Ant pattern for files to exclude,
     *                 if null DEFAULT_EXCLUDES is used, else DEFAULT_EXCLUDES is added.
     * @throws IOException if any
     */
    public ScmFileSet( File basedir, String includes, String excludes )
        throws IOException
    {
        this.basedir = basedir;

        if ( excludes != null && excludes.length() > 0 )
        {
            excludes += DELIMITER + DEFAULT_EXCLUDES;
        }
        else
        {
            excludes = DEFAULT_EXCLUDES;
        }
        List<File> fileList = FileUtils.getFiles( basedir, includes, excludes, false );
        this.files = fileList;
        this.includes = includes;
        this.excludes = excludes;
    }

    /**
     * Create a file set with files from basefile, using includes provided and default excludes.
     *
     * @param basedir  directory files are relative to
     * @param includes Ant pattern for files to include
     * @throws IOException if any
     * @since 1.0
     */
    public ScmFileSet( File basedir, String includes )
        throws IOException
    {
        this( basedir, includes, null );
    }

    /**
     * Create a file set with the files provided, relative to basedir.
     *
     * @param basedir directory files are relative to
     * @param files   files that the set will contain, have to be relative to basedir
     * @deprecated use ScmFileSet( File, List )
     */
    public ScmFileSet( File basedir, File[] files )
    {
        this( basedir, Arrays.asList( files ) );
    }

    /**
     * Create a file set with the files provided, relative to basedir.
     *
     * @param basedir directory files are relative to
     * @param files   list of File objects, files that the set will contain, have to be relative to basedir
     */
    public ScmFileSet( File basedir, List<File> files )
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

    /**
     * Get the base directory of the file set. It's the directory files in the set are relative to.
     *
     * @return base directory
     */
    public File getBasedir()
    {
        return basedir;
    }

    /**
     * Get the list of files in the set, relative to basedir
     *
     * @return files in this set
     * @deprecated use getFileList() instead
     */
    public File[] getFiles()
    {
        return this.files.toArray( new File[this.files.size()] );
    }

    /**
     * Get the list of files in the set, relative to basedir
     *
     * @return List of File objects
     */
    public List<File> getFileList()
    {
        return this.files;
    }


    /**
     * @return the includes files as a comma separated string
     */
    public String getIncludes()
    {
        return this.includes;
    }


    /**
     * @return the excludes files as a comma separated string
     */
    public String getExcludes()
    {
        return this.excludes;
    }

    /** {@inheritDoc} */
    public String toString()
    {
        return "basedir = " + basedir + "; files = " + files;
    }
}
