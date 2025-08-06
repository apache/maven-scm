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
package org.apache.maven.scm.plugin;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.scm.ScmBranch;
import org.apache.maven.scm.ScmException;
import org.apache.maven.scm.ScmFileSet;
import org.apache.maven.scm.ScmResult;
import org.apache.maven.scm.ScmRevision;
import org.apache.maven.scm.ScmTag;
import org.apache.maven.scm.ScmVersion;
import org.apache.maven.scm.manager.ScmManager;
import org.apache.maven.scm.provider.ScmProviderRepository;
import org.apache.maven.scm.provider.ScmProviderRepositoryWithHost;
import org.apache.maven.scm.provider.svn.repository.SvnScmProviderRepository;
import org.apache.maven.scm.repository.ScmRepository;
import org.apache.maven.scm.repository.ScmRepositoryException;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.SettingsProblem;
import org.apache.maven.settings.crypto.DefaultSettingsDecryptionRequest;
import org.apache.maven.settings.crypto.SettingsDecrypter;
import org.apache.maven.settings.crypto.SettingsDecryptionResult;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * @author <a href="evenisse@apache.org">Emmanuel Venisse</a>
 * @author Olivier Lamy
 */
public abstract class AbstractScmMojo extends AbstractMojo {

    protected static final String VERSION_TYPE_BRANCH = "branch";

    protected static final String VERSION_TYPE_REVISION = "revision";

    protected static final String VERSION_TYPE_TAG = "tag";

    protected static final String[] VALID_VERSION_TYPES = {VERSION_TYPE_BRANCH, VERSION_TYPE_REVISION, VERSION_TYPE_TAG
    };

    /**
     * The SCM connection URL.
     */
    @Parameter(property = "connectionUrl", defaultValue = "${project.scm.connection}")
    private String connectionUrl;

    /**
     * The SCM connection URL for developers.
     */
    @Parameter(property = "developerConnectionUrl", defaultValue = "${project.scm.developerConnection}")
    private String developerConnectionUrl;

    /**
     * The type of connection to use (connection or developerConnection).
     */
    @Parameter(property = "connectionType", defaultValue = "connection")
    private String connectionType;

    /**
     * The working directory.
     */
    @Parameter(property = "workingDirectory")
    private File workingDirectory;

    /**
     * The user name.
     * @see <a href="https://maven.apache.org/scm/authentication.html">Authentication</a>
     */
    @Parameter(property = "username")
    private String username;

    /**
     * The user password.
     * @see <a href="https://maven.apache.org/scm/authentication.html">Authentication</a>
     */
    @Parameter(property = "password")
    private String password;

    /**
     * The private key.
     * @see <a href="https://maven.apache.org/scm/authentication.html">Authentication</a>
     */
    @Parameter(property = "privateKey")
    private String privateKey;

    /**
     * The passphrase.
     * @see <a href="https://maven.apache.org/scm/authentication.html">Authentication</a>
     */
    @Parameter(property = "passphrase")
    private String passphrase;

    /**
     * The server id of the server which provides the credentials for the SCM in the <a href="https://maven.apache.org/settings.html">settings.xml</a> file.
     * If not set the default lookup uses the SCM URL to construct the server id like this:
     * {@code server-id=scm-host[":"scm-port]}.
     * <p>
     * Currently the POM does not allow to specify a server id for the SCM section.
     * <p>
     * Explicit authentication information provided via {@link #username}, {@link #password} or {@link #privateKey} will take precedence.
     * @see <a href="https://maven.apache.org/scm/authentication.html">Authentication</a>
     * @since 2.2.0
     */
    @Parameter(property = "project.scm.id")
    private String serverId;

    /**
     * The url of tags base directory (used by svn protocol). It is not
     * necessary to set it if you use the standard svn layout
     * (branches/tags/trunk).
     */
    @Parameter(property = "tagBase")
    private String tagBase;

    /**
     * Comma separated list of includes file pattern.
     */
    @Parameter(property = "includes")
    private String includes;

    /**
     * Comma separated list of excludes file pattern.
     */
    @Parameter(property = "excludes")
    private String excludes;

    /**
     * The base directory.
     */
    @Parameter(property = "basedir", required = true)
    private File basedir;

    @Parameter(defaultValue = "${settings}", readonly = true)
    private Settings settings;

    /**
     * List of System properties to set before executing the SCM command.
     */
    @Parameter
    private Properties systemProperties;

    /**
     * List of remapped provider implementations. Allows to bind a different implementation than the default one to a provider id.
     * The key is the remapped provider id, the value is the default provider id the implementation is bound to.
     * @see <a href="https://maven.apache.org/scm/scms-overview.html">Supported SCMs</a>
     */
    @Parameter
    private Map<String, String> providerImplementations;

