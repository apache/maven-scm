---
title: SCM Authentication
---

<!-- Licensed to the Apache Software Foundation (ASF) under one-->
<!-- or more contributor license agreements.  See the NOTICE file-->
<!-- distributed with this work for additional information-->
<!-- regarding copyright ownership.  The ASF licenses this file-->
<!-- to you under the Apache License, Version 2.0 (the-->
<!-- "License"); you may not use this file except in compliance-->
<!-- with the License.  You may obtain a copy of the License at-->
<!---->
<!--   http://www.apache.org/licenses/LICENSE-2.0-->
<!---->
<!-- Unless required by applicable law or agreed to in writing,-->
<!-- software distributed under the License is distributed on an-->
<!-- "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY-->
<!-- KIND, either express or implied.  See the License for the-->
<!-- specific language governing permissions and limitations-->
<!-- under the License.-->
<!-- NOTE: For help with the syntax of this file, see:-->
<!-- http://maven.apache.org/doxia/references/apt-format.html-->
# Authentication

The most common authentication mechanisms supported by SCM providers are outlined below.

<!-- MACRO{toc|fromDepth=2} -->

## Username/Password Authentication

All SCM providers support a username/password based authentication. As the username and password are transmitted in clear text one should only use this with encrypted transport protocols like HTTPS. 

## SSH Public Key Authentication

Most SCM providers now also support SSH authentication based on an SSH key pair. In order for the server to authenticate a client it must know the client's public SSH. You need to upload the SSH public key to the remote server in order to leverage this authentication and configure your private SSH key for the according repository.

Some information on how to set this up for Git servers can be found at <https://git-scm.com/book/en/v2/Git-on-the-Server-Generating-Your-SSH-Public-Key.html>.

## Configuration

There are multiple ways how the authentication can be configured. They are listed in the recommended order from safest to unsafest option.

### Implicit

Most SCM providers have built-in authentication configuration means. On demand they will interactively ask for credentials (in case Maven is not running in batch mode and the credentials are not yet known) and persist those throughout the session or even longer. Often one can also configure the credentials in advance in dedicated files.

- [Subversion Credentials](https://svnbook.red-bean.com/en/1.8/svn.serverconfig.netmodel.html#svn.serverconfig.netmodel.creds)
- [Git Credentials](https://git-scm.com/docs/gitcredentials)
- [Mercurial Credentials](https://www.mercurial-scm.org/help/topics/config#auth)

Providers supporting SSH authentication often leverage the [OpenSSH client configuration file](https://man.openbsd.org/ssh_config).

### Maven Settings

The configuration of the authentication can (in most cases) be provided in the user specific [`settings.xml`](https://maven.apache.org/settings.html). The configuration happens per server having a unique id. The mapping of an SCM repository to a particular server id is based on the repository URL like this:

```
server-id=repo-host[":"repo-port]
```

The repository port is optional and only used if a specific port is referenced in the repository URL.
In some plugins one can overwrite this automatic mapping and configure an explicit server id.
One can use encryption or interpolation (preferred) to store the credentials in a secure manner.

**This way of configuration is not enforced by maven-scm-api as it does not have a dependency on the Maven API (and therefore its `settings.xml`) however it is supported by most SCM consumers which are running inside Maven.**

The following Maven plugins support this configuration possibility:

- [maven-scm-plugin](maven-svm-plugin/)
- [maven-release-plugin](https://maven.apache.org/plugins/maven-release-plugin/)
- [maven-scm-publish-plugin](https://maven.apache.org/plugins/maven-scm-publish-plugin/)

### Plugin Parameter

Only in special cases one should rely on plugin parameters to pass authentication information. In any case this should never be hardcoded into the POM but always either interpolated from a Java system property/environment variable or passed as CLI argument. This takes precedence over all other configuration locations if set.
*Not all authentication parameters are supported as plugin parameters in all Maven plugins*.

For details how to configure refer to the individual Maven plugin like 

- [maven-scm-plugin](maven-svm-plugin/)
- [maven-release-plugin](https://maven.apache.org/plugins/maven-release-plugin/)
- [maven-scm-publish-plugin](https://maven.apache.org/plugins/maven-scm-publish-plugin/)
