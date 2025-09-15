/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.maven.scm.provider;

import java.util.ArrayList;
import java.util.List;

/**
 * A utility class that validates and parses scm url:s. The code here is
 * <strong>not</strong> scm provider specific.
 * <p>
 * If you need methods that work for a specific scm provider, please create a
 * similar class for that provider. E.g. create the class SvnScmUrlUtils if
 * you need Subversion-specific checking/parsing.
 * </p>
 *
 * @author <a href="mailto:dennisl@apache.org">Dennis Lundberg</a>
 */
public abstract class ScmUrlUtils {
    private static final String ILLEGAL_SCM_URL =
            "The scm url must be on the form 'scm:<scm provider><delimiter><provider specific part>' "
                    + "where <delimiter> can be either ':' or '|'.";

    /**
     * Get the delimiter used in the scm url.
     *
     * @param scmUrl a valid scm url to parse
     * @return the delimiter used in the scm url
     */
    public static String getDelimiter(String scmUrl) {
        scmUrl = scmUrl.substring(4);

        int index = scmUrl.indexOf('|');

        if (index == -1) {
            index = scmUrl.indexOf(':');

            if (index == -1) {
                throw new IllegalArgumentException("The scm url does not contain a valid delimiter.");
            }
        }

        return scmUrl.substring(index, index + 1);
    }

    /**
     * Get the scm provider from the scm url.
     *
     * @param scmUrl a valid scm url to parse
     * @return the scm provider from the scm url
     */
    public static String getProvider(String scmUrl) {
        String delimiter = getDelimiter(scmUrl);

        scmUrl = scmUrl.substring(4);

        int firstDelimiterIndex = scmUrl.indexOf(delimiter);

        return scmUrl.substring(0, firstDelimiterIndex);
    }

    /**
     * Get the provider specific part of the scm url.
     *
     * @param scmUrl a valid scm url to parse
     * @return the provider specific part of the scm url
     */
    public static String getProviderSpecificPart(String scmUrl) {
        String delimiter = getDelimiter(scmUrl);

        scmUrl = scmUrl.substring(4);

        int firstDelimiterIndex = scmUrl.indexOf(delimiter);

        return scmUrl.substring(firstDelimiterIndex + 1);
    }

    /**
     * Validate that the scm url is in the correct format.
     * <p>
     * <strong>Note</strong>: does not validate scm provider specific format.
     * </p>
     *
     * @param scmUrl the scm url to validate
     * @return <code>true</code> if the scm url is in the correct format,
     *         otherwise <code>false</code>
     */
    public static boolean isValid(String scmUrl) {
        List<String> messages = validate(scmUrl);

        return messages.isEmpty();
    }

    /**
     * Validate that the scm url is in the correct format.
     * <p>
     * <strong>Note</strong>: does not validate scm provider specific format.
     * </p>
     *
     * @param scmUrl the scm url to validate
     * @return a <code>List</code> that contains the errors that occured
     */
    public static List<String> validate(String scmUrl) {
        List<String> messages = new ArrayList<>();

        if (scmUrl == null) {
            messages.add("The scm url cannot be null.");

            return messages;
        }

        if (!scmUrl.startsWith("scm:")) {
            messages.add("The scm url must start with 'scm:'.");

            return messages;
        }

        if (scmUrl.length() < 6) {
            messages.add(ILLEGAL_SCM_URL);

            return messages;
        }

        try {
            getDelimiter(scmUrl);
        } catch (IllegalArgumentException e) {
            messages.add(e.getMessage());
        }

        return messages;
    }
}