    /**
     * Should distributed changes be pushed to the central repository?
     * For many distributed SCMs like Git, a change like a commit
     * is only stored in your local copy of the repository.  Pushing
     * the change allows your to more easily share it with other users.
     *
     * @since 1.4
     */
    @Parameter(property = "pushChanges", defaultValue = "true")
    private boolean pushChanges;

    /**
     * A workItem for SCMs like RTC, TFS etc, that may require additional
     * information to perform a pushChange operation.
     *
     * @since 1.9.5
     */
    @Parameter(property = "workItem")
    @Deprecated
    private String workItem;

    private final ScmManager manager;

    private final SettingsDecrypter settingsDecrypter;

    protected AbstractScmMojo(ScmManager manager, SettingsDecrypter settingsDecrypter) {
        this.manager = manager;
        this.settingsDecrypter = settingsDecrypter;
    }

    /** {@inheritDoc} */
    public void execute() throws MojoExecutionException {
        if (systemProperties != null) {
            // Add all system properties configured by the user
            Iterator<Object> iter = systemProperties.keySet().iterator();

            while (iter.hasNext()) {
                String key = (String) iter.next();

                String value = systemProperties.getProperty(key);

                System.setProperty(key, value);
            }
        }

        if (providerImplementations != null && !providerImplementations.isEmpty()) {
            for (Entry<String, String> entry : providerImplementations.entrySet()) {
                String providerType = entry.getKey();
                String providerImplementation = entry.getValue();
                getLog().info("Change the default '" + providerType + "' provider implementation to '"
                        + providerImplementation + "'.");
                getScmManager().setScmProviderImplementation(providerType, providerImplementation);
            }
        }
    }

    protected void setConnectionType(String connectionType) {
        this.connectionType = connectionType;
    }

    public String getConnectionUrl() {
        boolean requireDeveloperConnection = !"connection".equals(connectionType.toLowerCase());
        if ((connectionUrl != null && !connectionUrl.isEmpty()) && !requireDeveloperConnection) {
            return connectionUrl;
        } else if (developerConnectionUrl != null && !developerConnectionUrl.isEmpty()) {
            return developerConnectionUrl;
        }
        if (requireDeveloperConnection) {
            throw new NullPointerException("You need to define a developerConnectionUrl parameter");
        } else {
            throw new NullPointerException("You need to define a connectionUrl parameter");
        }
    }

    public void setConnectionUrl(String connectionUrl) {
        this.connectionUrl = connectionUrl;
    }

    public File getWorkingDirectory() {
        if (workingDirectory == null) {
            return basedir;
        }

        return workingDirectory;
    }

    public File getBasedir() {
        return this.basedir;
    }

    public void setWorkingDirectory(File workingDirectory) {
        this.workingDirectory = workingDirectory;
    }

    public ScmManager getScmManager() {
        return manager;
    }

    public ScmFileSet getFileSet() throws IOException {
        if (includes != null || excludes != null) {
            return new ScmFileSet(getWorkingDirectory(), includes, excludes);
        } else {
            return new ScmFileSet(getWorkingDirectory());
        }
    }

    public ScmRepository getScmRepository() throws ScmException {
        ScmRepository repository;

        try {
            repository = getScmManager().makeScmRepository(getConnectionUrl());

            ScmProviderRepository providerRepo = repository.getProviderRepository();

            providerRepo.setPushChanges(pushChanges);

            if (!(workItem == null || workItem.isEmpty())) {
                providerRepo.setWorkItem(workItem);
            }

            if (!(username == null || username.isEmpty())) {
                providerRepo.setUser(username);
            }

            if (!(password == null || password.isEmpty())) {
                providerRepo.setPassword(password);
            }

            if (repository.getProviderRepository() instanceof ScmProviderRepositoryWithHost) {
                ScmProviderRepositoryWithHost repo = (ScmProviderRepositoryWithHost) repository.getProviderRepository();

                loadInfosFromSettings(repo);

                if (!(username == null || username.isEmpty())) {
                    repo.setUser(username);
                }

                if (!(password == null || password.isEmpty())) {
                    repo.setPassword(password);
                }

                if (!(privateKey == null || privateKey.isEmpty())) {
                    repo.setPrivateKey(privateKey);
                }

                if (!(passphrase == null || passphrase.isEmpty())) {
                    repo.setPassphrase(passphrase);
                }
            }

            if (!(tagBase == null || tagBase.isEmpty())
                    && repository.getProvider().equals("svn")) {
                SvnScmProviderRepository svnRepo = (SvnScmProviderRepository) repository.getProviderRepository();

                svnRepo.setTagBase(tagBase);
            }
        } catch (ScmRepositoryException e) {
            if (!e.getValidationMessages().isEmpty()) {
                for (String message : e.getValidationMessages()) {
                    getLog().error(message);
                }
            }

            throw new ScmException("Can't load the scm provider.", e);
        } catch (Exception e) {
            throw new ScmException("Can't load the scm provider.", e);
        }

        return repository;
    }

