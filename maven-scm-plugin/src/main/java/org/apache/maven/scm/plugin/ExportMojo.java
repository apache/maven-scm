package org.apache.maven.scm.plugin;

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

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;
import org.codehaus.plexus.util.StringUtils;

import java.io.File;
import java.io.IOException;

/**
 * Get a fresh exported copy of the latest source from the configured scm url.
 *
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal export
 * @description Export a project
 * @requiresProject false
 */
public class ExportMojo
    extends AbstractScmMojo
{
    /**
     * The version type (branch/tag/revision) of scmVersion.
     *
     * @parameter expression="${scmVersionType}"
     */
    private String scmVersionType;

    /**
     * The version (revision number/branch name/tag name).
     *
     * @parameter expression="${scmVersion}"
     */
    private String scmVersion;

    /**
     * The directory to export the sources to.
     *
     * @parameter expression="${exportDirectory}"
     */
    private String exportDirectory;

    public void execute()
        throws MojoExecutionException
    {
        export();
    }

    protected String getExportDirectory()
    {
        return this.exportDirectory;
    }

    public void setExportDirectory( String exportDirectory )
    {
        this.exportDirectory = exportDirectory;
    }

    protected void export()
        throws MojoExecutionException
    {
        try
        {
            ScmRepository repository = getScmRepository();

            try
            {
                if ( StringUtils.isNotEmpty( getExportDirectory() ) )
                {
                    File f = new File( getExportDirectory() );
                    if ( f.exists() )
                    {
                        this.getLog().info( "Removing " + getExportDirectory() );

                        FileUtils.deleteDirectory( getExportDirectory() );
                    }

                    f.mkdirs();
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Cannot remove " + getExportDirectory() );
            }

            ExportScmResult result = getScmManager().getProviderByRepository( repository ).export( repository,
                                                                                                   new ScmFileSet(
                                                                                                       new File(
                                                                                                           getExportDirectory() ).getAbsoluteFile() ),
                                                                                                   getScmVersion(
                                                                                                       scmVersionType,
                                                                                                       scmVersion ),
                                                                                                   getExportDirectory() );

            checkResult( result );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run export command : ", e );
        }
    }
}
