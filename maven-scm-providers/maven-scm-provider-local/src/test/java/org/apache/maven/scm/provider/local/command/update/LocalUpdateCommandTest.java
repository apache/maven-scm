package org.apache.maven.scm.provider.local.command.update;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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
        return "scm:local|" + workingDirectory.getAbsolutePath() + "|" + moduleName;
	}

    public void initRepo( File workingDirectory, String moduleName )
		throws Exception
	{
        makeRepo( workingDirectory, moduleName, true );
	}

    private void makeRepo( File workingDirectory, String moduleName, boolean includeModuleName )
		throws Exception
	{
        if ( !includeModuleName )
        {
            moduleName = "";
        }

        makeFile( workingDirectory, moduleName + "/pom.xml" );

        makeFile( workingDirectory, moduleName + "/readme.txt" );

        makeFile( workingDirectory, moduleName + "/src/main/java/Application.java" );

        makeFile( workingDirectory, moduleName + "/src/test/java/Test.java" );

        makeDirectory( workingDirectory, moduleName + "/src/test/resources" );
    }

    public void checkOut( File workingDirectory, String moduleName )
    	throws Exception
    {
        makeRepo( workingDirectory, moduleName, false );
    }

    public void commit( File workingDirectory, ScmRepository repository )
    	throws Exception
    {
        LocalScmProviderRepository localRepository = (LocalScmProviderRepository) repository.getProviderRepository();

        // Only copy files newer than in the repo
        File repo = new File( localRepository.getRoot(), localRepository.getModule() );

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
