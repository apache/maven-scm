package org.apache.maven.scm.provider.cvslib.command.update;

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

import java.io.File;
import java.util.Iterator;
import java.util.List;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.command.update.AbstractUpdateCommand;
import org.apache.maven.scm.command.update.UpdateScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.cvslib.command.CvsCommand;
import org.apache.maven.scm.provider.cvslib.command.CvsCommandUtils;
import org.apache.maven.scm.provider.cvslib.repository.CvsScmProviderRepository;
import org.codehaus.plexus.util.StringUtils;
import org.codehaus.plexus.util.cli.Commandline;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse </a>
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 * @author Olivier Lamy
 *
 */
public abstract class AbstractCvsUpdateCommand
    extends AbstractUpdateCommand
    implements CvsCommand
{
    /** {@inheritDoc} */
    public UpdateScmResult executeUpdateCommand( ScmProviderRepository repo, ScmFileSet fileSet, ScmVersion version )
        throws ScmException
    {
        CvsScmProviderRepository repository = (CvsScmProviderRepository) repo;

        Commandline cl = CvsCommandUtils.getBaseCommand( "update", repository, fileSet, false );

        cl.createArg().setValue( "-d" );

        if ( version != null && StringUtils.isNotEmpty( version.getName() ) )
        {
            cl.createArg().setValue( "-r" + version.getName() );
        }
        
        List<File> files = fileSet.getFileList();
        if ( !files.isEmpty() )
        {
            Iterator<File> fileIterator = files.iterator();
            while ( fileIterator.hasNext() )
            {
                cl.createArg().setValue( ( (File) fileIterator.next() ).getPath() );
            }
        }

        if ( getLogger().isInfoEnabled() )
        {
            getLogger().info( "Executing: " + cl );
            getLogger().info( "Working directory: " + cl.getWorkingDirectory().getAbsolutePath() );
        }

        return executeCvsCommand( cl );
    }

    protected abstract UpdateScmResult executeCvsCommand( Commandline cl )
        throws ScmException;
}
