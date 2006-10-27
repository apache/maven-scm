package org.apache.maven.scm.provider.hg;

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

import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.command.changelog.ChangeLogScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.command.diff.DiffScmResult;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.command.status.StatusScmResult;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.AbstractScmProvider;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.hg.command.HgCommand;
import org.apache.maven.scm.provider.hg.command.add.HgAddCommand;
import org.apache.maven.scm.provider.hg.command.changelog.HgChangeLogCommand;
import org.apache.maven.scm.provider.hg.command.checkin.HgCheckInCommand;
import org.apache.maven.scm.provider.hg.command.checkout.HgCheckOutCommand;
import org.apache.maven.scm.provider.hg.command.diff.HgDiffCommand;
import org.apache.maven.scm.provider.hg.command.remove.HgRemoveCommand;
import org.apache.maven.scm.provider.hg.command.status.HgStatusCommand;
import org.apache.maven.scm.provider.hg.command.update.HgUpdateCommand;
import org.apache.maven.scm.provider.hg.repository.HgScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.scm.repository.UnknownRepositoryStructure;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Mercurial (HG) is a decentralized revision control system.
 * <a href="http://www.selenic.com/mercurial">http://www.selenic.com/mercurial</a> 
 *
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 * @plexus.component role="org.apache.maven.scm.provider.ScmProvider"
 *                   role-hint="hg"
 */
public class HgScmProvider
    extends AbstractScmProvider
{
    public String getScmSpecificFilename()
    {
        return ".hg";
    }

    public ScmProviderRepository makeProviderScmRepository( String scmSpecificUrl, char delimiter )
        throws ScmRepositoryException
    {
        return new HgScmProviderRepository( scmSpecificUrl );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#makeProviderScmRepository(java.io.File)
     */
    public ScmProviderRepository makeProviderScmRepository( File path )
        throws ScmRepositoryException, UnknownRepositoryStructure
    {
        if ( path == null || !path.isDirectory() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a valid directory." );
        }

        File hgDir = new File( path, ".hg" );

        if ( !hgDir.exists() )
        {
            throw new ScmRepositoryException( path.getAbsolutePath() + " isn't a hg directory." );
        }

        return makeProviderScmRepository( path.getAbsolutePath(), ':' );
    }

    public List validateScmUrl( String scmSpecificUrl, char delimiter )
    {

        List errorMessages = new ArrayList();

        String[] checkCmd = new String[] { HgCommand.CHECK, scmSpecificUrl };
        ScmResult result;
        try
        {
            File tmpDir = new File( System.getProperty( "java.io.tmpdir" ) );
            result = HgUtils.execute( tmpDir, checkCmd );
            if ( !result.isSuccess() )
            {
                errorMessages.add( result.getCommandOutput() );
                errorMessages.add( result.getProviderMessage() );
            }
        }
        catch ( ScmException e )
        {
            errorMessages.add( e.getMessage() );
        }

        return errorMessages;
    }

    public String getScmType()
    {
        return "hg";
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#add(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public AddScmResult add( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgAddCommand command = new HgAddCommand();

        command.setLogger( getLogger() );

        return (AddScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#changelog(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public ChangeLogScmResult changelog( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgChangeLogCommand command = new HgChangeLogCommand();

        command.setLogger( getLogger() );

        return (ChangeLogScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkin(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public CheckInScmResult checkin( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgCheckInCommand command = new HgCheckInCommand();

        command.setLogger( getLogger() );

        return (CheckInScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#checkout(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public CheckOutScmResult checkout( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgCheckOutCommand command = new HgCheckOutCommand();

        command.setLogger( getLogger() );

        return (CheckOutScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#diff(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public DiffScmResult diff( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgDiffCommand command = new HgDiffCommand();

        command.setLogger( getLogger() );

        return (DiffScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#remove(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public RemoveScmResult remove( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgRemoveCommand command = new HgRemoveCommand();

        command.setLogger( getLogger() );

        return (RemoveScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#status(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public StatusScmResult status( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgStatusCommand command = new HgStatusCommand();

        command.setLogger( getLogger() );

        return (StatusScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }

    /**
     * @see org.apache.maven.scm.provider.AbstractScmProvider#update(org.apache.maven.scm.repository.ScmRepository, org.apache.maven.scm.ScmFileSet, org.apache.maven.scm.CommandParameters)
     */
    public UpdateScmResult update( ScmRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        HgUpdateCommand command = new HgUpdateCommand();

        command.setLogger( getLogger() );

        return (UpdateScmResult) command.execute( repository.getProviderRepository(), fileSet, parameters );
    }
}
