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
package org.apache.maven.scm.command.blame;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Evgeny Mandrikov
 * @since 1.4
 */
public class BlameLine implements Serializable {

    private static final long serialVersionUID = 2675122069344705612L;

    private Date date;

    private String revision;

    private String author;

    private String committer;

    /**
     * @param date of the commit
     * @param revision of the commit
     * @param author will also be used as committer identification
     */
    public BlameLine(Date date, String revision, String author) {
        this(date, revision, author, author);
    }

    /**
     *
     * @param date of the commit
     * @param revision of the commit
     * @param author the person who wrote the line
     * @param committer the person who committed the change
     */
    public BlameLine(Date date, String revision, String author, String committer) {
        setDate(date);
        setRevision(revision);
        setAuthor(author);
        setCommitter(committer);
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCommitter() {
        return committer;
    }

    public void setCommitter(String committer) {
        this.committer = committer;
    }

    /**
     * @return the commit date
     */
    public Date getDate() {
        if (date != null) {
            return (Date) date.clone();
        }
        return null;
    }

    public void setDate(Date date) {
        if (date != null) {
            this.date = new Date(date.getTime());
        } else {
            this.date = null;
        }
    }
}
