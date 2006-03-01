package org.apache.maven.scm.provider.cvslib.cvsexe.command.checkin;

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
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.provider.cvslib.command.checkin.AbstractCvsCheckInCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

import java.io.File;
import java.io.IOException;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CvsExeCheckInCommand
    extends AbstractCvsCheckInCommand
{
    protected CheckInScmResult executeCvsCommand( Commandline cl, CvsScmProviderRepository repository,
                                                  File messageFile )
        throws ScmException
    {
        CvsCheckInConsumer consumer = new CvsCheckInConsumer( repository.getPath(), getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        int exitCode;

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        try
        {
            FileUtils.forceDelete( messageFile );
        }
        catch ( IOException ex )
        {
            // ignore
        }

        if ( exitCode != 0 )
        {
            return new CheckInScmResult( cl.toString(), "The cvs command failed.", stderr.getOutput(), false );
        }

        return new CheckInScmResult( cl.toString(), consumer.getCheckedInFiles() );
    }
}
