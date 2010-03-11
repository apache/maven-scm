package org.apache.maven.scm.command.blame;

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

import java.io.Serializable;
import java.util.Date;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class BlameLine
    implements Serializable
{
    private Date date;

    private String revision;

    private String author;

    public BlameLine( Date date, String revision, String author )
    {
        setDate( date );
        setRevision( revision );
        setAuthor( author );
    }

    public String getRevision()
    {
        return revision;
    }

    public void setRevision( String revision )
    {
        this.revision = revision;
    }

    public String getAuthor()
    {
        return author;
    }

    public void setAuthor( String author )
    {
        this.author = author;
    }

    public Date getDate()
    {
        if ( date != null )
        {
            return (Date) date.clone();
        }
        return null;
    }

    public void setDate( Date date )
    {
        if ( date != null )
        {
            this.date = new Date( date.getTime() );
        }
        else
        {
            this.date = null;
        }
    }
}
