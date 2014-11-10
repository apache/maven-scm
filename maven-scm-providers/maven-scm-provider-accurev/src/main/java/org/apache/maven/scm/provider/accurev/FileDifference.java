package org.apache.maven.scm.provider.accurev;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;

public class FileDifference
{

    private String oldVersionSpec = null;

    private File oldFile = null;

    private String newVersionSpec = null;

    private File newFile = null;

    private long elementId = -1;

    public FileDifference( long elementId, String newPath, String newVersion, String oldPath, String oldVersion )
    {
        setElementId( elementId );
        setNewVersion( newPath, newVersion );
        setOldVersion( oldPath, oldVersion );
    }

    public FileDifference()
    {

    }

    public String getOldVersionSpec()
    {
        return oldVersionSpec;
    }

    public File getOldFile()
    {
        return oldFile;
    }

    public String getNewVersionSpec()
    {
        return newVersionSpec;
    }

    public File getNewFile()
    {
        return newFile;
    }

    public long getElementId()
    {
        return elementId;
    }

    public void setElementId( long elementId )
    {
        this.elementId = elementId;
    }

    public void setNewVersion( String path, String version )
    {

        this.newFile =
            ( oldFile != null && oldFile.getPath().equals( path ) ) ? oldFile : path == null ? null : new File( path );
        this.newVersionSpec = version;

    }

    public void setOldVersion( String path, String version )
    {

        this.oldFile =
            ( newFile != null && newFile.getPath().equals( path ) ) ? newFile : path == null ? null : new File( path );
        this.oldVersionSpec = version;

    }

    @Override
    public String toString()
    {
        return "FileDifference [elementId=" + elementId + ", newFile=" + newFile + ", newVersionSpec=" + newVersionSpec
            + ", oldFile=" + oldFile + ", oldVersionSpec=" + oldVersionSpec + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) ( elementId ^ ( elementId >>> 32 ) );
        result = prime * result + ( ( newFile == null ) ? 0 : newFile.hashCode() );
        result = prime * result + ( ( newVersionSpec == null ) ? 0 : newVersionSpec.hashCode() );
        result = prime * result + ( ( oldFile == null ) ? 0 : oldFile.hashCode() );
        result = prime * result + ( ( oldVersionSpec == null ) ? 0 : oldVersionSpec.hashCode() );
        return result;
    }

    @Override
    public boolean equals( Object obj )
    {
        if ( this == obj )
        {
            return true;
        }
        if ( obj == null )
        {
            return false;
        }
        if ( getClass() != obj.getClass() )
        {
            return false;
        }
        FileDifference other = (FileDifference) obj;
        if ( elementId != other.elementId )
        {
            return false;
        }
        if ( newFile == null )
        {
            if ( other.newFile != null )
            {
                return false;
            }
        }
        else if ( !newFile.equals( other.newFile ) )
        {
            return false;
        }
        if ( newVersionSpec == null )
        {
            if ( other.newVersionSpec != null )
            {
                return false;
            }
        }
        else if ( !newVersionSpec.equals( other.newVersionSpec ) )
        {
            return false;
        }
        if ( oldFile == null )
        {
            if ( other.oldFile != null )
            {
                return false;
            }
        }
        else if ( !oldFile.equals( other.oldFile ) )
        {
            return false;
        }
        if ( oldVersionSpec == null )
        {
            if ( other.oldVersionSpec != null )
            {
                return false;
            }
        }
        else if ( !oldVersionSpec.equals( other.oldVersionSpec ) )
        {
            return false;
        }
        return true;
    }

}
