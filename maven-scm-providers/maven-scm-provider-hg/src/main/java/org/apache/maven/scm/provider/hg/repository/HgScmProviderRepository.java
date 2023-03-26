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
package org.apache.maven.scm.provider.hg.repository;

import java.io.File;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

/**
 * @author <a href="mailto:thurner.rupert@ymono.net">thurner rupert</a>
 *
 */
public class HgScmProviderRepository extends ScmProviderRepositoryWithHost {
    // Known and tested protocols
    private static final String FILE = "";

    private static final String SFTP = "sftp://";

    private static final String FTP = "ftp://";

    private static final String AFTP = "aftp://";

    private static final String HTTP = "http://";

    private static final String HTTPS = "https://";

    private final String path;

    private final String protocol;

    private final String orgUrl;

    public HgScmProviderRepository(String url) {
        orgUrl = url;
        protocol = getProtocol(url);
        path = parseUrl(url);
    }

    public String getURI() {
        return protocol + addAuthority() + addHost() + addPort() + addPath();
    }

    /**
     * @return A message if the repository as an invalid URI, null if the URI seems fine.
     */
    public String validateURI() {

        String msg = null;

        if (needsAuthentication()) {
            if (getUser() == null) {
                msg = "Username is missing for protocol " + protocol;
            } else if (getPassword() == null) {
                msg = "Password is missing for protocol " + protocol;
            } else if (getHost() == null) {
                msg = "Host (eg. www.myhost.com) is missing for protocol " + protocol;
            }
        } else if (getPort() != 0 && getHost() == null) {
            msg = "Got port information without any host for protocol " + protocol;
        }

        if (msg != null) {
            msg = "Something could be wrong about the repository URL: " + orgUrl + "\nReason: " + msg
                    + "\nCheck http://maven.apache.org/scm for usage and hints.";
        }
        return msg;
    }

    private String getProtocol(String url) {
        // Assume we have a file unless we find a URL based syntax
        String prot = FILE;
        if (url.startsWith(SFTP)) {
            prot = SFTP;
        } else if (url.startsWith(HTTP)) {
            prot = HTTP;
        } else if (url.startsWith(HTTPS)) {
            prot = HTTPS;
        }

        return prot;
    }

    private String parseUrl(String url) {
        if (Objects.equals(protocol, FILE)) {
            return url;
        }

        // Strip protocol
        url = url.substring(protocol.length());

        url = parseUsernameAndPassword(url);

        url = parseHostAndPort(url);

        url = parsePath(url);

        return url; // is now only the path
    }

    private String parseHostAndPort(String url) {
        if (!Objects.equals(protocol, FILE)) {
            int indexSlash = url.indexOf('/');

            String hostPort = url;
            if (indexSlash > 0) {
                hostPort = url.substring(0, indexSlash);
                url = url.substring(indexSlash);
            }

            int indexColon = hostPort.indexOf(':');
            if (indexColon > 0) {
                setHost(hostPort.substring(0, indexColon));
                setPort(Integer.parseInt(hostPort.substring(indexColon + 1)));
            } else {
                setHost(hostPort);
            }
        }

        return url;
    }

    private String parseUsernameAndPassword(String url) {
        if (canAuthenticate()) {
            String[] split = url.split("@");
            if (split.length == 2) {
                url = split[1]; // Strip away 'username:password@' from url
                split = split[0].split(":");
                if (split.length == 2) { // both username and password
                    setUser(split[0]);
                    setPassword(split[1]);
                } else { // only username
                    setUser(split[0]);
                }
            }
        }
        return url;
    }

    private String parsePath(String url) {
        if (Objects.equals(protocol, FILE)) {
            // Use OS dependent path separator
            url = StringUtils.replace(url, "/", File.separator);

            // Test first path separator (*nix systems use them to denote root)
            File tmpFile = new File(url); // most likly a *nix system
            String url2 = url.substring(File.pathSeparator.length());
            File tmpFile2 = new File(url2); // most likly a windows system
            if (!tmpFile.exists() && !tmpFile2.exists()) {
                // This is trouble - Trouble is reported in validateURI()
            }

            url = tmpFile2.exists() ? url2 : url;
        }

        return url;
    }

    private String addUser() {
        return (getUser() == null) ? "" : getUser();
    }

    private String addPassword() {
        return (getPassword() == null) ? "" : ":" + getPassword();
    }

    private String addHost() {
        return (getHost() == null) ? "" : getHost();
    }

    private String addPort() {
        return (getPort() == 0) ? "" : ":" + getPort();
    }

    private String addPath() {
        return path;
    }

    private boolean needsAuthentication() {
        return Objects.equals(protocol, SFTP)
                || Objects.equals(protocol, FTP)
                || Objects.equals(protocol, HTTPS)
                || Objects.equals(protocol, AFTP);
    }

    private String addAuthority() {
        return ((canAuthenticate() && (getUser() != null)) ? addUser() + addPassword() + "@" : "");
    }

    private boolean canAuthenticate() {
        return needsAuthentication() || Objects.equals(protocol, HTTP);
    }
    /** {@inheritDoc} */
    public String toString() {
        return "Hg Repository Interpreted from: " + orgUrl + ":\nProtocol: " + protocol + "\nHost: " + getHost()
                + "\nPort: " + getPort() + "\nUsername: " + getUser() + "\nPassword: " + getPassword() + "\nPath: "
                + path;
    }
}
