/********************************************************************************
 * CruiseControl, a Continuous Integration Toolkit
 * Copyright (c) 2001-2003, ThoughtWorks, Inc.
 * 651 W Washington Ave. Suite 500
 * Chicago, IL 60661 USA
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 *     + Redistributions of source code must retain the above copyright
 *       notice, this list of conditions and the following disclaimer.
 *
 *     + Redistributions in binary form must reproduce the above
 *       copyright notice, this list of conditions and the following
 *       disclaimer in the documentation and/or other materials provided
 *       with the distribution.
 *
 *     + Neither the name of ThoughtWorks, Inc., CruiseControl, nor the
 *       names of its contributors may be used to endorse or promote
 *       products derived from this software without specific prior
 *       written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 ********************************************************************************/

/* ====================================================================
 * Copyright 2003-2004 The Apache Software Foundation.
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
 * ====================================================================
 */

package org.apache.maven.scm.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Class to pump the error stream during Process's runtime. Copied from
 * the Ant built-in task.
 *
 * @since  June 11, 2001
 * @author <a href="mailto:fvancea@maxiq.com">Florin Vancea</a>
 * @author <a href="mailto:pj@thoughtworks.com">Paul Julius</a>
 */
public class StreamPumper extends Thread
{

    private BufferedReader in;
    private StreamConsumer consumer = null;
    private PrintWriter out = new PrintWriter( System.out );
    private static final int SIZE = 1024;

    public StreamPumper( InputStream in, PrintWriter writer )
    {
        this( in );
        out = writer;
    }

    public StreamPumper( InputStream in )
    {
        this.in = new BufferedReader( new InputStreamReader( in ), SIZE );
    }

    public StreamPumper( InputStream in, StreamConsumer consumer )
    {
        this( in );
        this.consumer = consumer;
    }

    public StreamPumper( InputStream in, PrintWriter writer,
                         StreamConsumer consumer )
    {
        this( in );
        this.out = writer;
        this.consumer = consumer;
    }

    public void run()
    {
        try
        {
            String s = in.readLine();
            while ( s != null )
            {
                consumeLine( s );
                if (out != null)
                {
                    out.println( s );
                    out.flush();
                }

                s = in.readLine();
            }
        }
        catch ( IOException e )
        {
            // do nothing
        }
        finally
        {
            try
            {
                in.close();
            }
            catch ( IOException e )
            {
                // do nothing
            }
        }
    }

    public void flush()
    {
        out.flush();
    }

    public void close()
    {
        flush();
        out.close();
    }

    private void consumeLine( String line )
    {
        if ( consumer != null )
        {
            consumer.consumeLine( line );
        }
    }
}
