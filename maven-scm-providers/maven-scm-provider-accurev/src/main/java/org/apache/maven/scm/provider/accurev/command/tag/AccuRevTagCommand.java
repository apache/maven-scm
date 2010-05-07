package org.apache.maven.scm.provider.accurev.command.tag;

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

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevInfo;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

public class AccuRevTagCommand
    extends AbstractAccuRevCommand
{

    public AccuRevTagCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        ArrayList<File> taggedFiles = new ArrayList<File>();

        AccuRev accuRev = repository.getAccuRev();

        String tagName = parameters.getString( CommandParameter.TAG_NAME );

        tagName = repository.getExtendedTagName( tagName );

        File basedir = fileSet.getBasedir();
        boolean success = true;

        AccuRevInfo info = accuRev.info( basedir );

        success = accuRev.mksnap( tagName, info.getBasis() );
        if ( success )
        {
            success = accuRev.statTag( tagName, taggedFiles );
        }

        if ( success )
        {
            return new TagScmResult( accuRev.getCommandLines(), getScmFiles( taggedFiles, ScmFileStatus.TAGGED ) );
        }
        else
        {
            return new TagScmResult( accuRev.getCommandLines(), "AccuRev error", accuRev.getErrorOutput(), false );
        }
    }

    public TagScmResult tag( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {
        return (TagScmResult) execute( repository, fileSet, parameters );
    }

}
