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
package org.apache.maven.scm.provider.git.gitexe.command.remove;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.git.gitexe.command.status.GitStatusConsumer;
import org.apache.maven.scm.util.AbstractConsumer;

/**
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Olivier Lamy
 *
 */
public class GitRemoveConsumer extends AbstractConsumer {
    /**
     * The pattern used to match deleted file lines
     */
    private static final Pattern REMOVED_PATTERN = Pattern.compile("^rm\\s'(.*)'");

    private final List<ScmFile> removedFiles = new ArrayList<>();

    private final URI relativeRepositoryPath;

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public GitRemoveConsumer() {
        this(null);
    }

    public GitRemoveConsumer(URI relativeRepositoryPath) {
        this.relativeRepositoryPath = relativeRepositoryPath;
    }

    /**
     * {@inheritDoc}
     */
    public void consumeLine(String line) {
        if (line.length() <= 2) {
            return;
        }

        Matcher matcher = REMOVED_PATTERN.matcher(line);
        if (matcher.matches()) {
            String file = GitStatusConsumer.resolvePath(matcher.group(1), relativeRepositoryPath);
            removedFiles.add(new ScmFile(file, ScmFileStatus.DELETED));
        } else {
            if (logger.isInfoEnabled()) {
                logger.info("could not parse line: " + line);
            }

            return;
        }
    }

    public List<ScmFile> getRemovedFiles() {
        return removedFiles;
    }
}
