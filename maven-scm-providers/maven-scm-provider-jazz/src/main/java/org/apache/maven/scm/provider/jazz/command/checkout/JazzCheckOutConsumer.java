package org.apache.maven.scm.provider.jazz.command.checkout;

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

import org.apache.maven.scm.ScmFile;
import org.apache.maven.scm.ScmFileStatus;
import org.apache.maven.scm.log.ScmLogger;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.jazz.command.consumer.AbstractRepositoryConsumer;
import org.apache.regexp.RE;
import org.apache.regexp.RESyntaxException;

import java.util.ArrayList;
import java.util.List;

/**
 * Consume the output of the scm command for the "load" operation.
 *
 * @author <a href="mailto:ChrisGWarp@gmail.com">Chris Graham</a>
 */
public class JazzCheckOutConsumer
    extends AbstractRepositoryConsumer
{
    private static final String DOWNLOAD_PATTERN = "^Downloading\\s(.*)\\s\\s\\(\\d.*B\\)$";

    /**
     * @see #DOWNLOAD_PATTERN
     */
    private RE downloadRegexp;

    protected String fCurrentDir = "";

    private List<ScmFile> fCheckedOutFiles = new ArrayList<ScmFile>();

    /**
     * Construct the JazzCheckOutCommand consumer.
     *
     * @param repository The repository we are working with.
     * @param logger     The logger to use.
     */
    public JazzCheckOutConsumer( ScmProviderRepository repository, ScmLogger logger )
    {
        super( repository, logger );

        try
        {
            downloadRegexp = new RE( DOWNLOAD_PATTERN );
        }
        catch ( RESyntaxException ex )
        {
            throw new RuntimeException(
                "INTERNAL ERROR: Could not create regexp to parse jazz scm checkout output. This shouldn't happen. Something is probably wrong with the oro installation.",
                ex );
        }
    }

    /**
     * Process one line of output from the execution of the "scm load" command.
     *
     * @param line The line of output from the external command that has been pumped to us.
     * @see org.codehaus.plexus.util.cli.StreamConsumer#consumeLine(java.lang.String)
     */
    public void consumeLine( String line )
    {
        // Examples:
        // Downloading /checkout-test/src/emptyFile.txt (0 B)
        // Downloading /checkout-test/src/folder with spaces/file with spaces.java (24.0 KB)
        if ( downloadRegexp.match( line ) )
        {
            fCheckedOutFiles.add( new ScmFile( downloadRegexp.getParen( 1 ), ScmFileStatus.CHECKED_OUT ) );
        }
    }

    public List<ScmFile> getCheckedOutFiles()
    {
        return fCheckedOutFiles;
    }

}