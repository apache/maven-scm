package org.apache.maven.scm.provider.accurev.command.blame;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.File;
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.blame.BlameLine;
import org.apache.maven.scm.command.blame.BlameScmResult;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRev;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.command.AbstractAccuRevCommand;

/**
 * @author Evgeny Mandrikov
 * @author Grant Gardner
 * @since 1.4
 */
public class AccuRevBlameCommand
    extends AbstractAccuRevCommand
{

    public AccuRevBlameCommand( ScmLogger logger )
    {

        super( logger );
    }

    @Override
    protected BlameScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                                    CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        AccuRev accuRev = repository.getAccuRev();

        File file = new File( parameters.getString( CommandParameter.FILE ) );

        List<BlameLine> lines = accuRev.annotate( fileSet.getBasedir(), file );

        if ( lines != null )
        {
            return new BlameScmResult( accuRev.getCommandLines(), lines );
        }
        else
        {
            return new BlameScmResult( accuRev.getCommandLines(), "AccuRev Error", accuRev.getErrorOutput(), false );
        }

    }

    public BlameScmResult blame( ScmProviderRepository repository, ScmFileSet fileSet, CommandParameters parameters )
        throws ScmException
    {

        return (BlameScmResult) execute( repository, fileSet, parameters );
    }
}
