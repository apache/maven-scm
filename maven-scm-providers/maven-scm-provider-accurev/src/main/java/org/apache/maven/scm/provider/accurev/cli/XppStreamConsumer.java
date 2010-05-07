package org.apache.maven.scm.provider.accurev.cli;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.maven.scm.log.ScmLogger;
import org.codehaus.plexus.util.cli.StreamConsumer;
import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

/**
 * This class is required because Plexus command line won't let you get to the process stream output process.
 * 
 * @author ggardner
 */
public abstract class XppStreamConsumer
    extends Thread
    implements StreamConsumer

{
    public ScmLogger getLogger()
    {
        return logger;
    }

    private PipedWriter writer;

    private PipedReader reader;

    private XmlPullParser parser = new MXParser();

    private volatile boolean complete = false;

    private ScmLogger logger;

    public XppStreamConsumer( ScmLogger logger )
    {

        super();
        this.logger = logger;
        writer = new PipedWriter();
        try
        {
            reader = new PipedReader( writer );
            parser.setInput( reader );
        }
        catch ( Exception e )
        {
            logger.error( "Exception initialising pipe", e );
        }

        this.start();

    }

    public final void consumeLine( String line )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Consumed: " + line );
        }

        try
        {
            writer.append( line );
            writer.flush();
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "error pumping line to pipe", e );
        }

    }

    @Override
    public void run()
    {

        try
        {
            parse( parser );
        }
        catch ( Exception e )
        {
            caughtParseException( e );
        }
        finally
        {
            synchronized ( this )
            {
                complete = true;
                this.notifyAll();
            }
        }
    }

    protected void caughtParseException( Exception e )
    {
        logger.warn( "Exception parsing input", e );

    }

    protected void parse( XmlPullParser p )
        throws XmlPullParserException, IOException
    {
        List<String> tagPath = new ArrayList<String>();
        int eventType = p.getEventType();
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "Event " + eventType );
        }

        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            int lastIndex = tagPath.size() - 1;
            String tagName;
            switch ( eventType )
            {
                case XmlPullParser.START_DOCUMENT:

                    break;

                case XmlPullParser.START_TAG:
                    tagName = p.getName();
                    if ( tagName != null )
                    {
                        tagPath.add( tagName );
                        int attributeCount = p.getAttributeCount();
                        Map<String, String> attributes = new HashMap<String, String>( Math.max( attributeCount, 0 ) );
                        for ( int i = 0; i < attributeCount; i++ )
                        {
                            attributes.put( p.getAttributeName( i ), p.getAttributeValue( i ) );
                        }

                        startTag( tagPath, attributes );
                    }
                    break;

                case XmlPullParser.TEXT:
                    if ( !p.isWhitespace() )
                    {
                        String text = p.getText();
                        text( tagPath, text );
                    }
                    break;

                case XmlPullParser.END_TAG:
                    tagName = p.getName();

                    if ( lastIndex < 0 || !tagName.equals( tagPath.get( lastIndex ) ) )
                    {
                        logger.warn( "Bad tag path: " + Arrays.toString( tagPath.toArray() ) );
                    }
                    endTag( tagPath );
                    tagPath.remove( lastIndex );
                    break;

                default:
                    logger.warn( "Unexpected event type " + eventType );
                    break;
            }
            p.next();
            eventType = p.getEventType();
            if ( logger.isDebugEnabled() )
            {
                logger.debug( "Event " + eventType );
            }
        }
    }

    /**
     * close the input and wait for parsing to complete
     */
    public void waitComplete()
    {

        //Can close the writer here because CommandLineUtils waits for the StreamPumpers to finish.
        if ( writer != null )
        {
            try
            {
                writer.close();
            }
            catch ( IOException e )
            {
                logger.warn( e );
            }

        }
        while ( !isComplete() )
        {

            synchronized ( this )
            {
                try
                {
                    if ( !isComplete() )
                    {
                        this.wait( 2000 );
                    }
                }
                catch ( Exception e )
                {
                    logger.warn( e );
                }

            }
        }
    }

    private boolean isComplete()
    {
        return complete;
    }

    protected void startTag( List<String> tagPath, Map<String, String> attributes )
    {
        if ( logger.isDebugEnabled() )
        {
            String tagName = getTagName( tagPath );
            logger.debug( "START_TAG: " + tagName + "(" + attributes.size() + ")" );
        }
    }

    protected static String getTagName( List<String> tagPath )
    {
        return tagPath.size() == 0 ? null : tagPath.get( tagPath.size() - 1 );

    }

    protected void endTag( List<String> tagPath )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "END_TAG: " + getTagName( tagPath ) );
        }

    }

    protected void text( List<String> tagPath, String text )
    {
        if ( logger.isDebugEnabled() )
        {
            logger.debug( "TEXT: " + text );
        }

    }

}
