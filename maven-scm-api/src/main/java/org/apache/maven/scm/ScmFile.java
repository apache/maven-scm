package org.apache.maven.scm;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
