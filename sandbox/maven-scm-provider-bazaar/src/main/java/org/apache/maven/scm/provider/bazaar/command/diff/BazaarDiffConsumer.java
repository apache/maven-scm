package org.apache.maven.scm.provider.bazaar.command.diff;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.bazaar.command.BazaarConsumer;

/** @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a> */
public class BazaarDiffConsumer
    extends BazaarConsumer
{

    private final static String MODIFIED_FILE_TOKEN = "=== modified file ";

    private final static String ADDED_FILE_TOKEN = "=== added file ";

    private final static String DELETED_FILE_TOKEN = "=== deleted file ";

    //TODO: What is this?
    private final static String NO_NEWLINE_TOKEN = "\\ No newline at end of file";

    private final static String FROM_FILE_TOKEN = "---";

    private final static String TO_FILE_TOKEN = "+++";

    private final static String ADDED_LINE_TOKEN = "+";

    private final static String REMOVED_LINE_TOKEN = "-";

    private final static String UNCHANGED_LINE_TOKEN = " ";

    private final static String RANGE_TOKEN = "@@";

    private ScmLogger logger;

    private File workingDirectory;

    private String currentFile;

    private StringBuffer currentDifference;

    private List changedFiles = new ArrayList();

    private Map differences = new HashMap();

    private StringBuffer patch = new StringBuffer();

    public BazaarDiffConsumer( ScmLogger logger, File workingDirectory )
    {
        super( logger );
        this.logger = logger;
        this.workingDirectory = workingDirectory;
    }

    public void doConsume( ScmFileStatus status, String line )
    {
        String tmpLine = new String( line );
        patch.append( line ).append( "\n" );

        // Parse line
        if ( line.startsWith( MODIFIED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( MODIFIED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.MODIFIED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( ADDED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( ADDED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.ADDED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( DELETED_FILE_TOKEN ) )
        {
            tmpLine = line.substring( DELETED_FILE_TOKEN.length() );
            tmpLine = tmpLine.trim();
            status = ScmFileStatus.DELETED;
            addChangedFile( status, line, tmpLine );
        }
        else if ( line.startsWith( TO_FILE_TOKEN ) || line.startsWith( FROM_FILE_TOKEN ) )
        {
            // ignore (to avoid conflicts with add and remove tokens)
        }
        else if ( line.startsWith( ADDED_LINE_TOKEN ) || line.startsWith( REMOVED_LINE_TOKEN )
            || line.startsWith( UNCHANGED_LINE_TOKEN ) || line.startsWith( RANGE_TOKEN )
            || line.startsWith( NO_NEWLINE_TOKEN ) )
        {
            currentDifference.append( line ).append( "\n" );
        }
    }

    private void addChangedFile( ScmFileStatus status, String line, String tmpLine )
    {
        tmpLine = tmpLine.substring( 1, tmpLine.length() - 1 );
        File tmpFile = new File( workingDirectory, tmpLine );
        if ( tmpFile.isFile() )
        {
            currentFile = tmpLine;
            currentDifference = new StringBuffer();
            differences.put( currentFile, currentDifference );
            changedFiles.add( new ScmFile( tmpLine, status ) );
        }
        else
        {
            logger.warn( "Could not figure out of line: " + line );
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
