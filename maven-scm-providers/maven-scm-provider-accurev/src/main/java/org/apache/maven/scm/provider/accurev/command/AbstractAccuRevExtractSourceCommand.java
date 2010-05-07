package org.apache.maven.scm.provider.accurev.command;

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
import java.util.List;

import org.apache.maven.scm.CommandParameter;
import org.apache.maven.scm.CommandParameters;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.accurev.AccuRevException;
import org.apache.maven.scm.provider.accurev.AccuRevScmProviderRepository;
import org.apache.maven.scm.provider.accurev.AccuRevVersion;

public abstract class AbstractAccuRevExtractSourceCommand
    extends AbstractAccuRevCommand
{

    public AbstractAccuRevExtractSourceCommand( ScmLogger logger )
    {
        super( logger );
    }

    @Override
    protected ScmResult executeAccurevCommand( AccuRevScmProviderRepository repository, ScmFileSet fileSet,
                                               CommandParameters parameters )
        throws ScmException, AccuRevException
    {

        List<File> checkedOutFiles = new ArrayList<File>();

        ScmVersion scmVersion = parameters.getScmVersion( CommandParameter.SCM_VERSION, null );

        AccuRevVersion accuRevVersion = repository.getAccuRevVersion( scmVersion );

        String basisStream = accuRevVersion.getBasisStream();
        String transactionId = accuRevVersion.getTimeSpec();

        File basedir = fileSet.getBasedir();
        String outputDirectory = parameters.getString( CommandParameter.OUTPUT_DIRECTORY, null );
        if ( outputDirectory != null )
        {
            basedir = new File( outputDirectory );
        }

        if ( !basedir.exists() )
        {
            basedir.mkdirs();
        }

        if ( !basedir.isDirectory() || basedir.list().length != 0 )
        {
            throw new ScmException( "Checkout directory " + basedir.getAbsolutePath() + " not empty" );
        }

        boolean success = extractSource( repository, basedir, basisStream, transactionId, checkedOutFiles );

        List<ScmFile> scmFiles = getScmFiles( checkedOutFiles, ScmFileStatus.CHECKED_OUT );

        return getScmResult( repository, scmFiles, success );

    }

    protected abstract ScmResult getScmResult( AccuRevScmProviderRepository repository, List<ScmFile> scmFiles,
                                               boolean success );

    protected abstract boolean extractSource( AccuRevScmProviderRepository repository, File basedir,
                                              String basisStream, String transactionId, List<File> checkedOutFiles )
        throws AccuRevException;
}