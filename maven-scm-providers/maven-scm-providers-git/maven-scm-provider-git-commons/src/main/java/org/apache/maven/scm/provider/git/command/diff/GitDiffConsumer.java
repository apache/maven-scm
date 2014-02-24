package org.apache.maven.scm.provider.git.command.diff;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author <a href="mailto:brett@apache.org">Brett Porter</a>
 * @author <a href="mailto:struberg@yahoo.de">Mark Struberg</a>
 * @author Olivier Lamy
 *
 */
public class GitDiffConsumer
    implements StreamConsumer
{
    // diff --git a/readme.txt b/readme.txt
    // index fea1611..9e131cf 100644
    // --- a/readme.txt
    // +++ b/readme.txt
    // @@ -1 +1 @@
    // -/readme.txt
    // \ No newline at end of file
    // +new version of /readme.txt


    /**
     * patern matches the index line of the diff comparison
     * paren.1 matches the first file
     * paren.2 matches the 2nd file
     */
    private static final Pattern DIFF_FILES_PATTERN = Pattern.compile( "^diff --git\\sa/(.*)\\sb/(.*)" );

    private static final String START_REVISION_TOKEN = "---";

    private static final String END_REVISION_TOKEN = "+++";

    private static final String ADDED_LINE_TOKEN = "+";

    private static final String REMOVED_LINE_TOKEN = "-";

    private static final String UNCHANGED_LINE_TOKEN = " ";

    private static final String CHANGE_SEPARATOR_TOKEN = "@@";

    private static final String NO_NEWLINE_TOKEN = "\\ No newline at end of file";

    private static final String INDEX_LINE_TOKEN = "index ";

    private static final String NEW_FILE_MODE_TOKEN = "new file mode ";

    private static final String DELETED_FILE_MODE_TOKEN = "deleted file mode ";

    private ScmLogger logger;

    private String currentFile;

    private StringBuilder currentDifference;

    private List<ScmFile> changedFiles = new ArrayList<ScmFile>();

    private Map<String,CharSequence> differences = new HashMap<String,CharSequence>();

    private StringBuilder patch = new StringBuilder();

    // ----------------------------------------------------------------------
    //
    // ----------------------------------------------------------------------

    public GitDiffConsumer( ScmLogger logger, File workingDirectory )
    {
        this.logger = logger;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    /** {@inheritDoc} */
    public void consumeLine( String line )
    {
        Matcher matcher = DIFF_FILES_PATTERN.matcher( line );
        if ( matcher.matches() )
        {
            // start a new file
            currentFile = matcher.group( 1 );

            changedFiles.add( new ScmFile( currentFile, ScmFileStatus.MODIFIED ) );

            currentDifference = new StringBuilder();

            differences.put( currentFile, currentDifference );

            patch.append( line ).append( "\n" );

            return;
        }

        if ( currentFile == null )
        {
            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Unparseable line: '" + line + "'" );
            }
            patch.append( line ).append( "\n" );
            return;
        }
        else if ( line.startsWith( INDEX_LINE_TOKEN ) )
        {
            // skip, though could parse to verify start revision and end revision
            patch.append( line ).append( "\n" );
        }
        else if ( line.startsWith( NEW_FILE_MODE_TOKEN ) || line.startsWith( DELETED_FILE_MODE_TOKEN ) )
        {
            // skip, though could parse to verify file mode
            patch.append( line ).append( "\n" );
        }
        else if ( line.startsWith( START_REVISION_TOKEN ) )
        {
            // skip, though could parse to verify filename, start revision
            patch.append( line ).append( "\n" );
        }
        else if ( line.startsWith( END_REVISION_TOKEN ) )
        {
            // skip, though could parse to verify filename, end revision
            patch.append( line ).append( "\n" );
        }
        else if ( line.startsWith( ADDED_LINE_TOKEN ) || line.startsWith( REMOVED_LINE_TOKEN )
            || line.startsWith( UNCHANGED_LINE_TOKEN ) || line.startsWith( CHANGE_SEPARATOR_TOKEN )
            || line.equals( NO_NEWLINE_TOKEN ) )
        {
            // add to buffer
            currentDifference.append( line ).append( "\n" );
            patch.append( line ).append( "\n" );
        }
        else
        {
            // TODO: handle property differences

            if ( logger.isWarnEnabled() )
            {
                logger.warn( "Unparseable line: '" + line + "'" );
            }
            patch.append( line ).append( "\n" );
            // skip to next file
            currentFile = null;
            currentDifference = null;
        }
    }

    public List<ScmFile> getChangedFiles()
    {
        return changedFiles;
    }

    public Map<String,CharSequence> getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch.toString();
    }

}
