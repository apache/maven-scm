package org.apache.maven.scm.provider.local.command.update;

/*
 * LICENSE
 */

import java.io.File;
import java.util.Iterator;

import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalUpdateCommandTest
	extends AbstractUpdateCommandTest
{
    public String getScmUrl( File workingDirectory, String moduleName )
    	throws Exception
	{
        return "scm:local:" + workingDirectory.getAbsolutePath() + ":" + moduleName;
	}

    public void initRepo( File workingDirectory, String moduleName )
		throws Exception
	{
        makeFile( workingDirectory, moduleName + "/pom.xml" );

        makeFile( workingDirectory, moduleName + "/readme.txt" );

        makeFile( workingDirectory, moduleName + "/src/main/java/Application.java" );

        makeFile( workingDirectory, moduleName + "/src/test/java/Test.java" );

        makeDirectory( workingDirectory, moduleName + "/src/test/resources" );
	}

    public void checkOut( File workingDirectory, String moduleName )
    	throws Exception
    {
        initRepo( workingDirectory, moduleName );
    }

    public void commit( File workingDirectory, ScmRepository repository )
    	throws Exception
    {
        LocalScmProviderRepository localRepository = (LocalScmProviderRepository) repository.getProviderRepository();

        // Only copy files newer than in the repo
        File repo = new File( localRepository.getRoot() );

        Iterator it = FileUtils.getFiles( workingDirectory, "**", null ).iterator();

        while ( it.hasNext() )
        {
            File file = (File) it.next();

            File repoFile = new File( repo, file.getAbsolutePath().substring( workingDirectory.getAbsolutePath().length()) );

            if ( repoFile.exists() )
            {
                String repoFileContents = FileUtils.fileRead( repoFile );

                String fileContents = FileUtils.fileRead( file );

                if ( fileContents.equals( repoFileContents ) )
                {
                    continue;
                }
            }

            FileUtils.copyFile( file, repoFile );
        }
    }
}
