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
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.command.remove.RemoveScmResult;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.IOException;

/**
 * Mark a set of files for deletion.
 *
 * @author <a href="paul@webotech.co.uk">Paul Mackinlay</a>
 */
@Mojo( name = "remove", aggregator = true )
public class RemoveMojo
    extends AbstractScmMojo
{

    /**
     * The commit message.
     */
    @Parameter( property = "message" )
    private String message;

    /**
     * {@inheritDoc}
     */
    public void execute()
        throws MojoExecutionException
    {
        super.execute();
        try
        {
            ScmRepository repository = getScmRepository();
            RemoveScmResult result = getScmManager().remove( repository, getFileSet(), message );
            checkResult( result );
        }
        catch ( IOException | ScmException e )
        {
            throw new MojoExecutionException( "Cannot run remove command : " + e.getMessage(), e );
        }
    }
}
