package org.apache.maven.scm.provider.svn.command.info;

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

/**
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 *
 * $Id$
 */
public class SvnInfoItem
{
    private String path;

    private String url;

    private String repositoryRoot;

    private String repositoryUUID;

    private String revision;

    private String nodeKind;

    private String schedule;

    private String lastChangedAuthor;

    private String lastChangedRevision;

    private String lastChangedDate;

    public String getPath()
    {
        return path;
    }

    public void setPath( String path )
    {
        this.path = path;
    }

    public String getURL()
    {
        return url;
    }

    public void setURL( String url )
    {
        this.url = url;
    }

    public String getRepositoryRoot()
    {
        return repositoryRoot;
    }

    public void setRepositoryRoot( String repositoryRoot )
    {
        this.repositoryRoot = repositoryRoot;
    }

    public String getRepositoryUUID()
    {
        return repositoryUUID;
    }

    public void setRepositoryUUID( String repositoryUUID )
    {
        this.repositoryUUID = repositoryUUID;
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    public String getNodeKind()
    {
        return nodeKind;
    }

    public void setNodeKind( String nodeKind )
    {
        this.nodeKind = nodeKind;
    }

    public String getSchedule()
    {
        return schedule;
    }

    public void setSchedule( String schedule )
    {
        this.schedule = schedule;
    }

    public String getLastChangedAuthor()
    {
        return lastChangedAuthor;
    }

    public void setLastChangedAuthor( String lastChangedAuthor )
    {
        this.lastChangedAuthor = lastChangedAuthor;
    }

    public String getLastChangedRevision()
    {
        return lastChangedRevision;
    }

    public void setLastChangedRevision( String lastChangedRevision )
    {
        this.lastChangedRevision = lastChangedRevision;
    }

    public String getLastChangedDate()
    {
        return lastChangedDate;
    }

    public void setLastChangedDate( String lastChangedDate )
    {
        this.lastChangedDate = lastChangedDate;
    }
}
