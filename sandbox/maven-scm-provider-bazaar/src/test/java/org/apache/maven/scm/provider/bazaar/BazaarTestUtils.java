package org.apache.maven.scm.provider.bazaar;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.codehaus.plexus.util.FileUtils;

/**
 * Common code used in all tests.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarTestUtils
{

    public static final String[] filesInTestBranch = new String[] {
        "pom.xml",
        "readme.txt",
        "src/main/java/Application.java",
        "src/test/java/Test.java" };

    public static final String TCK_FILE_CONSTANT = "/";

    public static final String BRANCH_NAME = "target" + File.separator + "test-branch";

    public static final File WORKING_DIR = new File(BRANCH_NAME);

    public static final String COMMIT_MESSAGE = "Add files to test branch";

    public static String getScmUrl()
        throws Exception
    {
        return "scm:bazaar:" + WORKING_DIR.getAbsolutePath();
    }

    public static void initRepo()
        throws Exception
    {
        // Prepare tmp directory
        if ( WORKING_DIR.exists() )
        {
            FileUtils.deleteDirectory( WORKING_DIR );
        }
        boolean workingDirReady = WORKING_DIR.mkdirs();
        if ( !workingDirReady )
        {
            throw new IOException( "Could not initiate test branch at: " + WORKING_DIR );
        }

        // Init repository
        String[] init_cmd = new String[] { BazaarCommand.INIT_CMD };
        BazaarUtils.execute( WORKING_DIR, init_cmd );

        // Create and add files to repository
        List files = new ArrayList();
        for ( int i = 0; i < filesInTestBranch.length; i++ )
        {
            File file = new File( WORKING_DIR.getAbsolutePath(), filesInTestBranch[i] );
            if ( file.getParentFile() != null && !file.getParentFile().exists() )
            {
                boolean success = file.getParentFile().mkdirs();
                if ( !success )
                {
                    throw new IOException( "Could not create directories in branch for: " + file );
                }
            }
            file.createNewFile();

            FileUtils.fileWrite( file.getAbsolutePath(), TCK_FILE_CONSTANT + filesInTestBranch[i] );

            files.add(file);
        }

        //Add to repository
        String[] add_cmd = new String[] { BazaarCommand.ADD_CMD };
        ScmFileSet filesToAdd = new ScmFileSet(new File(""), (File[])files.toArray(new File[0]));
        add_cmd = BazaarUtils.expandCommandLine(add_cmd, filesToAdd);
        BazaarUtils.execute( WORKING_DIR, add_cmd );

        // Commit the initial repository
        String[] commit_cmd = new String[] { BazaarCommand.COMMIT_CMD,
                BazaarCommand.MESSAGE_OPTION, COMMIT_MESSAGE };
        BazaarUtils.execute( WORKING_DIR, commit_cmd );
    }
}
