package org.apache.maven.scm.provider.svn.util;

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
