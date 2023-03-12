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
package org.apache.maven.scm.provider.git.repository;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;

/**
 * @author <a href="mailto:evenisse@apache.org">Emmanuel Venisse</a>
 * @author <a href="mailto:struberg@apache.org">Mark Struberg</a>
 *
 */
public class GitScmProviderRepository extends ScmProviderRepositoryWithHost {

    /**
     * sequence used to delimit the fetch URL
     */
    public static final String URL_DELIMITER_FETCH = "[fetch=]";

    /**
     * sequence used to delimit the push URL
     */
    public static final String URL_DELIMITER_PUSH = "[push=]";

    /**
     * this trails every protocol
     */
    public static final String PROTOCOL_SEPARATOR = "://";

    /**
     * use local file as transport
     */
    public static final String PROTOCOL_FILE = "file";

    /**
     * use gits internal protocol
     */
    public static final String PROTOCOL_GIT = "git";

    /**
     * use secure shell protocol
     */
    public static final String PROTOCOL_SSH = "ssh";

    /**
     * use the standard port 80 http protocol
     */
    public static final String PROTOCOL_HTTP = "http";

    /**
     * use the standard port 443 https protocol
     */
    public static final String PROTOCOL_HTTPS = "https";

    /**
     * use rsync for retrieving the data
     * TODO implement!
     */
    public static final String PROTOCOL_RSYNC = "rsync";

    private static final Pattern HOST_AND_PORT_EXTRACTOR =
            Pattern.compile("([^:/\\\\~]*)(?::(\\d*))?(?:([:/\\\\~])(.*))?");

    /**
     * No special protocol specified. Git will either use git://
     * or ssh:// depending on whether we work locally or over the network
     */
    public static final String PROTOCOL_NONE = "";

    /**
     * this may either 'git' or 'jgit' depending on the underlying implementation being used
     */
    private String provider;

    /**
     * the URL used to fetch from the upstream repository
     */
    private RepositoryUrl fetchInfo;

    /**
     * the URL used to push to the upstream repository
     */
    private RepositoryUrl pushInfo;

    public GitScmProviderRepository(String url) throws ScmException {
        if (url == null) {
            throw new ScmException("url must not be null");
        }

        if (url.startsWith(URL_DELIMITER_FETCH)) {
            String fetch = url.substring(URL_DELIMITER_FETCH.length());

            int indexPushDelimiter = fetch.indexOf(URL_DELIMITER_PUSH);
            if (indexPushDelimiter >= 0) {
                String push = fetch.substring(indexPushDelimiter + URL_DELIMITER_PUSH.length());
                pushInfo = parseUrl(push);

                fetch = fetch.substring(0, indexPushDelimiter);
            }

            fetchInfo = parseUrl(fetch);

            if (pushInfo == null) {
                pushInfo = fetchInfo;
            }
        } else if (url.startsWith(URL_DELIMITER_PUSH)) {
            String push = url.substring(URL_DELIMITER_PUSH.length());

            int indexFetchDelimiter = push.indexOf(URL_DELIMITER_FETCH);
            if (indexFetchDelimiter >= 0) {
                String fetch = push.substring(indexFetchDelimiter + URL_DELIMITER_FETCH.length());
                fetchInfo = parseUrl(fetch);

                push = push.substring(0, indexFetchDelimiter);
            }

            pushInfo = parseUrl(push);

            if (fetchInfo == null) {
                fetchInfo = pushInfo;
            }
        } else {
            fetchInfo = pushInfo = parseUrl(url);
        }

        // set the default values for backward compatibility from the push url
        // because it's more likely that the push URL contains 'better' credentials
        setUser(pushInfo.getUserName());
        setPassword(pushInfo.getPassword());
        setHost(pushInfo.getHost());
        if (pushInfo.getPort() != null && pushInfo.getPort().length() > 0) {
            setPort(Integer.parseInt(pushInfo.getPort()));
        }
    }

    public GitScmProviderRepository(String url, String user, String password) throws ScmException {
        this(url);

        setUser(user);

        setPassword(password);
    }

