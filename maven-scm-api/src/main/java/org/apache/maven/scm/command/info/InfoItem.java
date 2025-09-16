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
package org.apache.maven.scm.command.info;

import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;

/**
 * Encapsulates meta information about a file (or directory) being managed with an SCM.
 *
 * For historical reasons the field/method names are inspired from (and sometimes only applicable to) the <a href="https://svnbook.red-bean.com/">Subversion SCM</a>.
 *
 * @author <a href="mailto:kenney@apache.org">Kenney Westerhof</a>
 * @author Olivier Lamy
 * @since 1.5
 */
public class InfoItem {
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

    private OffsetDateTime lastChangedDateTime;

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getURL() {
        return url;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public String getRepositoryRoot() {
        return repositoryRoot;
    }

    public void setRepositoryRoot(String repositoryRoot) {
        this.repositoryRoot = repositoryRoot;
    }

    public String getRepositoryUUID() {
        return repositoryUUID;
    }

    public void setRepositoryUUID(String repositoryUUID) {
        this.repositoryUUID = repositoryUUID;
    }

    public String getRevision() {
        return revision;
    }

    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getNodeKind() {
        return nodeKind;
    }

    public void setNodeKind(String nodeKind) {
        this.nodeKind = nodeKind;
    }

    public String getSchedule() {
        return schedule;
    }

    public void setSchedule(String schedule) {
        this.schedule = schedule;
    }

    public String getLastChangedAuthor() {
        return lastChangedAuthor;
    }

    public void setLastChangedAuthor(String lastChangedAuthor) {
        this.lastChangedAuthor = lastChangedAuthor;
    }

    public String getLastChangedRevision() {
        return lastChangedRevision;
    }

    public void setLastChangedRevision(String lastChangedRevision) {
        this.lastChangedRevision = lastChangedRevision;
    }

    /**
     * @deprecated use {@link #getLastChangedDateTime()} instead
     */
    @Deprecated
    public String getLastChangedDate() {
        return lastChangedDate;
    }

    /**
     * @deprecated use {@link #setLastChangedDateTime(TemporalAccessor)} instead
     */
    @Deprecated
    public void setLastChangedDate(String lastChangedDate) {
        this.lastChangedDate = lastChangedDate;
    }

    /**
     * @return the date when the file indicated via {@link #getPath()} has been changed in the SCM for the last time
     * @since 2.1.0
     */
    public OffsetDateTime getLastChangedDateTime() {
        return lastChangedDateTime;
    }

    /**
     * @param accessor temporal accessor from which to populate the last changed date
     * @since 2.1.0
     */
    public void setLastChangedDateTime(TemporalAccessor accessor) {
        this.lastChangedDateTime = OffsetDateTime.from(accessor);
    }
}
