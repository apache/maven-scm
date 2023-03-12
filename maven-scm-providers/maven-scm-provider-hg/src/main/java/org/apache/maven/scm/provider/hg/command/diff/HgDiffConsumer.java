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
package org.apache.maven.scm.provider.hg.command.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @author Olivier Lamy
 *
 */
public class HgDiffConsumer extends HgConsumer {

    // private static final String MODIFIED_FILE_TOKEN = "=== modified file ";

    private static final String INDEX_TOKEN = "diff -r ";

    private static final String FILE_SEPARATOR_TOKEN = "===";

    private static final String START_REVISION_TOKEN = "---";

    private static final String END_REVISION_TOKEN = "+++";

    private static final String ADDED_LINE_TOKEN = "+";

    private static final String REMOVED_LINE_TOKEN = "-";

    private static final String UNCHANGED_LINE_TOKEN = " ";

    private static final String CHANGE_SEPARATOR_TOKEN = "@@";

    private static final String NO_NEWLINE_TOKEN = "\\ No newline at end of file";

    private static final int HASH_ID_LEN = 12;

    private String currentFile;

    private StringBuilder currentDifference;

    private final List<ScmFile> changedFiles = new ArrayList<>();

    private final Map<String, CharSequence> differences = new HashMap<>();

    private final StringBuilder patch = new StringBuilder();

    @SuppressWarnings("unused")
    private final File workingDirectory;

    public HgDiffConsumer(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine(String line) {
        if (line.startsWith(INDEX_TOKEN)) {
            // start a new file
            currentFile = line.substring(INDEX_TOKEN.length() + HASH_ID_LEN + 1);

            changedFiles.add(new ScmFile(currentFile, ScmFileStatus.MODIFIED));

            currentDifference = new StringBuilder();

            differences.put(currentFile, currentDifference);

            patch.append(line).append("\n");

            return;
        }

        if (currentFile == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Unparseable line: '" + line + "'");
            }
            patch.append(line).append("\n");
            return;
        }

        if (line.startsWith(FILE_SEPARATOR_TOKEN)) {
            // skip
            patch.append(line).append("\n");
        } else if (line.startsWith(START_REVISION_TOKEN)) {
            // skip, though could parse to verify filename, start revision
            patch.append(line).append("\n");
        } else if (line.startsWith(END_REVISION_TOKEN)) {
            // skip, though could parse to verify filename, end revision
            patch.append(line).append("\n");
        } else if (line.startsWith(ADDED_LINE_TOKEN)
                || line.startsWith(REMOVED_LINE_TOKEN)
                || line.startsWith(UNCHANGED_LINE_TOKEN)
                || line.startsWith(CHANGE_SEPARATOR_TOKEN)
                || line.equals(NO_NEWLINE_TOKEN)) {
            // add to buffer
            currentDifference.append(line).append("\n");
            patch.append(line).append("\n");
        } else {
            // TODO: handle property differences

            if (logger.isWarnEnabled()) {
                logger.warn("Unparseable line: '" + line + "'");
            }
            patch.append(line).append("\n");
            // skip to next file
            currentFile = null;
            currentDifference = null;
        }
    }

    public List<ScmFile> getChangedFiles() {
        return changedFiles;
    }

    public Map<String, CharSequence> getDifferences() {
        return differences;
    }

    public String getPatch() {
        return patch.toString();
    }
}