    /**
     * @return either 'git' or 'jgit' depending on the underlying implementation being used
     */
    public String getProvider() {
        return provider;
    }

    public RepositoryUrl getFetchInfo() {
        return fetchInfo;
    }

    public RepositoryUrl getPushInfo() {
        return pushInfo;
    }

    /**
     * @return the URL used to fetch from the upstream repository
     */
    public String getFetchUrl() {
        return getUrl(fetchInfo);
    }

    /**
     * @return the URL used to push to the upstream repository
     */
    public String getPushUrl() {
        return getUrl(pushInfo);
    }

    /**
     * Parse the given url string and store all the extracted
     * information in a {@code RepositoryUrl}
     *
     * @param url to parse
     * @return filled with the information from the given URL
     * @throws ScmException
     */
    private RepositoryUrl parseUrl(String url) throws ScmException {
        RepositoryUrl repoUrl = new RepositoryUrl();

        url = parseProtocol(repoUrl, url);
        url = parseUserInfo(repoUrl, url);
        url = parseHostAndPort(repoUrl, url);
        // the rest of the url must be the path to the repository on the server
        repoUrl.setPath(url);
        return repoUrl;
    }

    /**
     * @param repoUrl
     * @return TODO
     */
    private String getUrl(RepositoryUrl repoUrl) {
        StringBuilder urlSb = new StringBuilder(repoUrl.getProtocol());
        boolean urlSupportsUserInformation = false;

        if (PROTOCOL_SSH.equals(repoUrl.getProtocol())
                || PROTOCOL_RSYNC.equals(repoUrl.getProtocol())
                || PROTOCOL_GIT.equals(repoUrl.getProtocol())
                || PROTOCOL_HTTP.equals(repoUrl.getProtocol())
                || PROTOCOL_HTTPS.equals(repoUrl.getProtocol())
                || PROTOCOL_NONE.equals(repoUrl.getProtocol())) {
            urlSupportsUserInformation = true;
        }

        if (repoUrl.getProtocol() != null && repoUrl.getProtocol().length() > 0) {
            urlSb.append("://");
        }

        // add user information if given and allowed for the protocol
        if (urlSupportsUserInformation) {
            String userName = repoUrl.getUserName();
            // if specified on the commandline or other configuration, we take this.
            if (getUser() != null && getUser().length() > 0) {
                userName = getUser();
            }

            String password = repoUrl.getPassword();
            if (getPassword() != null && getPassword().length() > 0) {
                password = getPassword();
            }

            if (userName != null && userName.length() > 0) {
                String userInfo = userName;
                if (password != null && password.length() > 0) {
                    userInfo += ":" + password;
                }

                try {
                    URI uri = new URI(null, userInfo, "localhost", -1, null, null, null);
                    urlSb.append(uri.getRawUserInfo());
                } catch (URISyntaxException e) {
                    // Quite impossible...
                    // Otherwise throw a RTE since this method is also used by toString()
                    e.printStackTrace();
                }

                urlSb.append('@');
            }
        }

        // add host and port information
        urlSb.append(repoUrl.getHost());
        if (repoUrl.getPort() != null && repoUrl.getPort().length() > 0) {
            urlSb.append(':').append(repoUrl.getPort());
        }

        // finaly we add the path to the repo on the host
        urlSb.append(repoUrl.getPath());

        return urlSb.toString();
    }

    /**
     * Parse the protocol from the given url and fill it into the given RepositoryUrl.
     *
     * @param repoUrl
     * @param url
     * @return the given url with the protocol parts removed
     */
    private String parseProtocol(RepositoryUrl repoUrl, String url) throws ScmException {
        // extract the protocol
        if (url.startsWith(PROTOCOL_FILE + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_FILE);
        } else if (url.startsWith(PROTOCOL_HTTPS + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_HTTPS);
        } else if (url.startsWith(PROTOCOL_HTTP + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_HTTP);
        } else if (url.startsWith(PROTOCOL_SSH + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_SSH);
        } else if (url.startsWith(PROTOCOL_GIT + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_GIT);
        } else if (url.startsWith(PROTOCOL_RSYNC + PROTOCOL_SEPARATOR)) {
            repoUrl.setProtocol(PROTOCOL_RSYNC);
        } else {
            // when no protocol is specified git will pick either ssh:// or git://
            // depending on whether we work locally or over the network
            repoUrl.setProtocol(PROTOCOL_NONE);
            return url;
        }

        url = url.substring(repoUrl.getProtocol().length() + 3);

        return url;
    }

