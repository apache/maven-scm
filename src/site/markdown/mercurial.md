 ------
 SCM Implementation: Mercurial (hg)
 ------
 Ryan Daum
 ------
 2007-04-12
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

SCM Implementation: Mercurial

* General Info

    Link: {{http://www.selenic.com/mercurial/wiki/}}

    License: GNU General Public License

    "Mercurial: a fast, lightweight Source Control Management system designed for efficient handling of very large distributed projects."

    This provider supports version 0.9.2 and greater of Mercurial.

* SCM URL

    Path or url to the branch location.
    Supported protocols: HTTP, HTTPS, FILE, and local path.

-------
scm:hg:url_to_repository/local_repository_directory
-------

* Examples

-------
scm:hg:http://host/v3
scm:hg:file://C:/dev/project/v3 (windows drive)
scm:hg:file:///home/smorgrav/dev/project/v3 (linux drive)
scm:hg:/home/smorgrav/dev/project/v3 (local directory)
-------
