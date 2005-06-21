package org.apache.maven.scm.provider.cvslib.command.tag;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
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
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.cli.CommandLineException;
import org.codehaus.plexus.util.cli.CommandLineUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @version $Id$
 */
public class CvsTagCommand extends AbstractTagCommand implements CvsCommand
{
    public ScmResult executeTagCommand( ScmProviderRepository repo, ScmFileSet fileSet, String tag )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = new Commandline();

        cl.setExecutable( "cvs" );

        cl.setWorkingDirectory( fileSet.getBasedir().getAbsolutePath() );

        cl.createArgument().setValue( "-f" ); // don't use ~/.cvsrc

        cl.createArgument().setValue( "-d" );

        cl.createArgument().setValue( repository.getCvsRoot() );

        cl.createArgument().setValue( "-q" );

        cl.createArgument().setValue( "tag" );

        cl.createArgument().setValue( "-F" );

        cl.createArgument().setValue( "-c" );

        cl.createArgument().setValue( tag );

        int exitCode;

        CvsTagConsumer consumer = new CvsTagConsumer( getLogger() );

        CommandLineUtils.StringStreamConsumer stderr = new CommandLineUtils.StringStreamConsumer();

        try
        {
            exitCode = CommandLineUtils.executeCommandLine( cl, consumer, stderr );
        }
        catch ( CommandLineException ex )
        {
            throw new ScmException( "Error while executing command.", ex );
        }

        if ( exitCode != 0 )
        {
            // TODO: Improve this error message
            return new TagScmResult( "The cvs tag command failed.", stderr.getOutput(), false );
        }

        return new TagScmResult( consumer.getTaggedFiles() );
    }
}
