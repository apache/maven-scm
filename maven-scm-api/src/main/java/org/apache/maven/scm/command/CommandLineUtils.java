package org.apache.maven.scm.command;

/*
 * Copyright 2001-2004 The Apache Software Foundation.
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

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l </a>
 * @version $Id$
 */
public abstract class CommandLineUtils
{/*
    public static class StringStreamConsumer
        implements StreamConsumer
    {
        private StringBuffer string = new StringBuffer();

        private String ls = System.getProperty( "line.separator" );

        public void consumeLine( String line )
        {
            string.append( line + ls );
        }

        public String getOutput()
        {
            return string.toString();
        }
    }

    public static int executeCommandLine( Commandline cl, StreamConsumer systemOut, StreamConsumer systemErr )
        throws ScmException
    {
        if ( cl == null )
        {
            throw new ScmException( "The command line cannot be null." );
        }

        Process p;

        System.out.println( "Executing: " + cl );
        System.out.println( "pwd: " + cl.getWorkingDirectory().getAbsolutePath() );

        try
        {
            p = cl.execute();
        }
        catch( IOException ex )
        {
            throw new ScmException( "Error while executing external command.", ex );
        }

        StreamPumper inputPumper = new StreamPumper( p.getInputStream(), systemOut );

        StreamPumper errorPumper = new StreamPumper( p.getErrorStream(), systemErr );

        inputPumper.start();

        errorPumper.start();

        try
        {
            int returnValue = p.waitFor();

            while( !inputPumper.isDone() )
            {
                Thread.sleep( 0 );
            }

            while( !errorPumper.isDone() )
            {
                Thread.sleep( 0 );
            }

            inputPumper.close();

            errorPumper.close();

            return returnValue;
        }
        catch( InterruptedException ex )
        {
            throw new ScmException( "Error while executing external command.", ex );
        }
    }
*/
}
