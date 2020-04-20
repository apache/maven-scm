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

import org.apache.maven.scm.provider.dimensionscm.constants.DimensionsConstants;
import org.apache.maven.scm.repository.ScmRepositoryException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utilities for working with Dimensions CM URLs.
 */
public class UrlUtil
{

    private static final String SCM_CONNECTION_STR_PATTERN = "//(?:([^:]*)(?::([^@]*))?@)?([^:/]+)"
        + "(?::([\\d]+|(?i)\\bcac\\b))?/([^/]+)(?:@([^/]+)?)(?:[/]?)([^/:]*)"
        + "(?:[:])([^/]*)(?:[/]?)(.*)";

    private static final Pattern SCM_CONNECTION_PATTERN = Pattern.compile( SCM_CONNECTION_STR_PATTERN );

    public static boolean isValidUrl( String url )
    {
        Matcher scmMatcher = SCM_CONNECTION_PATTERN.matcher( url );
        return scmMatcher.find();
    }

    public static Matcher getMatcher( String url ) throws ScmRepositoryException
    {
        Matcher scmMatcher = SCM_CONNECTION_PATTERN.matcher( url );

        if ( !scmMatcher.find() )
        {
            throw new ScmRepositoryException( "The specified url is invalid, it should use this format - "
                + DimensionsConstants.URL_FORMAT );
        }
        return scmMatcher;
    }


}
