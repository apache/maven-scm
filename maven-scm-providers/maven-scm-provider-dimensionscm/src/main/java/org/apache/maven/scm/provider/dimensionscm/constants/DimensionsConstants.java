package org.apache.maven.scm.provider.dimensionscm.constants;

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
 * Definition of some Dimensions CM specific constant values.
 */
public class DimensionsConstants
{

    public static final String COMMAND_FAILED = "The Dimensions CM command failed.";
    public static final String COMMAND_SUCCEEDED = "The Dimensions CM command succeeded.";

    /**
     * Definition of Dimensions CM Maven SCM URL format.
     */
    public static final String URL_FORMAT =
        "scm:dimensionscm://[<username>][:<password>]@<server>[:<port>]/"
        + "<dbName>@<dbConnection>[/product][:project][/relativeLocation]";

}
