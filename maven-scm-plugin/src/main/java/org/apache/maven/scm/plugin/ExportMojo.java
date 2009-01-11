package org.apache.maven.scm.plugin;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import java.io.File;
import java.io.IOException;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.export.ExportScmResult;
import org.apache.maven.scm.repository.ScmRepository;
import org.codehaus.plexus.util.FileUtils;

/**
 * Get a fresh exported copy of the latest source from the configured scm url.
 * 
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 * @goal export
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
     * @parameter expression="${exportDirectory}" default-value="${project.build.directory}/export
     * @required
     */
    private File exportDirectory;
    
    /**
     * Skip export if exportDirectory exists.
     *
     * @parameter expression="${skipExportIfExists}" default-value="false"
     */
    private boolean skipExportIfExists = false;
    

    /** {@inheritDoc} */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();

        if ( this.skipExportIfExists && this.exportDirectory.isDirectory()  )
        {
            return;
        }
        
        export();
    }

    protected File getExportDirectory()
    {
        return this.exportDirectory;
    }

    public void setExportDirectory( File exportDirectory )
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
                if ( this.exportDirectory.exists() )
                {
                    this.getLog().info( "Removing " + this.exportDirectory );

                    FileUtils.deleteDirectory( this.exportDirectory );
                }
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Cannot remove " + getExportDirectory() );
            }

            if ( !this.exportDirectory.mkdirs() )
            {
                throw new MojoExecutionException( "Cannot create " + this.exportDirectory );
            }                
            
            ExportScmResult result = getScmManager().export( repository,
                                                             new ScmFileSet( this.exportDirectory.getAbsoluteFile() ),
                                                             getScmVersion( scmVersionType, scmVersion ) );

            checkResult( result );
        }
        catch ( ScmException e )
        {
            throw new MojoExecutionException( "Cannot run export command : ", e );
        }
    }
}
