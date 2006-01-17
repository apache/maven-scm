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
public final class ScmFileStatus
{
    public final static ScmFileStatus ADDED = new ScmFileStatus( "added" );

    public final static ScmFileStatus DELETED = new ScmFileStatus( "deleted" );

    public final static ScmFileStatus CHECKED_IN = new ScmFileStatus( "checked-in" );

    public final static ScmFileStatus CHECKED_OUT = new ScmFileStatus( "checked-out" );

    public final static ScmFileStatus CONFLICT = new ScmFileStatus( "conflict" );

    public final static ScmFileStatus PATCHED = new ScmFileStatus( "patched" );

    public final static ScmFileStatus UPDATED = new ScmFileStatus( "updated" );

    public static final ScmFileStatus TAGGED = new ScmFileStatus( "tagged" );

    public static final ScmFileStatus MODIFIED = new ScmFileStatus( "modified" );

    public static final ScmFileStatus UNKNOWN = new ScmFileStatus( "unknown" );

    private String name;

    private ScmFileStatus( String name )
    {
        this.name = name;
    }

    public boolean equals( Object o )
    {
        if ( !( o instanceof ScmFileStatus ) )
        {
            return false;
        }

        return ( (ScmFileStatus) o ).name.equals( name );
    }

    public String toString()
    {
        return name;
    }
}
