package org.apache.maven.scm.provider.accurev.util;

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

import java.util.HashMap;
import java.util.Map;

/**
 * @author ggardner
 */
public final class QuotedPropertyParser
{

    private QuotedPropertyParser()
    {

    }

    public static Map<String, String> parse( CharSequence seq )
    {
        Map<String, String> hashMap = new HashMap<String, String>();

        parse( seq, hashMap );
        return hashMap;
    }

    public static void parse( CharSequence string, Map<? super String, ? super String> propertyMap )
    {

        QuotedParseState state = QuotedParseState.KEY;
        char quote = '\0';
        StringBuilder buffer = new StringBuilder();
        String propertyKey = "";

        int i = 0; // where we are up to in the scan
        int pos = 0; // where we have consumed into the buffer
        while ( i < string.length() )
        {
            char current = string.charAt( i );
            switch ( state )
            {
                case KEY:
                    switch ( current )
                    {
                        case '"':
                        case '\'':
                            quote = current;
                            state = QuotedParseState.IN_QUOTED_KEY;
                            if ( i >= pos )
                            {
                                buffer.append( string.subSequence( pos, i ) );
                            }
                            pos = i + 1;
                            break;
                        case '=':
                            if ( i >= pos )
                            {
                                buffer.append( string.subSequence( pos, i ) );
                            }
                            propertyKey = buffer.toString();
                            buffer = new StringBuilder();
                            state = QuotedParseState.VALUE;
                            pos = i + 1;
                            break;
                        default:
                    }
                    break;

                case VALUE:
                    switch ( current )
                    {
                        case '"':
                        case '\'':
                            quote = current;
                            state = QuotedParseState.IN_QUOTED_VALUE;
                            if ( i >= pos )
                            {
                                buffer.append( string.subSequence( pos, i ) );
                            }
                            pos = i + 1;
                            break;
                        case '&':
                            if ( i >= pos )
                            {
                                buffer.append( string.subSequence( pos, i ) );
                            }
                            propertyMap.put( propertyKey, buffer.toString() );
                            pos = i + 1;
                            buffer = new StringBuilder();
                            state = QuotedParseState.KEY;
                            break;
                        default:
                    }

                    break;
                case IN_QUOTED_KEY:
                case IN_QUOTED_VALUE:
                    if ( current == quote )
                    {
                        state =
                            ( state == QuotedParseState.IN_QUOTED_KEY ) ? QuotedParseState.KEY : QuotedParseState.VALUE;
                        if ( i >= pos )
                        {
                            buffer.append( string.subSequence( pos, i ) );
                        }
                        pos = i + 1;
                    }
                    break;
                default:
                    break;
            }

            i++;
        }

        if ( state == QuotedParseState.VALUE )
        {
            if ( i >= pos )
            {
                buffer.append( string.subSequence( pos, i ) );
            }
            propertyMap.put( propertyKey, buffer.toString() );
        }
    }

    /**
     * 
     */
    // Has to be down here to avoid a QDOX exception
    public enum QuotedParseState
    {
        KEY, IN_QUOTED_KEY, IN_QUOTED_VALUE, VALUE
    }

}
