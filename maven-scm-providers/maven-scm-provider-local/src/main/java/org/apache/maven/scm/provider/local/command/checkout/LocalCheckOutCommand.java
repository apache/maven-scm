package org.apache.maven.scm.provider.local.command.checkout;

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.command.LocalCommand;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;

import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalCheckOutCommand
    extends AbstractCheckOutCommand
    implements LocalCommand
{
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, File workingDirectory, String tag, File[] files )
        throws ScmException
    {
        LocalScmProviderRepository repository = (LocalScmProviderRepository) repo;

        if ( !StringUtils.isEmpty( tag ) )
        {
            throw new ScmException( "The local scm doesn't support tags." );
        }

        File root = new File( repository.getRoot() );

        String module = repository.getModule();

        File source = new File( root, module );

        File baseDestination = new File( workingDirectory, module );

        if ( !workingDirectory.exists() )
        {
            throw new ScmException( "The working directory doesn't exist (" + workingDirectory.getAbsolutePath() + ")." );
        }

        if ( !root.exists() )
        {
            throw new ScmException( "The base directory doesn't exist (" + root.getAbsolutePath() + ")." );
        }

        if ( !source.exists() )
        {
            throw new ScmException( "The module directory doesn't exist (" + source.getAbsolutePath() + ")." );
        }

    	List checkedOutFiles;

        try
        {
        	FileUtils.deleteDirectory( baseDestination );

        	if ( !baseDestination.mkdirs() )
        	{
        	    throw new ScmException( "Could not create destination directory '" + baseDestination.getAbsolutePath() + "'." );
        	}

        	System.err.println( "Checking out '" + source.getAbsolutePath() + "' to '" + baseDestination.getAbsolutePath() + "'." );

        	List fileList;

        	if ( files == null || files.length == 0 )
            {
                fileList = FileUtils.getFiles( source.getAbsoluteFile(), "**", null );
            }
	        else
	        {
	            fileList = Arrays.asList( files );
	        }

            checkedOutFiles = checkOut( source, baseDestination, fileList, repository.getModule() );
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while checking out the files.", ex );
        }

        return new LocalCheckOutScmResult( checkedOutFiles );
    }

    private List checkOut( File source, File baseDestination, List files, String module )
    	throws ScmException, IOException
    {
        String sourcePath = source.getAbsolutePath();

        List checkedOutFiles = new ArrayList();

        int chop = baseDestination.getAbsolutePath().length();

        for ( Iterator i = files.iterator(); i.hasNext(); )
        {
            File file = (File) i.next();

            String dest = file.getAbsolutePath();

            dest = dest.substring( sourcePath.length() + 1 );

            File destination = new File( baseDestination, dest );

            destination = destination.getParentFile();

            if ( !destination.exists() && !destination.mkdirs() )
            {
                throw new ScmException( "Could not create destination directory '" + destination.getAbsolutePath() + "'." );
            }

            FileUtils.copyFileToDirectory( file, destination );

            File parent = file.getParentFile();

            // TODO: Add more excludes here
            if ( parent != null && parent.getName().equals( "CVS" ) )
            {
                continue;
            }

            String fileName = "/" + module + file.getAbsolutePath().substring( chop );

            checkedOutFiles.add( new ScmFile( fileName, ScmFileStatus.CHECKED_OUT ) );
        }

        return checkedOutFiles;
    }
}
