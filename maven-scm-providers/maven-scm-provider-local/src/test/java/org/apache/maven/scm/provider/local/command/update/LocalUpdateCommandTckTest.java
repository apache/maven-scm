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
import org.apache.maven.scm.tck.command.update.UpdateCommandTckTest;

import org.codehaus.plexus.util.FileUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalUpdateCommandTckTest
	extends UpdateCommandTckTest
{
    private final static String moduleName = "update-tck";

    public String getScmUrl()
    	throws Exception
	{
        return "scm:local|" + getRepositoryRoot() + "|" + moduleName;
	}

    public void initRepo()
		throws Exception
	{
        makeRepo( getRepositoryRoot(), true );
	}

    private void makeRepo( File workingDirectory, boolean includeModuleName )
		throws Exception
	{
        String module = "";

        if ( includeModuleName )
        {
            module = moduleName;
        }

        makeFile( workingDirectory, module + "/pom.xml" );

        makeFile( workingDirectory, module + "/readme.txt" );

        makeFile( workingDirectory, module + "/src/main/java/Application.java" );

        makeFile( workingDirectory, module + "/src/test/java/Test.java" );

        makeDirectory( workingDirectory, module + "/src/test/resources" );
    }

    public void checkOut( File workingDirectory )
    	throws Exception
    {
        makeRepo( workingDirectory, false );
    }

    public void addFileToRepository( File workingDirectory, String file )
        throws Exception
    {
        // empty
    }

    public void addDirectoryToRepository( File workingDirectory, String directory )
        throws Exception
    {
        // empty
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
