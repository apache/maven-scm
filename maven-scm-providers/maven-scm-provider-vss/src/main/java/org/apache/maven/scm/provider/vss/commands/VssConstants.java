package org.apache.maven.scm.provider.vss.commands;

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

/**
 * Holds all the constants for the VSS tasks.
 *
 * @version $Id$
 */
public final class VssConstants
{

    private VssConstants()
    {
    }

    /**
     * Constant for the thing to execute
     */
    public static final String SS_EXE = "ss";

    /**
     * Dollar Sigh to prefix the project path
     */
    public static final String PROJECT_PREFIX = "$";

    /**
     * The 'CP' command
     */
    public static final String COMMAND_CP = "CP";

    /**
     * The 'Add' command
     */
    public static final String COMMAND_ADD = "Add";

    /**
     * The 'Get' command
     */
    public static final String COMMAND_GET = "Get";

    /**
     * The 'Checkout' command
     */
    public static final String COMMAND_CHECKOUT = "Checkout";

    /**
     * The 'Checkin' command
     */
    public static final String COMMAND_CHECKIN = "Checkin";

    /**
     * The 'Label' command
     */
    public static final String COMMAND_LABEL = "Label";

    /**
     * The 'History' command
     */
    public static final String COMMAND_HISTORY = "History";

    /**
     * The 'Create' command
     */
    public static final String COMMAND_CREATE = "Create";

    /**
     * The 'Status' command
     */
    public static final String COMMAND_DIFF = "Diff";

    /**
     * The 'Status' command
     */
    public static final String COMMAND_STATUS = "Status";

    /**
     * The brief style flag
     */
    public static final String STYLE_BRIEF = "brief";

    /**
     * The codediff style flag
     */
    public static final String STYLE_CODEDIFF = "codediff";

    /**
     * The nofile style flag
     */
    public static final String STYLE_NOFILE = "nofile";

    /**
     * The default style flag
     */
    public static final String STYLE_DEFAULT = "default";

    /**
     * The text for  current (default) timestamp
     */
    public static final String TIME_CURRENT = "current";

    /**
     * The text for  modified timestamp
     */
    public static final String TIME_MODIFIED = "modified";

    /**
     * The text for  updated timestamp
     */
    public static final String TIME_UPDATED = "updated";

    /**
     * The text for replacing writable files
     */
    public static final String WRITABLE_REPLACE = "replace";

    /**
     * The text for skiping writable files
     */
    public static final String WRITABLE_SKIP = "skip";

    /**
     * The text for failing on writable files
     */
    public static final String WRITABLE_FAIL = "fail";

    public static final String FLAG_LOGIN = "-Y";

    public static final String FLAG_OVERRIDE_WORKING_DIR = "-GL";

    public static final String FLAG_AUTORESPONSE_DEF = "-I-";

    public static final String FLAG_AUTORESPONSE_YES = "-I-Y";

    public static final String FLAG_AUTORESPONSE_NO = "-I-N";

    public static final String FLAG_RECURSION = "-R";

    public static final String FLAG_VERSION = "-V";

    public static final String FLAG_VERSION_DATE = "-Vd";

    public static final String FLAG_VERSION_LABEL = "-VL";

    public static final String FLAG_WRITABLE = "-W";

    public static final String VALUE_NO = "-N";

    public static final String VALUE_YES = "-Y";

    public static final String FLAG_QUIET = "-O-";

    public static final String FLAG_COMMENT = "-C";

    public static final String FLAG_LABEL = "-L";

    public static final String VALUE_FROMDATE = "~d";

    public static final String VALUE_FROMLABEL = "~L";

    public static final String FLAG_OUTPUT = "-O";

    public static final String FLAG_USER = "-U";

    public static final String FLAG_NO_FILE = "-F-";

    public static final String FLAG_BRIEF = "-B";

    public static final String FLAG_CODEDIFF = "-D";

    public static final String FLAG_FILETIME_DEF = "-GTC";

    public static final String FLAG_FILETIME_MODIFIED = "-GTM";

    public static final String FLAG_FILETIME_UPDATED = "-GTU";

    public static final String FLAG_REPLACE_WRITABLE = "-GWR";

    public static final String FLAG_SKIP_WRITABLE = "-GWS";

    public static final String FLAG_NO_GET = "-G-";
}
