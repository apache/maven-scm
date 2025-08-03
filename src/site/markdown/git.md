 ------
 SCM Implementation: Git
 ------
 Olivier Lamy
 ------
 2008-08-10
 ------

~~ Licensed to the Apache Software Foundation (ASF) under one
~~ or more contributor license agreements.  See the NOTICE file
~~ distributed with this work for additional information
~~ regarding copyright ownership.  The ASF licenses this file
~~ to you under the Apache License, Version 2.0 (the
~~ "License"); you may not use this file except in compliance
~~ with the License.  You may obtain a copy of the License at
~~
~~   http://www.apache.org/licenses/LICENSE-2.0
~~
~~ Unless required by applicable law or agreed to in writing,
~~ software distributed under the License is distributed on an
~~ "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
~~ KIND, either express or implied.  See the License for the
~~ specific language governing permissions and limitations
~~ under the License.

~~ NOTE: For help with the syntax of this file, see:
~~ http://maven.apache.org/doxia/references/apt-format.html

SCM Implementation: Git

* General Info

    Link: {{http://git-scm.com/}}

    License: GNU General Public License v2

* SCM URL

    For all URLs below, we use a colon (:) as separator. If you use a colon for one of the variables (e.g. a windows path), then use a pipe (|) as separator. The separator for the port has to be a colon in any case since this part is specified in the git URL specification. See man git-fetch.

-------
scm:git:git://server_name[:port]/path_to_repository
scm:git:http://server_name[:port]/path_to_repository
scm:git:https://server_name[:port]/path_to_repository
scm:git:ssh://server_name[:port]/path_to_repository
scm:git:file://[hostname]/path_to_repository
-------

 * Examples

-------
scm:git:git://github.com/path_to_repository
scm:git:http://github.com/path_to_repository
scm:git:https://github.com/path_to_repository
scm:git:ssh://github.com/path_to_repository
scm:git:file://localhost/path_to_repository
-------

* Different Fetch and Push URLs

  In some cases a different URL has to be used for read and write operations. This can happen if e.g. fetch is performed via the http protocol, but writing to the repository is only possible via ssh. In this case both URLs may be written into the <developerConnection> tag. The fetch URL has to be prefixed with <<<[fetch=]>>> and the push URL with <<<[push=]>>>

 * Example:

-------
<developerConnection>scm:git:[fetch=]http://mywebserver.org/path_to_repository[push=]ssh://username@otherserver:8898/~/repopath.git</developerConnection>
-------

* Working with branches

  Since version 1.3, we assume that the name of the branch in the upstream repo is the same as the name of the current local branch. So whenever you invoke a maven-scm action which has to access the upstream repository, e.g. start a release, you should be on that very branch.

  In other words: If no branch is specified manually, every git-fetch, git-pull, git-push, etc will always work on the branch in the upstream repository which has the same branch name as your current local branch

-------
git push pushUrl currentBranch:currentBranch
-------


* Provider Configuration

  The provider configuration is defined in <<<$\{user.home\}/.scm/git-settings.xml>>>.
  For more information see the {{{./maven-scm-providers/maven-scm-providers-git/maven-scm-provider-git-commons/git-settings.html}reference guide}}.
