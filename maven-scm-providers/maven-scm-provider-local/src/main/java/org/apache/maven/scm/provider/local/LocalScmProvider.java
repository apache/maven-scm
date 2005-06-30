package org.apache.maven.scm.provider.local;

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
import java.util.Map;

import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.local.repository.LocalScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;

import org.codehaus.plexus.util.StringUtils;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class LocalScmProvider
    extends AbstractScmProvider
{
    /** @requirement org.apache.maven.scm.CvsCommand */
    private Map commands;

    protected Map getCommands()
    {
        return commands;
    }

    public String getScmType()
    {
        return "local";
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        String[] tokens = StringUtils.split( scmSpecificUrl, Character.toString( delimiter ) );

        if ( tokens.length != 2 )
        {
            throw new ScmRepositoryException( "The connection string didn't contain the expected number of tokens. Expected 2 tokens but got " + tokens.length + " tokens." );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String root = tokens[ 0 ];

        File rootFile = new File( root );

        if ( !rootFile.isAbsolute() )
        {
            String basedir = System.getProperty( "basedir", new File( "" ).getAbsolutePath() );

            rootFile = new File( basedir, root );
        }

        if ( !rootFile.exists() )
        {
            throw new ScmRepositoryException( "The root doesn't exists (" + rootFile.getAbsolutePath() + ")." );
        }

        if ( !rootFile.isDirectory() )
        {
            throw new ScmRepositoryException( "The root isn't a directory (" + rootFile.getAbsolutePath() + ")." );
        }

        // ----------------------------------------------------------------------
        //
        // ----------------------------------------------------------------------

        String module = tokens[ 1 ];

        File moduleFile = new File( rootFile, module );

        if ( !moduleFile.exists() )
        {
            throw new ScmRepositoryException( "The module doesn't exist (root: " + rootFile.getAbsolutePath() + ", module: " + module + ").");
        }

        if ( !moduleFile.isDirectory() )
        {
            throw new ScmRepositoryException( "The module isn't a directory." );
        }

        return new LocalScmProviderRepository( rootFile.getAbsolutePath(), module );
    }

    // ----------------------------------------------------------------------
    // Utility methods
    // ----------------------------------------------------------------------

    public static String fixModuleName( String module )
    {
        if ( module.endsWith( "/" ) )
        {
            module = module.substring( 0, module.length() - 1 );
        }

        if ( module.startsWith( "/" ) )
        {
            module = module.substring( 1 );
        }

        return module;
    }
}
