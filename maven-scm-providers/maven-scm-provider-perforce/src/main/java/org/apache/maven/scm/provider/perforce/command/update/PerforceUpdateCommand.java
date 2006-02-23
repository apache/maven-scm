package org.apache.maven.scm.provider.perforce.command.update;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.changelog.ChangeLogCommand;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.perforce.command.PerforceCommand;
import org.apache.maven.scm.provider.perforce.command.changelog.PerforceChangeLogCommand;
import org.apache.maven.scm.provider.perforce.command.checkout.PerforceCheckOutCommand;

/**
 * @author Mike Perham
 * @version $Id: PerforceChangeLogCommand.java 264804 2005-08-30 16:09:04Z
 *          evenisse $
 */
public class PerforceUpdateCommand
    extends AbstractUpdateCommand
    implements PerforceCommand
{

    protected UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet files, String tag )
        throws ScmException
    {
        // In Perforce, there is no difference between update and checkout.
        // Here we just run the checkout command and map the result onto an
        // UpdateScmResult.
        PerforceCheckOutCommand command = new PerforceCheckOutCommand();
        command.setLogger( getLogger() );
        CommandParameters params = new CommandParameters();
        params.setString( CommandParameter.TAG, tag );

        CheckOutScmResult cosr = (CheckOutScmResult) command.execute( repo, files, params );
        if ( !cosr.isSuccess() )
        {
            return new UpdateScmResult( cosr.getCommandLine(), cosr.getProviderMessage(), cosr.getCommandOutput(),
                                        false );
        }

        return new UpdateScmResult( cosr.getCommandLine(), cosr.getCheckedOutFiles() );
    }

    protected ChangeLogCommand getChangeLogCommand()
    {
        PerforceChangeLogCommand command = new PerforceChangeLogCommand();
        command.setLogger( getLogger() );
        return command;
    }
}
