package org.apache.maven.scm.plugin;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.command.Command;
import org.apache.maven.scm.command.checkout.CheckOutScmResult;
import org.apache.maven.scm.repository.ScmRepository;

import java.io.File;

/**
 * @goal checkout
 * @description Check out a project
 *
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class CheckoutMojo
    extends AbstractScmMojo
{
    /**
     * @parameter expression="${connectionUrl}
     * @required
     */
    private String connectionUrl;

    /**
     * @parameter expression="${branch}
     */
    private String branch;

    /**
     * @parameter expression="${tag}
     */
    private String tag;

    public void execute()
        throws MojoExecutionException
    {
        try
        {
            getScmManager().addListener( new DefaultLog( getLog() ) );

            ScmRepository repository = getScmManager().makeScmRepository( connectionUrl );

            String currentTag = null;

            if ( branch != null )
            {
                currentTag = branch;
            }

            if ( tag != null )
            {
                currentTag = tag;
            }

            CheckOutScmResult result = getScmManager().checkOut( repository, new ScmFileSet( getWorkingDirectory() ), currentTag );

            if ( !result.isSuccess() )
            {
                getLog().error( result.getCommandOutput() );
            }
        }
        catch ( Exception e )
        {
            throw new MojoExecutionException( "Cannot run checkout command : ", e );
        }
    }
}

