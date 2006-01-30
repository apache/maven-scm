package org.apache.maven.scm.provider.bazaar;

import java.io.File;
import java.io.IOException;

import org.apache.maven.scm.provider.bazaar.command.BazaarCommand;
import org.codehaus.plexus.util.FileUtils;

/**
 * Common code used in all tests.
 *
 * @author <a href="mailto:torbjorn@smorgrav.org">Torbjørn Eikli Smørgrav</a>
 */
public class BazaarTestUtils
{

    public static final String[] filesInOriginalRepository = new String[] {
        "pom.xml",
        "readme.txt",
        "src/main/java/Application.java",
        "src/test/java/Test.java" };

    public static final String TCK_FILE_CONSTANT = "/";

    public static final String TEMP_PATH = "target"; //System.getProperty( "java.io.tmpdir" );

    public static final String COMMIT_MESSAGE = "Initial revision";

    public static String getScmUrl()
        throws Exception
    {
        File workingDir = new File( TEMP_PATH, "BazaarTmpRepository" );
        return "scm:bazaar:" + workingDir.getAbsolutePath();
    }

    public static void initRepo()
        throws Exception
    {
        // Prepare tmp directory
        File workingDir = new File( TEMP_PATH, "BazaarTmpRepository" );
        if ( workingDir.exists() )
        {
            FileUtils.deleteDirectory( workingDir );
        }
        boolean workingDirReady = workingDir.mkdirs();
        if ( !workingDirReady )
        {
            throw new IOException( "Could not initiate test repository at: " + workingDir );
        }

        // Init repository
        String[] init_cmd = new String[] { BazaarCommand.INIT_CMD };
        BazaarUtils.execute( workingDir, init_cmd );

        // Create and add files to repository
        for ( int i = 0; i < filesInOriginalRepository.length; i++ )
        {
            File file = new File( workingDir, filesInOriginalRepository[i] );
            if ( !file.getParentFile().exists() )
            {
                boolean success = file.getParentFile().mkdirs();
                if ( !success )
                {
                    throw new IOException( "Could not create directories in repository for: " + file );
                }
            }
            file.createNewFile();

            FileUtils.fileWrite( file.getAbsolutePath(), TCK_FILE_CONSTANT + filesInOriginalRepository[i] );
        }
        String[] add_cmd = new String[] { BazaarCommand.ADD_CMD };
        BazaarUtils.execute( workingDir, add_cmd );

        // Commit the initial repository
        String[] commit_cmd = new String[] { BazaarCommand.COMMIT_CMD, BazaarCommand.MESSAGE_OPTION, COMMIT_MESSAGE };
        BazaarUtils.execute( workingDir, commit_cmd );
    }
}
