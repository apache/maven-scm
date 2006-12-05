package org.apache.maven.scm.provider.synergy.consumer;

/*
 * Copyright 2001-2006 The Apache Software Foundation.
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

import java.util.ArrayList;
import java.util.List;

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;

/**
 * 
 * @author <a href="mailto:julien.henry@capgemini.com">Julien Henry</a>
 */
public class SynergyGetWorkingFilesConsumer implements StreamConsumer
{
    private ScmLogger logger;

    private List files = new ArrayList();

    public static String OUTPUT_FORMAT = "%name";

    public SynergyGetWorkingFilesConsumer( ScmLogger logger )
    {
        this.logger = logger;
    }

    public void consumeLine( String line )
    {
        logger.debug( line );
        if ( !line.trim().equals( "" ) )
        {
            files.add( line );
        }
    }

    public List getFiles()
    {
        return files;
    }
}
