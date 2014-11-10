package org.apache.maven.scm.provider.integrity.command.add;

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
import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.command.add.AbstractAddCommand;
import org.apache.maven.scm.command.add.AddScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.Sandbox;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;

import java.util.List;

/**
 * MKS Integrity implementation for Maven's AbstractAddCommand
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityAddCommand.java 1.4 2011/08/22 13:06:13EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityAddCommand
    extends AbstractAddCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public AddScmResult executeAddCommand( ScmProviderRepository repository, ScmFileSet fileSet, String message,
                                           boolean binary )
        throws ScmException
    {
        getLogger().info( "Attempting to add new files from directory " + fileSet.getBasedir().getAbsolutePath() );
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;
        Sandbox siSandbox = iRepo.getSandbox();
        String excludes = Sandbox.formatFilePatterns( fileSet.getExcludes() );
        String includes = Sandbox.formatFilePatterns( fileSet.getIncludes() );
        String msg = ( ( null == message || message.length() == 0 ) ? System.getProperty( "message" ) : message );
        List<ScmFile> addedFiles = siSandbox.addNonMembers( excludes, includes, msg );
        if ( siSandbox.getOverallAddSuccess() )
        {
            return new AddScmResult( "si add", addedFiles );
        }
        else
        {
            return new AddScmResult( addedFiles,
                                     new ScmResult( "si add", "There was a problem adding files to the repository", "",
                                                    false ) );
        }
    }

}
