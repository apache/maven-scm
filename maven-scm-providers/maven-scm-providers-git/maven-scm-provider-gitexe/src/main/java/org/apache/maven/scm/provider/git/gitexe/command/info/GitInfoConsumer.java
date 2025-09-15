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
package org.apache.maven.scm.provider.git.gitexe.command.info;

import java.nio.file.Path;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.command.info.InfoItem;
import org.apache.maven.scm.util.AbstractConsumer;
import org.codehaus.plexus.util.cli.Arg;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * Parses output of {@code git log} with a particular format and populates a {@link InfoItem}.
 *
 * @author Olivier Lamy
 * @see <a href="https://git-scm.com/docs/git-log#_pretty_formats">Pretty Formats</a>
 * @since 1.5
 */
public class GitInfoConsumer extends AbstractConsumer {

    private final InfoItem infoItem;
    private final int revisionLength;

    public GitInfoConsumer(Path path, int revisionLength) {
        infoItem = new InfoItem();
        infoItem.setPath(path.toString());
        infoItem.setURL(path.toUri().toASCIIString());
        this.revisionLength = revisionLength;
    }

    enum LineParts {
        HASH(0),
        AUTHOR_NAME(3),
        AUTHOR_EMAIL(2),
        AUTHOR_LAST_MODIFIED(1);

        private final int index;

        LineParts(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }

    /**
     * @param line the line which is supposed to have the format as specified by {@link #getFormatArgument()}
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine(String line) {
        if (logger.isDebugEnabled()) {
            logger.debug("consume line {}", line);
        }

        // name must be last token as it may contain separators
        String[] parts = line.split("\\s", 4);
        if (parts.length != 4) {
            throw new IllegalArgumentException(
                    "Unexpected line: expecting 4 tokens separated by whitespace but got " + line);
        }
        infoItem.setLastChangedAuthor(
                parts[LineParts.AUTHOR_NAME.getIndex()] + " <" + parts[LineParts.AUTHOR_EMAIL.getIndex()] + ">");
        String revision = parts[LineParts.HASH.getIndex()];
        if (revisionLength > -1) {
            // do not truncate below 4 characters
            revision = StringUtils.truncate(revision, Integer.max(4, revisionLength));
        }
        infoItem.setRevision(revision);
        infoItem.setLastChangedDateTime(
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(parts[LineParts.AUTHOR_LAST_MODIFIED.getIndex()]));
    }

    public InfoItem getInfoItem() {
        return infoItem;
    }

    /**
     * The format argument to use with {@code git log}
     *
     * @return the format argument to use {@code git log} command
     * @see <a href="https://git-scm.com/docs/git-log#_pretty_formats">Pretty Formats</a>
     */
    public static Arg getFormatArgument() {
        Commandline.Argument arg = new Commandline.Argument();
        arg.setValue("--format=format:%H %aI %aE %aN");
        return arg;
    }
}