    /**
     * Parse the user information from the given url and fill
     * user name and password into the given RepositoryUrl.
     *
     * @param repoUrl
     * @param url
     * @return the given url with the user parts removed
     */
    private String parseUserInfo(RepositoryUrl repoUrl, String url) throws ScmException {
        if (PROTOCOL_FILE.equals(repoUrl.getProtocol())) {
            // a file:// URL may contain userinfo according to RFC 8089, but our implementation is broken
            return url;
        }
        // extract user information, broken see SCM-907
        int indexAt = url.lastIndexOf('@');
        if (indexAt >= 0) {
            String userInfo = url.substring(0, indexAt);
            int indexPwdSep = userInfo.indexOf(':');
            if (indexPwdSep < 0) {
                repoUrl.setUserName(userInfo);
            } else {
                repoUrl.setUserName(userInfo.substring(0, indexPwdSep));
                repoUrl.setPassword(userInfo.substring(indexPwdSep + 1));
            }

            url = url.substring(indexAt + 1);
        }
        return url;
    }

    /**
     * Parse server and port from the given url and fill it into the
     * given RepositoryUrl.
     *
     * @param repoUrl
     * @param url
     * @return the given url with the server parts removed
     * @throws ScmException
     */
    private String parseHostAndPort(RepositoryUrl repoUrl, String url) throws ScmException {

        repoUrl.setPort("");
        repoUrl.setHost("");

        if (PROTOCOL_FILE.equals(repoUrl.getProtocol())) {
            // a file:// URL doesn't need any further parsing as it cannot contain a port, etc
            return url;
        } else {

            Matcher hostAndPortMatcher = HOST_AND_PORT_EXTRACTOR.matcher(url);
            if (hostAndPortMatcher.matches()) {
                if (hostAndPortMatcher.groupCount() > 1 && hostAndPortMatcher.group(1) != null) {
                    repoUrl.setHost(hostAndPortMatcher.group(1));
                }
                if (hostAndPortMatcher.groupCount() > 2 && hostAndPortMatcher.group(2) != null) {
                    repoUrl.setPort(hostAndPortMatcher.group(2));
                }

                StringBuilder computedUrl = new StringBuilder();
                if (hostAndPortMatcher.group(hostAndPortMatcher.groupCount() - 1) != null) {
                    computedUrl.append(hostAndPortMatcher.group(hostAndPortMatcher.groupCount() - 1));
                }
                if (hostAndPortMatcher.group(hostAndPortMatcher.groupCount()) != null) {
                    computedUrl.append(hostAndPortMatcher.group(hostAndPortMatcher.groupCount()));
                }
                return computedUrl.toString();
            } else {
                // Pattern doesn't match, let's return the original url
                return url;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    public String getRelativePath(ScmProviderRepository ancestor) {
        if (ancestor instanceof GitScmProviderRepository) {
            GitScmProviderRepository gitAncestor = (GitScmProviderRepository) ancestor;

            // X TODO review!
            String url = getFetchUrl();
            String path = url.replaceFirst(gitAncestor.getFetchUrl() + "/", "");

            if (!path.equals(url)) {
                return path;
            }
        }
        return null;
    }

    /**
     * {@inheritDoc}
     */
    public String toString() {
        // yes we really like to check if those are the exact same instance!
        if (fetchInfo == pushInfo) {
            return getUrl(fetchInfo);
        }
        return URL_DELIMITER_FETCH + getUrl(fetchInfo) + URL_DELIMITER_PUSH + getUrl(pushInfo);
    }
}
