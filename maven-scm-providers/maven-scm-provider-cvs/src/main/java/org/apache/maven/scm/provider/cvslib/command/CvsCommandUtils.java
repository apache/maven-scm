package org.apache.maven.scm.provider.cvslib.command;

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

import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.util.CvsUtil;
import org.apache.maven.scm.providers.cvslib.settings.Settings;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.util.Enumeration;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsCommandUtils
{
    private CvsCommandUtils()
    {
    }

    public static Commandline getBaseCommand( String commandName, CvsScmProviderRepository repo, ScmFileSet fileSet )
    {
        return getBaseCommand( commandName, repo, fileSet, null );
    }

    public static Commandline getBaseCommand( String commandName, CvsScmProviderRepository repo, ScmFileSet fileSet,
                                              String options )
    {
        Settings settings = CvsUtil.getSettings();

        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        if ( settings.getCompressionLevel() > 0 )
        {
            cl.createArgument().setValue( "-z" + settings.getCompressionLevel() );
        }
        else if ( !System.getProperty( "maven.scm.cvs.use_compression", "true" ).equals( "false" ) )
        {
            cl.createArgument().setValue( "-z3" );
        }

        if ( !settings.isUseCvsrc() )
        {
            cl.createArgument().setValue( "-f" ); // don't use ~/.cvsrc
        }

        if ( settings.isTraceCvsCommand() )
        {
            cl.createArgument().setValue( "-t" );
        }

        if ( !StringUtils.isEmpty( settings.getTemporaryFilesDirectory() ) )
        {
            File tempDir = new File( settings.getTemporaryFilesDirectory() );

            if ( !tempDir.exists() )
            {
                tempDir.mkdirs();
            }

            cl.createArgument().setValue( "-T" );

            cl.createArgument().setValue( tempDir.getAbsolutePath() );
        }

        if ( settings.getCvsVariables().size() > 0 )
        {
            for ( Enumeration e = settings.getCvsVariables().propertyNames(); e.hasMoreElements(); )
            {
                String key = (String) e.nextElement();
                String value = settings.getCvsVariables().getProperty( key );
                cl.createArgument().setValue( "-s" );
                cl.createArgument().setValue( key + "=" + value );
            }
        }

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( repo.getCvsRoot() );

        cl.createArgument().setLine( options );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( commandName );

        return cl;
    }
}
