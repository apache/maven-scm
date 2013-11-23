package org.apache.maven.scm.provider.integrity.command.tag;

/**
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

import com.mks.api.response.APIException;
import com.mks.api.response.Response;
import com.mks.api.response.WorkItem;
import groovy.lang.Binding;
import groovy.lang.GroovyShell;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmTagParameters;
import org.apache.maven.scm.command.tag.AbstractTagCommand;
import org.apache.maven.scm.command.tag.TagScmResult;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.integrity.ExceptionHandler;
import org.apache.maven.scm.provider.integrity.Project;
import org.apache.maven.scm.provider.integrity.repository.IntegrityScmProviderRepository;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * MKS Integrity implementation of Maven's AbstractTagCommand
 * <br>This command will execute a 'si checkpoint' command using a groovy
 * script for evaluating the tag (label) name
 *
 * @author <a href="mailto:cletus@mks.com">Cletus D'Souza</a>
 * @version $Id: IntegrityTagCommand.java 1.4 2011/08/22 13:06:38EDT Cletus D'Souza (dsouza) Exp  $
 * @since 1.6
 */
public class IntegrityTagCommand
    extends AbstractTagCommand
{
    /**
     * {@inheritDoc}
     */
    @Override
    public TagScmResult executeTagCommand( ScmProviderRepository repository, ScmFileSet fileSet, String tagName,
                                           ScmTagParameters scmTagParameters )
        throws ScmException
    {
        getLogger().info(
            "Attempting to checkpoint project associated with sandbox " + fileSet.getBasedir().getAbsolutePath() );
        TagScmResult result;
        String message = scmTagParameters.getMessage();
        IntegrityScmProviderRepository iRepo = (IntegrityScmProviderRepository) repository;

        try
        {
            // First validate the checkpoint label string by evaluating the groovy script
            String chkptLabel = evalGroovyExpression( tagName );
            Project.validateTag( chkptLabel );
            String msg = ( ( null == message || message.length() == 0 ) ? System.getProperty( "message" ) : message );
            // Get information about the Project
            Project siProject = iRepo.getProject();
            // Ensure this is not a build project configuration
            if ( !siProject.isBuild() )
            {
                Response res = siProject.checkpoint( msg, chkptLabel );
                int exitCode = res.getExitCode();
                boolean success = ( exitCode == 0 ? true : false );
                WorkItem wi = res.getWorkItem( siProject.getConfigurationPath() );
                String chkpt = wi.getResult().getField( "resultant" ).getItem().getId();
                getLogger().info(
                    "Successfully checkpointed project " + siProject.getConfigurationPath() + " with label '"
                        + chkptLabel + "', new revision is " + chkpt );
                result =
                    new TagScmResult( res.getCommandString(), wi.getResult().getMessage(), "Exit Code: " + exitCode,
                                      success );
            }
            else
            {
                getLogger().error(
                    "Cannot checkpoint a build project configuration: " + siProject.getConfigurationPath() + "!" );
                result =
                    new TagScmResult( "si checkpoint", "Cannot checkpoint a build project configuration!", "", false );
            }
        }
        catch ( CompilationFailedException cfe )
        {
            getLogger().error( "Groovy Compilation Exception: " + cfe.getMessage() );
            result = new TagScmResult( "si checkpoint", cfe.getMessage(), "", false );
        }
        catch ( APIException aex )
        {
            ExceptionHandler eh = new ExceptionHandler( aex );
            getLogger().error( "MKS API Exception: " + eh.getMessage() );
            getLogger().info( eh.getCommand() + " exited with return code " + eh.getExitCode() );
            result = new TagScmResult( eh.getCommand(), eh.getMessage(), "Exit Code: " + eh.getExitCode(), false );
        }
        catch ( Exception e )
        {
            getLogger().error( "Failed to checkpoint project! " + e.getMessage() );
            result = new TagScmResult( "si checkpoint", e.getMessage(), "", false );
        }
        return result;
    }

    public String evalGroovyExpression( String expression )
    {
        Binding binding = new Binding();
        binding.setVariable( "env", System.getenv() );
        binding.setVariable( "sys", System.getProperties() );
        CompilerConfiguration config = new CompilerConfiguration();
        GroovyShell shell = new GroovyShell( binding, config );
        Object result = shell.evaluate( "return \"" + expression + "\"" );
        if ( result == null )
        {
            return "";
        }
        else
        {
            return result.toString().trim();
        }
    }
}
