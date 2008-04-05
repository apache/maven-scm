package org.apache.maven.scm.provider.git;

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

import org.codehaus.plexus.util.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @version $Id$
 */
public class GitConfigFileReader
{
    private File configDirectory;

    public File getConfigDirectory()
    {
        if ( configDirectory == null )
        {
            
            File confDir = new File( ".git" );
            
            if ( confDir.exists() ) 
            {
                configDirectory = confDir;
            }
            else 
            {
                confDir = new File( "." );
                
                if ( confDir.exists() ) 
                {
                    configDirectory = confDir;
                }
            }
            
        }

        return configDirectory;
    }

    public void setConfigDirectory( File configDirectory )
    {
        this.configDirectory = configDirectory;
    }

    public String getProperty( String group, String propertyName )
    {
        List lines = getConfLines();

        boolean inGroup = false;
        for ( Iterator i = lines.iterator(); i.hasNext(); )
        {
            String line = ( (String) i.next() ).trim();

            if ( !inGroup )
            {
                if ( ( "[" + group + "]" ).equals( line ) )
                {
                    inGroup = true;
                }
            }
            else
            {
                if ( line.startsWith( "[" ) && line.endsWith( "]" ) )
                {
                    //a new group start
                    return null;
                }

                if ( line.startsWith( "#" ) )
                {
                    continue;
                }
                if ( line.indexOf( '=' ) < 0 )
                {
                    continue;
                }

                String property = line.substring( 0, line.indexOf( '=' ) ).trim();

                if ( !property.equals( propertyName ) )
                {
                    continue;
                }

                String value = line.substring( line.indexOf( '=' ) + 1 );
                return value.trim();
            }
        }

        return null;
    }

    /**
     * Load the git config file
     *
     * @return the list of all lines
     */
    private List getConfLines()
    {
        List lines = new ArrayList();

        BufferedReader reader = null;

        try
        {
            if ( getConfigDirectory().exists() )
            {
                reader = new BufferedReader( new FileReader( new File( getConfigDirectory(), "config" ) ) );
                String line;
                while ( ( line = reader.readLine() ) != null )
                {
                    if ( !line.startsWith( "#" ) && StringUtils.isNotEmpty( line ) )
                    {
                        lines.add( line );
                    }
                }
            }
        }
        catch ( IOException e )
        {
            lines.clear();
        }
        finally
        {
            if ( reader != null )
            {
                try
                {
                    reader.close();
                }
                catch ( IOException e )
                {
                    //Do nothing
                }
                reader = null;
            }
        }

        return lines;
    }
}
