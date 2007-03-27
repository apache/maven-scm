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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class ScmFile
    implements Comparable
{
    private String path;

    private ScmFileStatus status;

    /**
     * @param path   The file path
     * @param status The file status
     */
    public ScmFile( String path, ScmFileStatus status )
    {
        this.path = path;

        this.status = status;
    }

    /**
     * @return the file path
     */
    public String getPath()
    {
        return path;
    }

    /**
     * @return The file status
     */
    public ScmFileStatus getStatus()
    {
        return status;
    }

    // ----------------------------------------------------------------------
    // Comparable Implementation
    // ----------------------------------------------------------------------

    public int compareTo( Object other )
    {
        return ( (ScmFile) other ).getPath().compareTo( path );
    }

    // ----------------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------------

    public boolean equals( Object other )
    {
        if ( !( other instanceof ScmFile ) )
        {
            return false;
        }

        return ( (ScmFile) other ).getPath().equals( path );
    }

    public int hashCode()
    {
        return path.hashCode();
    }

    public String toString()
    {
        return "[" + path + ":" + status + "]";
    }
}
