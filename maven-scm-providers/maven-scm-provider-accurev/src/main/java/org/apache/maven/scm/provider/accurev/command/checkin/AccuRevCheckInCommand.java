package org.apache.maven.scm.provider.accurev.command.checkin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.checkin.CheckInScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

public class AccuRevCheckInCommand
    extends AbstractAccuRevCommand
{

    public AccuRevCheckInCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        AccuRev accuRev = repository.getAccuRev();

        String message = parameters.getString( CommandParameter.MESSAGE );
        final List<File> promotedFiles = new ArrayList<File>();

        boolean success = false;

        File basedir = fileSet.getBasedir();
        @SuppressWarnings("unchecked")
        List<File> fileList = fileSet.getFileList();

        if ( fileList.isEmpty() )
        {
            // TODO the above test will be matched by a fileset where excludes and includes produce a set with no files.
            // This is
            // NOT the same as a fileset created with only a base directory. Raise maven-scm JIRA for this.
            AccuRevInfo info = accuRev.info( basedir );

            if ( repository.isWorkSpaceRoot( info ) )
            {
                success = accuRev.promoteAll( basedir, message, promotedFiles );
            }
            else
            {
                throw new ScmException( String.format( "Unsupported recursive checkin for %s. Not the workspace root",
                                                       basedir.getAbsolutePath() ) );
            }
        }
        else
        {
            success = accuRev.promote( basedir, fileList, message, promotedFiles );
        }

        Iterator<File> iter = promotedFiles.iterator();
        while ( iter.hasNext() )
        {
            if ( new File( basedir, iter.next().getPath() ).isDirectory() )
            {
                iter.remove();
            }
        }

        if ( success )
        {
            return new CheckInScmResult( accuRev.getCommandLines(), getScmFiles( promotedFiles,
                                                                                 ScmFileStatus.CHECKED_IN ) );
        }
        else
        {
            return new CheckInScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }
    }

    public CheckInScmResult checkIn( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (CheckInScmResult) execute( repository, fileSet, parameters );
    }

}
