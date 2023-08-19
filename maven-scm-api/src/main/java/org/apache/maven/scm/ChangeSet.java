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
package org.apache.maven.scm;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.util.FilenameUtils;
import org.apache.maven.scm.util.ThreadSafeDateFormat;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public class ChangeSet implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 7097705862222539801L;

    /**
     * Escaped <code>&lt;</code> entity
     */
    public static final String LESS_THAN_ENTITY = "&lt;";

    /**
     * Escaped <code>&gt;</code> entity
     */
    public static final String GREATER_THAN_ENTITY = "&gt;";

    /**
     * Escaped <code>&amp;</code> entity
     */
    public static final String AMPERSAND_ENTITY = "&amp;";

    /**
     * Escaped <code>'</code> entity
     */
    public static final String APOSTROPHE_ENTITY = "&apos;";

    /**
     * Escaped <code>"</code> entity
     */
    public static final String QUOTE_ENTITY = "&quot;";

    private static final String DATE_PATTERN = "yyyy-MM-dd";

    /**
     * Formatter used by the getDateFormatted method.
     */
    private static final ThreadSafeDateFormat DATE_FORMAT = new ThreadSafeDateFormat(DATE_PATTERN);

    private static final String TIME_PATTERN = "HH:mm:ss";

    /**
     * Formatter used by the getTimeFormatted method.
     */
    private static final ThreadSafeDateFormat TIME_FORMAT = new ThreadSafeDateFormat(TIME_PATTERN);

    /**
     * Formatter used to parse date/timestamp.
     */
    private static final ThreadSafeDateFormat TIMESTAMP_FORMAT_1 = new ThreadSafeDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final ThreadSafeDateFormat TIMESTAMP_FORMAT_2 = new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss");

    private static final ThreadSafeDateFormat TIMESTAMP_FORMAT_3 = new ThreadSafeDateFormat("yyyy/MM/dd HH:mm:ss z");

    private static final ThreadSafeDateFormat TIMESTAMP_FORMAT_4 = new ThreadSafeDateFormat("yyyy-MM-dd HH:mm:ss z");

    /**
     * Date the changes were committed
     */
    private Date date;

    /**
     * User who made changes
     */
    private String author;

    /**
     * comment provided at commit time
     */
    private String comment = "";

    /**
     * List of ChangeFile
     */
    private List<ChangeFile> files;

    /**
     * List of tags
     */
    private List<String> tags;

    /**
     * The SCM revision id for this changeset.
     * @since 1.3
     */
    private String revision;

    /**
     * Revision from which this one originates
     * @since 1.7
     */
    private String parentRevision;

    /**
     * Revisions that were merged into this one
     * @since 1.7
     */
    private Set<String> mergedRevisions;

    /**
     * @param strDate         Date the changes were committed
     * @param userDatePattern pattern of date
     * @param comment         comment provided at commit time
     * @param author          User who made changes
     * @param files           The ChangeFile list
     */
    public ChangeSet(String strDate, String userDatePattern, String comment, String author, List<ChangeFile> files) {
        this(null, comment, author, files);

        setDate(strDate, userDatePattern);
    }

    /**
     * @param date    Date the changes were committed
     * @param comment comment provided at commit time
     * @param author  User who made changes
     * @param files   The ChangeFile list
     */
    public ChangeSet(Date date, String comment, String author, List<ChangeFile> files) {
        setDate(date);

        setAuthor(author);

        setComment(comment);

        this.files = files;
    }

    /**
     * Constructor used when attributes aren't available until later
     */
    public ChangeSet() {
        // no op
    }

    /**
     * Getter for ChangeFile list.
     *
     * @return List of ChangeFile.
     */
    public List<ChangeFile> getFiles() {
        if (files == null) {
            return new ArrayList<>();
        }
        return files;
    }

    /**
     * Setter for ChangeFile list.
     *
     * @param files List of ChangeFiles.
     */
    public void setFiles(List<ChangeFile> files) {
        this.files = files;
    }

    public void addFile(ChangeFile file) {
        if (files == null) {
            files = new ArrayList<>();
        }

        files.add(file);
    }

    /**
     * @deprecated Use method {@link #containsFilename(String)}
     * @param filename TODO
     * @param repository NOT USED
     * @return TODO
     */
    public boolean containsFilename(String filename, ScmProviderRepository repository) {
        return containsFilename(filename);
    }

    public boolean containsFilename(String filename) {
        if (files != null) {
            for (ChangeFile file : files) {
                String f1 = FilenameUtils.normalizeFilename(file.getName());
                String f2 = FilenameUtils.normalizeFilename(filename);
                if (f1.indexOf(f2) >= 0) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Getter for property author.
     *
     * @return Value of property author.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Setter for property author.
     *
     * @param author New value of property author.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Getter for property comment.
     *
     * @return Value of property comment.
     */
    public String getComment() {
        return comment;
    }

    /**
     * Setter for property comment.
     *
     * @param comment New value of property comment.
     */
    public void setComment(String comment) {
        this.comment = comment;
    }

    /**
     * Getter for property date.
     *
     * @return Value of property date.
     */
    public Date getDate() {
        if (date != null) {
            return (Date) date.clone();
        }

        return null;
    }

    /**
     * Setter for property date.
     *
     * @param date New value of property date.
     */
    public void setDate(Date date) {
        if (date != null) {
            this.date = new Date(date.getTime());
        }
    }

    /**
     * Setter for property date that takes a string and parses it
     *
     * @param date - a string in yyyy/MM/dd HH:mm:ss format
     */
    public void setDate(String date) {
        setDate(date, null);
    }

    /**
     * Setter for property date that takes a string and parses it
     *
     * @param date            - a string in yyyy/MM/dd HH:mm:ss format
     * @param userDatePattern - pattern of date
     */
    public void setDate(String date, String userDatePattern) {
        try {
            if (!(userDatePattern == null || userDatePattern.isEmpty())) {
                SimpleDateFormat format = new SimpleDateFormat(userDatePattern);

                this.date = format.parse(date);
            } else {
                this.date = TIMESTAMP_FORMAT_3.parse(date);
            }
        } catch (ParseException e) {
            if (!(userDatePattern == null || userDatePattern.isEmpty())) {
                try {
                    this.date = TIMESTAMP_FORMAT_3.parse(date);
                } catch (ParseException pe) {
                    try {
                        this.date = TIMESTAMP_FORMAT_4.parse(date);
                    } catch (ParseException pe1) {
                        try {
                            this.date = TIMESTAMP_FORMAT_1.parse(date);
                        } catch (ParseException pe2) {
                            try {
                                this.date = TIMESTAMP_FORMAT_2.parse(date);
                            } catch (ParseException pe3) {
                                throw new IllegalArgumentException("Unable to parse date: " + date);
                            }
                        }
                    }
                }
            } else {
                try {
                    this.date = TIMESTAMP_FORMAT_4.parse(date);
                } catch (ParseException pe1) {
                    try {
                        this.date = TIMESTAMP_FORMAT_1.parse(date);
                    } catch (ParseException pe2) {
                        try {
                            this.date = TIMESTAMP_FORMAT_2.parse(date);
                        } catch (ParseException pe3) {
                            throw new IllegalArgumentException("Unable to parse date: " + date);
                        }
                    }
                }
            }
        }
    }

    /**
     * @return date in yyyy-mm-dd format
     */
    public String getDateFormatted() {
        return DATE_FORMAT.format(getDate());
    }

    /**
     * @return time in HH:mm:ss format
     */
    public String getTimeFormatted() {
        return TIME_FORMAT.format(getDate());
    }

    /**
     * Getter for property tags.
     *
     * @return Value of property author.
     */
    public List<String> getTags() {
        if (tags == null) {
            return new ArrayList<>();
        }
        return tags;
    }

    /**
     * Setter for property tags.
     *
     * @param tags New value of property tags. This replaces the existing list (if any).
     */
    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    /**
     * Setter for property tags.
     *
     * @param tag New tag to add to the list of tags.
     */
    public void addTag(String tag) {
        if (tag == null) {
            return;
        }
        tag = tag.trim();
        if (tag.isEmpty()) {
            return;
        }
        if (tags == null) {
            tags = new ArrayList<>();
        }
        tags.add(tag);
    }

    /**
     * @return TODO
     * @since 1.3
     */
    public String getRevision() {
        return revision;
    }

    /**
     * @param revision TODO
     * @since 1.3
     */
    public void setRevision(String revision) {
        this.revision = revision;
    }

    public String getParentRevision() {
        return parentRevision;
    }

    public void setParentRevision(String parentRevision) {
        this.parentRevision = parentRevision;
    }

    public void addMergedRevision(String mergedRevision) {
        if (mergedRevisions == null) {
            mergedRevisions = new LinkedHashSet<>();
        }
        mergedRevisions.add(mergedRevision);
    }

    public Set<String> getMergedRevisions() {
        return mergedRevisions == null ? Collections.<String>emptySet() : mergedRevisions;
    }

    public void setMergedRevisions(Set<String> mergedRevisions) {
        this.mergedRevisions = mergedRevisions;
    }

    /** {@inheritDoc} */
    public String toString() {
        StringBuilder result = new StringBuilder(author == null ? " null " : author);
        result.append("\n").append(date == null ? "null " : date.toString()).append("\n");
        List<String> tags = getTags();
        if (!tags.isEmpty()) {
            result.append("tags:").append(tags).append("\n");
        }
        // parent(s)
        if (parentRevision != null) {
            result.append("parent: ").append(parentRevision);
            if (!getMergedRevisions().isEmpty()) {
                result.append(" + ");
                result.append(getMergedRevisions());
            }
            result.append("\n");
        }
        if (files != null) {
            for (ChangeFile file : files) {
                result.append(file == null ? " null " : file.toString()).append("\n");
            }
        }

        result.append(comment == null ? " null " : comment);

        return result.toString();
    }

    /**
     * Provide the changelog entry as an XML snippet.
     *
     * @return a changelog-entry in xml format
     * TODO make sure comment doesn't contain CDATA tags - MAVEN114
     */
    public String toXML() {
        StringBuilder buffer = new StringBuilder("\t<changelog-entry>\n");

        if (getDate() != null) {
            buffer.append("\t\t<date pattern=\"" + DATE_PATTERN + "\">")
                    .append(getDateFormatted())
                    .append("</date>\n")
                    .append("\t\t<time pattern=\"" + TIME_PATTERN + "\">")
                    .append(getTimeFormatted())
                    .append("</time>\n");
        }

        buffer.append("\t\t<author><![CDATA[").append(author).append("]]></author>\n");

        if (parentRevision != null) {
            buffer.append("\t\t<parent>").append(getParentRevision()).append("</parent>\n");
        }
        for (String mergedRevision : getMergedRevisions()) {
            buffer.append("\t\t<merge>").append(mergedRevision).append("</merge>\n");
        }

        if (files != null) {
            for (ChangeFile file : files) {
                buffer.append("\t\t<file>\n");
                if (file.getAction() != null) {
                    buffer.append("\t\t\t<action>").append(file.getAction()).append("</action>\n");
                }
                buffer.append("\t\t\t<name>")
                        .append(escapeValue(file.getName()))
                        .append("</name>\n");
                buffer.append("\t\t\t<revision>").append(file.getRevision()).append("</revision>\n");
                if (file.getOriginalName() != null) {
                    buffer.append("\t\t\t<orig-name>");
                    buffer.append(escapeValue(file.getOriginalName()));
                    buffer.append("</orig-name>\n");
                }
                if (file.getOriginalRevision() != null) {
                    buffer.append("\t\t\t<orig-revision>");
                    buffer.append(file.getOriginalRevision());
                    buffer.append("</orig-revision>\n");
                }
                buffer.append("\t\t</file>\n");
            }
        }
        buffer.append("\t\t<msg><![CDATA[").append(removeCDataEnd(comment)).append("]]></msg>\n");
        List<String> tags = getTags();
        if (!tags.isEmpty()) {
            buffer.append("\t\t<tags>\n");
            for (String tag : tags) {
                buffer.append("\t\t\t<tag>").append(escapeValue(tag)).append("</tag>\n");
            }
            buffer.append("\t\t</tags>\n");
        }
        buffer.append("\t</changelog-entry>\n");

        return buffer.toString();
    }

    /** {@inheritDoc} */
    public boolean equals(Object obj) {
        if (obj instanceof ChangeSet) {
            ChangeSet changeSet = (ChangeSet) obj;

            if (toString().equals(changeSet.toString())) {
                return true;
            }
        }

        return false;
    }

    /** {@inheritDoc} */
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((author == null) ? 0 : author.hashCode());
        result = prime * result + ((comment == null) ? 0 : comment.hashCode());
        result = prime * result + ((date == null) ? 0 : date.hashCode());
        result = prime * result + ((parentRevision == null) ? 0 : parentRevision.hashCode());
        result = prime * result + ((mergedRevisions == null) ? 0 : mergedRevisions.hashCode());
        result = prime * result + ((files == null) ? 0 : files.hashCode());
        return result;
    }

    /**
     * remove a <code>]]></code> from comments (replace it with <code>] ] ></code>).
     *
     * @param message The message to modify
     * @return a clean string
     */
    private String removeCDataEnd(String message) {
        // check for invalid sequence ]]>
        int endCdata;
        while (message != null && (endCdata = message.indexOf("]]>")) > -1) {
            message = message.substring(0, endCdata) + "] ] >" + message.substring(endCdata + 3, message.length());
        }
        return message;
    }

    /**
     * <p>Escape the <code>toString</code> of the given object.
     * For use in an attribute value.</p>
     * <p>
     * swiped from jakarta-commons/betwixt -- XMLUtils.java
     *
     * @param value escape <code>value.toString()</code>
     * @return text with characters restricted (for use in attributes) escaped
     */
    public static String escapeValue(Object value) {
        StringBuilder buffer = new StringBuilder(value.toString());
        for (int i = 0, size = buffer.length(); i < size; i++) {
            switch (buffer.charAt(i)) {
                case '<':
                    buffer.replace(i, i + 1, LESS_THAN_ENTITY);
                    size += 3;
                    i += 3;
                    break;
                case '>':
                    buffer.replace(i, i + 1, GREATER_THAN_ENTITY);
                    size += 3;
                    i += 3;
                    break;
                case '&':
                    buffer.replace(i, i + 1, AMPERSAND_ENTITY);
                    size += 4;
                    i += 4;
                    break;
                case '\'':
                    buffer.replace(i, i + 1, APOSTROPHE_ENTITY);
                    size += 5;
                    i += 5;
                    break;
                case '\"':
                    buffer.replace(i, i + 1, QUOTE_ENTITY);
                    size += 5;
                    i += 5;
                    break;
                default:
            }
        }
        return buffer.toString();
    }
}