    /**
     * Load username password from settings if user has not set them in JVM properties
     *
     * @param repo not null
     */
    private void loadInfosFromSettings(ScmProviderRepositoryWithHost repo) {
        if (username == null || password == null) {
            String serverId = this.serverId;
            if (serverId == null || serverId.isEmpty()) {
                // construct server id from scm repository host and port
                serverId = repo.getHost();
                int port = repo.getPort();
                if (port > 0) {
                    serverId += ":" + port;
                }
            }

            Server server = this.settings.getServer(serverId);

            if (server != null) {
                server = decrypt(server);

                if (username == null) {
                    username = server.getUsername();
                }

                if (password == null) {
                    password = server.getPassword();
                }

                if (privateKey == null) {
                    privateKey = server.getPrivateKey();
                }

                if (passphrase == null) {
                    passphrase = server.getPassphrase();
                }
            }
        }
    }

    private Server decrypt(Server server) {
        SettingsDecryptionResult result = settingsDecrypter.decrypt(new DefaultSettingsDecryptionRequest(server));
        for (SettingsProblem problem : result.getProblems()) {
            getLog().error(problem.getMessage(), problem.getException());
        }

        return result.getServer();
    }

    public void checkResult(ScmResult result) throws MojoExecutionException {
        if (!result.isSuccess()) {
            getLog().error("Provider message:");

            getLog().error(result.getProviderMessage() == null ? "" : result.getProviderMessage());

            getLog().error("Command output:");

            getLog().error(result.getCommandOutput() == null ? "" : result.getCommandOutput());

            throw new MojoExecutionException("Command failed: " + Objects.toString(result.getProviderMessage()));
        }
    }

    public String getIncludes() {
        return includes;
    }

    public void setIncludes(String includes) {
        this.includes = includes;
    }

    public String getExcludes() {
        return excludes;
    }

    public void setExcludes(String excludes) {
        this.excludes = excludes;
    }

    public ScmVersion getScmVersion(String versionType, String version) throws MojoExecutionException {
        if ((versionType == null || versionType.isEmpty()) && (version != null && !version.isEmpty())) {
            throw new MojoExecutionException("You must specify the version type.");
        }

        if (version == null || version.isEmpty()) {
            return null;
        }

        if (VERSION_TYPE_BRANCH.equals(versionType)) {
            return new ScmBranch(version);
        }

        if (VERSION_TYPE_TAG.equals(versionType)) {
            return new ScmTag(version);
        }

        if (VERSION_TYPE_REVISION.equals(versionType)) {
            return new ScmRevision(version);
        }

        throw new MojoExecutionException("Unknown '" + versionType + "' version type.");
    }

    protected void handleExcludesIncludesAfterCheckoutAndExport(File checkoutDirectory) throws MojoExecutionException {
        List<String> includes = new ArrayList<>();

        if (!StringUtils.isBlank(this.getIncludes())) {
            String[] tokens = StringUtils.split(this.getIncludes(), ",");
            for (int i = 0; i < tokens.length; ++i) {
                includes.add(tokens[i]);
            }
        }

        List<String> excludes = new ArrayList<>();

        if (!StringUtils.isBlank(this.getExcludes())) {
            String[] tokens = StringUtils.split(this.getExcludes(), ",");
            for (int i = 0; i < tokens.length; ++i) {
                excludes.add(tokens[i]);
            }
        }

        if (includes.isEmpty() && excludes.isEmpty()) {
            return;
        }

        FileSetManager fileSetManager = new FileSetManager();

        FileSet fileset = new FileSet();
        fileset.setDirectory(checkoutDirectory.getAbsolutePath());
        fileset.setIncludes(excludes); // revert the order to do the delete
        fileset.setExcludes(includes);
        fileset.setUseDefaultExcludes(false);

        try {
            fileSetManager.delete(fileset);
        } catch (IOException e) {
            throw new MojoExecutionException(
                    "Error found while cleaning up output directory base on " + "excludes/includes configurations.", e);
        }
    }
}
