package org.apache.maven.scm.provider.cvslib.command.checkout;

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

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.checkout.AbstractCheckOutCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.IOException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public abstract class AbstractCvsCheckOutCommand
    extends AbstractCheckOutCommand
    implements CvsCommand
{
    protected CheckOutScmResult executeCheckOutCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        if ( fileSet.getBasedir().exists() )
        {
            try
            {
                FileUtils.deleteDirectory( fileSet.getBasedir() );
            }
            catch ( IOException e )
            {
                getLogger().warn( "Can't delete " + fileSet.getBasedir().getAbsolutePath(), e );
            }
        }

        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "checkout", repository, fileSet );

        cl.setWorkingDirectory( fileSet.getBasedir().getParentFile().getAbsolutePath() );

        if ( !StringUtils.isEmpty( tag ) )
        {
            cl.createArgument().setValue( "-r" );
            cl.createArgument().setValue( tag );
        }

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( fileSet.getBasedir().getName() );

        cl.createArgument().setValue( repository.getModule() );

        getLogger().info( "Executing: " + cl );
        getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );

        return executeCvsCommand( cl );
    }

    protected abstract CheckOutScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
