package org.apache.maven.scm.provider.svn.util;

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

import org.codehaus.plexus.util.xml.pull.MXParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParser;
import org.codehaus.plexus.util.xml.pull.XmlPullParserException;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EntriesReader
{
    public List read( Reader reader )
        throws IOException, XmlPullParserException
    {
        XmlPullParser parser = new MXParser();

        parser.setInput( reader );

        List entries = new ArrayList();

        int eventType = parser.getEventType();

        while ( eventType != XmlPullParser.END_DOCUMENT )
        {
            if ( eventType == XmlPullParser.START_TAG )
            {
                while ( parser.nextTag() == XmlPullParser.START_TAG )
                {
                    if ( parser.getName().equals( "entry" ) )
                    {
                        entries.add( parseEntry( "entry", parser ) );
                    }
                    else
                    {
                        parser.nextText();
                    }
                }
            }
            eventType = parser.next();
        }

        return entries;
    }

    private Entry parseEntry( String tagName, XmlPullParser parser )
        throws IOException, XmlPullParserException
    {
        Entry entry = new Entry();

        entry.setName( getTrimmedValue( parser.getAttributeValue( "", "name" ) ) );

        entry.setUrl( getTrimmedValue( parser.getAttributeValue( "", "url" ) ) );

        return entry;
    }

    public int getIntegerValue( String s )
    {
        if ( s != null )
        {
            return Integer.valueOf( s ).intValue();
        }
        return 0;
    }

    public String getTrimmedValue( String s )
    {
        if ( s != null )
        {
            s = s.trim();
        }
        return s;
    }
}
