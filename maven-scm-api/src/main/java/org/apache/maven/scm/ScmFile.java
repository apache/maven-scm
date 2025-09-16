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
package org.apache.maven.scm;

import java.io.Serializable;

/**
 * @author <a href="mailto:trygvis@inamo.no">Trygve Laugst&oslash;l</a>
 */
public class ScmFile implements Comparable<ScmFile>, Serializable {
    private static final long serialVersionUID = -9133015730693522690L;

    private String path;

    private ScmFileStatus status;

    /**
     * @param path   the relative path of the file, should <b>never</b> start with any {@link java.io.File#separator}
     * @param status the file status
     */
    public ScmFile(String path, ScmFileStatus status) {
        this.path = path;

        this.status = status;
    }

    /**
     * Returns the relative path of the file.
     *
     * @return the file path
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the file status
     */
    public ScmFileStatus getStatus() {
        return status;
    }

    // ----------------------------------------------------------------------
    // Comparable Implementation
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public int compareTo(ScmFile other) {
        return other.getPath().compareTo(path);
    }

    // ----------------------------------------------------------------------
    // Object overrides
    // ----------------------------------------------------------------------

    /**
     * {@inheritDoc}
     */
    public boolean equals(Object other) {
        if (!(other instanceof ScmFile)) {
            return false;
        }

        return ((ScmFile) other).getPath().equals(path);
    }

    /**
     * {@inheritDoc}
     */
    public int hashCode() {
        return path.hashCode();
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        return "[" + path + ":" + status + "]";
    }
}
