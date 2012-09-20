package org.apache.maven.scm.provider.cvslib.command.export;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.export.AbstractExportCommand;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 *
 */
public abstract class AbstractCvsExportCommand
    extends AbstractExportCommand
{
    /** {@inheritDoc} */
    protected ExportScmResult executeExportCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version,
                                                    String outputDirectory )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "export", repository, fileSet );

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            cl.createArg().setValue( "-r" + version.getName() );
        }
        else
        {
            cl.createArg().setValue( "-rHEAD" );
        }

        if ( StringUtils.isNotEmpty( outputDirectory ) )
        {
            cl.createArg().setValue( "-d" );

            cl.createArg().setValue( outputDirectory );
        }

        cl.createArg().setValue( repository.getModule() );

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + cl );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        return executeCvsCommand( cl );
    }

    protected abstract ExportScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
