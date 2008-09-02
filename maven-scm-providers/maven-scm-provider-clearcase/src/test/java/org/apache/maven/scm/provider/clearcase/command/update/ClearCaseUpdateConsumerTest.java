package org.apache.maven.scm.provider.clearcase.command.update;

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
import org.apache.maven.scm.ScmTestCase;
import org.apache.maven.scm.log.DefaultLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Locale;

/**
 * @author <a href="mailto:wim.deblauwe@gmail.com">Wim Deblauwe</a>
 */
public class ClearCaseUpdateConsumerTest
    extends ScmTestCase
{
    private InputStream getResourceAsStream( String name, Locale locale )
    {
        String path;
        if ( locale == null || "".equals( locale.getLanguage() ) )
        {
            path = name;
        }
        else
        {
            String base = name.substring( 0, name.lastIndexOf( '.' ) );
            String ext = name.substring( name.lastIndexOf( '.' ) );
            path = base + "_" + locale.getLanguage() + ext;
            if ( !new File( path ).exists() )
            {
                path = name;
            }
        }

        return super.getResourceAsStream( path );
    }

    private void localizedConsumer( Locale locale )
        throws IOException
    {
        InputStream inputStream = getResourceAsStream( "/clearcase/update/update.txt", locale );

        BufferedReader in = new BufferedReader( new InputStreamReader( inputStream, "UTF-8" ) );

        String s = in.readLine();

        ClearCaseUpdateConsumer consumer = new ClearCaseUpdateConsumer( new DefaultLog() );

        while ( s != null )
        {
            consumer.consumeLine( s );

            s = in.readLine();
        }

        String message = "locale is \"" + locale.getLanguage() + "\"";
        Collection entries = consumer.getUpdatedFiles();

        assertEquals( message + " Wrong number of entries returned", 1, entries.size() );

        ScmFile scmFile = (ScmFile) entries.iterator().next();
        assertEquals( message, "my_vob\\modules\\utils\\utils-logging-jar\\testfile.txt", scmFile.getPath() );
        assertEquals( message, ScmFileStatus.UPDATED, scmFile.getStatus() );
    }

    public void testConsumer()
        throws IOException
    {
        // Locale[] locales = { Locale.US, Locale.JAPANESE };
        Locale[] locales = { Locale.getDefault() };
        Locale defaultLocale = Locale.getDefault();

        for ( int i = 0; i < locales.length; i++ )
        {
            Locale.setDefault( locales[i] );
            localizedConsumer( locales[i] );
        }
        Locale.setDefault( defaultLocale );
    }
}
