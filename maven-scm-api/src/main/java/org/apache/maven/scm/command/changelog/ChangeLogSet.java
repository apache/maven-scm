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
package org.apache.maven.scm.command.changelog;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.maven.scm.ChangeSet;
import org.apache.maven.scm.ScmVersion;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 */
public class ChangeLogSet {
    public static final String DEFAULT_ENCODING = "ISO-8859-1";

    private List<ChangeSet> entries;

    private Date startDate;

    private Date endDate;

    private ScmVersion startVersion;

    private ScmVersion endVersion;

    /**
     * Initializes a new instance of this class.
     *
     * @param startDate the start date/tag for this set
     * @param endDate   the end date/tag for this set, or <code>null</code> if this set goes to the present time
     */
    public ChangeLogSet(Date startDate, Date endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    /**
     * Initializes a new instance of this class.
     *
     * @param entries   collection of {@link org.apache.maven.scm.ChangeSet} objects for this set
     * @param startDate the start date/tag for this set
     * @param endDate   the end date/tag for this set, or <code>null</code> if this set goes to the present time
     */
    public ChangeLogSet(List<ChangeSet> entries, Date startDate, Date endDate) {
        this(startDate, endDate);
        setChangeSets(entries);
    }

    /**
     * Returns the start date.
     *
     * @return the start date
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * Returns the end date for this set.
     *
     * @return the end date for this set, or <code>null</code> if this set goes to the present time
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * Returns the start version (revision/branch/label) for this set.
     *
     * @return the start version (revision/branch/label) for this set, or <code>null</code>
     */
    public ScmVersion getStartVersion() {
        return startVersion;
    }

    public void setStartVersion(ScmVersion startVersion) {
        this.startVersion = startVersion;
    }

    /**
     * Returns the end version (revision/branch/label) for this set.
     *
     * @return the end version (revision/branch/label) for this set, or <code>null</code>
     */
    public ScmVersion getEndVersion() {
        return endVersion;
    }

    public void setEndVersion(ScmVersion endVersion) {
        this.endVersion = endVersion;
    }

    /**
     * Returns the collection of changeSet.
     *
     * @return the collection of {@link org.apache.maven.scm.ChangeSet} objects for this set
     */
    public List<ChangeSet> getChangeSets() {
        return entries;
    }

    public void setChangeSets(List<ChangeSet> changeSets) {
        this.entries = changeSets;
    }

    /**
     * Creates an XML representation of this change log set with a default encoding (ISO-8859-1).
     *
     * @return TODO
     */
    public String toXML() {
        return toXML(DEFAULT_ENCODING);
    }

    /**
     * Creates an XML representation of this change log set.
     *
     * @param encoding encoding of output
     * @return TODO
     */
    public String toXML(String encoding) {
        String encodingString = encoding;

        if (encodingString == null) {
            encodingString = DEFAULT_ENCODING;
        }

        StringBuilder buffer = new StringBuilder();
        String pattern = "yyyyMMdd HH:mm:ss z";
        SimpleDateFormat formatter = new SimpleDateFormat(pattern);

        buffer.append("<?xml version=\"1.0\" encoding=\"" + encodingString + "\"?>\n");
        buffer.append("<changeset datePattern=\"").append(pattern).append("\"");

        if (startDate != null) {
            buffer.append(" start=\"").append(formatter.format(getStartDate())).append("\"");
        }
        if (endDate != null) {
            buffer.append(" end=\"").append(formatter.format(getEndDate())).append("\"");
        }

        if (startVersion != null) {
            buffer.append(" startVersion=\"").append(getStartVersion()).append("\"");
        }
        if (endVersion != null) {
            buffer.append(" endVersion=\"").append(getEndVersion()).append("\"");
        }

        buffer.append(">\n");

        //  Write out the entries
        for (ChangeSet changeSet : getChangeSets()) {
            buffer.append(changeSet.toXML());
        }

        buffer.append("</changeset>\n");

        return buffer.toString();
    }
}
