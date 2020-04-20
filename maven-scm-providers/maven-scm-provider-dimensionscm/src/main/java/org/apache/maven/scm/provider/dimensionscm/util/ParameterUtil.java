package org.apache.maven.scm.provider.dimensionscm.util;

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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Methods for getting parameters for SCM operations.
 */
public class ParameterUtil
{

    /**
     * System property (boolean value) for enabling or disabling whether all foreign content
     * is allowed to be delivered to a stream.
     */
    private static final String SYSTEM_ALL = "dimensionscm.all";

    /**
     * System property (boolean value) for enabling or disabling additions on check-in.
     */
    private static final String SYSTEM_ADD = "dimensionscm.add";

    /**
     * System property for passing a project/stream relative location through
     * to a dimensions command.
     */
    private static final String SYSTEM_RELATIVE_LOCATION = "dimensionscm.relativeLocation";

    /**
     * System property for passing a request or a comma separated request list through
     * to a dimensions command.
     */
    private static final String SYSTEM_REQUESTS = "dimensionscm.requests";

    /**
     * System property for passing a baseline through to a dimensions command.
     */
    private static final String SYSTEM_BASELINE = "dimensionscm.baseline";

    /**
     * System property for passing a specific object type through to a dimensions command.
     */
    private static final String SYSTEM_TYPE = "dimensionscm.type";

    /**
     * System property for passing user defined attributes (in json format) through
     * to a dimensions command.
     */
    private static final String SYSTEM_ATTRIBUTES = "dimensionscm.attributes";

    /**
     * System property for passing user defined attributes (in org.json format) through
     * to a dimensions command.
     */
    private static final String SYSTEM_FILENAME = "dimensionscm.filename";

    /**
     * System property for message.
     */
    private static final String SYSTEM_MESSAGE = "message";

    /**
     * System property for include files.
     */
    private static final String SYSTEM_INCLUDES = "includes";

    /**
     * System property for exclude files.
     */
    private static final String SYSTEM_EXCLUDES = "excludes";

    /**
     * Returns whether all foreign content is enabled when delivering to a stream.
     *
     * @return <code>true</code> if all foreign content is enabled when delivering to a stream.
     */
    public static boolean getSystemAll( boolean defaultValue )
    {
        String all = System.getProperty( SYSTEM_ALL );
        return StringUtils.isBlank( all ) ? defaultValue : Boolean.parseBoolean( all );
    }

    /**
     * Returns whether additions are enabled when calculating work area differences.
     *
     * @return <code>true</code> if additions are enabled when calculating work area differences.
     */
    public static boolean getSystemAdd( boolean defaultValue )
    {
        String add = System.getProperty( SYSTEM_ADD );
        return StringUtils.isBlank( add ) ? defaultValue : Boolean.parseBoolean( add );
    }

    /**
     * Returns a project/stream relative location string extracted from system properties.
     *
     * @return Project/stream relative location string extracted from system properties.
     */
    public static String getSystemRelativeLocation()
    {
        return System.getProperty( SYSTEM_RELATIVE_LOCATION );
    }

    /**
     * Returns a list of requests extracted from system properties.
     *
     * @return List of requests extracted from system properties.
     */
    public static String getSystemRequests()
    {
        return System.getProperty( SYSTEM_REQUESTS );
    }

    /**
     * Returns a baseline string extracted from system properties.
     *
     * @return Baseline string extracted from system properties.
     */
    public static String getSystemBaseline( String product ) throws IOException
    {
        String systemBaseline = System.getProperty( SYSTEM_BASELINE );
        if ( StringUtils.isBlank( systemBaseline ) || systemBaseline.contains( ":" ) )
        {
            return systemBaseline;
        }

        if ( StringUtils.isBlank( product ) )
        {
            throw new IOException( "Product name required to build baseline specification." );
        }
        return String.format( "%s:%s", product, systemBaseline );
    }

    public static String getSystemFilename()
    {
        return System.getProperty( SYSTEM_FILENAME );
    }

    public static String getSystemBaselineType()
    {
        return System.getProperty( SYSTEM_TYPE );
    }

    public static String getSystemAttributes()
    {
        String attributes = System.getProperty( SYSTEM_ATTRIBUTES );

        if ( StringUtils.isBlank( attributes ) )
        {
            return attributes;
        }

        JsonObject jsonObject = new JsonParser().parse( attributes ).getAsJsonObject();
        List<String> attributeList = new ArrayList<>();

        for ( String key : jsonObject.keySet() )
        {
            JsonElement value = jsonObject.get( key );
            StringBuilder attribute = new StringBuilder();
            attribute.append( key ).append( "=" );
            if ( value.isJsonArray() )
            {
                attribute.append( String.format( "%s", value.getAsJsonArray().toString() ) );
            }
            else
            {
                attribute.append( String.format( "\"%s\"", value.getAsString() ) );
            }
            attributeList.add( attribute.toString() );
        }

        return StringUtils.join( attributeList, "," );
    }

    public static String getSystemMessage()
    {
        return System.getProperty( SYSTEM_MESSAGE );
    }

    public static String getSystemIncludes()
    {
        return System.getProperty( SYSTEM_INCLUDES );
    }

    public static String getSystemExcludes()
    {
        return System.getProperty( SYSTEM_EXCLUDES );
    }
}
