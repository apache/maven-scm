package org.apache.maven.scm.provider.hg.command.diff;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.hg.command.HgConsumer;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 */
public class HgDiffConsumer
        extends HgConsumer
{

    // private final static String MODIFIED_FILE_TOKEN = "=== modified file ";

    private final static String INDEX_TOKEN = "diff -r ";

    private final static String FILE_SEPARATOR_TOKEN = "===";

    private final static String START_REVISION_TOKEN = "---";

    private final static String END_REVISION_TOKEN = "+++";

    private final static String ADDED_LINE_TOKEN = "+";

    private final static String REMOVED_LINE_TOKEN = "-";

    private final static String UNCHANGED_LINE_TOKEN = " ";

    private final static String CHANGE_SEPARATOR_TOKEN = "@@";

    private final static String NO_NEWLINE_TOKEN = "\\ No newline at end of file";

    private final static int HASH_ID_LEN = 12;

    private ScmLogger logger;

    private String currentFile;

    private StringBuffer currentDifference;

    private List changedFiles = new ArrayList();

    private Map differences = new HashMap();

    private StringBuffer patch = new StringBuffer();

    private File workingDirectory;


    public HgDiffConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger );
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    // ----------------------------------------------------------------------
    // StreamConsumer Implementation
    // ----------------------------------------------------------------------

    public void consumeLine( String line )
    {
        if ( line.startsWith( INDEX_TOKEN ) )
        {
            // start a new file
            currentFile = line.substring( INDEX_TOKEN.length() + HASH_ID_LEN + 1 );

            changedFiles.add( new ScmFile( currentFile, ScmFileStatus.MODIFIED ) );

            currentDifference = new StringBuffer();

            differences.put( currentFile, currentDifference );

            patch.append( line ).append( "\n" );

            return;
        }

        if ( currentFile == null )
        {
            logger.warn( "Unparseable line: '" + line + "'" );
            patch.append( line ).append( "\n" );
            return;
        }

        if ( line.startsWith( FILE_SEPARATOR_TOKEN ) )
        {
            // skip
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
        else if ( line.startsWith( ADDED_LINE_TOKEN ) || line.startsWith( REMOVED_LINE_TOKEN ) ||
                  line.startsWith( UNCHANGED_LINE_TOKEN ) || line.startsWith( CHANGE_SEPARATOR_TOKEN ) ||
                  line.equals( NO_NEWLINE_TOKEN ) )
        {
            // add to buffer
            currentDifference.append( line ).append( "\n" );
            patch.append( line ).append( "\n" );
        }
        else
        {
            // TODO: handle property differences

            logger.warn( "Unparseable line: '" + line + "'" );
            patch.append( line ).append( "\n" );
            // skip to next file
            currentFile = null;
            currentDifference = null;
        }
    }

    public List getChangedFiles()
    {
        return changedFiles;
    }

    public Map getDifferences()
    {
        return differences;
    }

    public String getPatch()
    {
        return patch.toString();
    }
}
