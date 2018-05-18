#######################################
Description of linear-changelog repository
#######################################

The 'linear-changelog' repositories in src\test\resources of git implementations has been created
    - using the script below
    - and then using a `clone --bare` command

- - - - - - - - - - - - - - - -
Repository structure
- - - - - - - - - - - - - - - -

    $ git log --graph --format="%h - %s (authored) <%an> %ai (commited) <%cn> %ci"
    * 464921b - action 5 (authored) <John Doe> 2017-10-01 12:00:00 +0000 (commited) <John Doe> 2017-10-10 12:00:00 +0000
    * db46d63 - action 4 (authored) <John Doe> 2017-08-01 12:00:00 +0000 (commited) <John Doe> 2017-08-10 12:00:00 +0000
    * e3864d9 - action 3 (authored) <John Doe> 2017-06-01 12:00:00 +0000 (commited) <John Doe> 2017-06-10 12:00:00 +0000
    * 0f1e817 - action 2 (authored) <John Doe> 2017-04-01 12:00:00 +0000 (commited) <John Doe> 2017-04-10 12:00:00 +0000
    * e75cb5a - action 1 (authored) <John Doe> 2017-02-01 12:00:00 +0000 (commited) <John Doe> 2017-02-10 12:00:00 +0000

- - - - - - - - - - - - - - - -
Creation script
- - - - - - - - - - - - - - - -
    #!/bin/sh

    cd /tmp
    rm -rf linear-changelog
    mkdir linear-changelog
    cd linear-changelog
    git init
    git config user.name "John Doe"
    git config user.email "john.doe@somewhere.com"

    mkdir -p src/main/java
    mkdir -p src/test/java

    echo -n "/pom.xml" > pom.xml
    echo -n "/readme.txt" > readme.txt
    echo -n "/src/main/java/Application.java" > src/main/java/Application.java
    echo -n "/src/test/java/Test.java" > src/test/java/Test.java

    echo "## Linear changelog" >> README.md
    git add .

    echo "- 1" >> README.md
    git add -u
    git commit -m "action 1"
    export GIT_AUTHOR_DATE="2017-02-01T12:00:00Z"
    export GIT_COMMITTER_DATE="2017-02-10T12:00:00Z"
    git commit --amend --no-edit --date "$GIT_AUTHOR_DATE"

    echo "- 2" >> README.md
    git add -u
    git commit -m "action 2"
    export GIT_AUTHOR_DATE="2017-04-01T12:00:00Z"
    export GIT_COMMITTER_DATE="2017-04-10T12:00:00Z"
    git commit --amend --no-edit --date "$GIT_AUTHOR_DATE"

    echo "- 3" >> README.md
    git add -u
    git commit -m "action 3"
    export GIT_AUTHOR_DATE="2017-06-01T12:00:00Z"
    export GIT_COMMITTER_DATE="2017-06-10T12:00:00Z"
    git commit --amend --no-edit --date "$GIT_AUTHOR_DATE"

    echo "- 4" >> README.md
    git add -u
    git commit -m "action 4"
    export GIT_AUTHOR_DATE="2017-08-01T12:00:00Z"
    export GIT_COMMITTER_DATE="2017-08-10T12:00:00Z"
    git commit --amend --no-edit --date "$GIT_AUTHOR_DATE"

    echo "- 5" >> README.md
    git add -u
    git commit -m "action 5"
    export GIT_AUTHOR_DATE="2017-10-01T12:00:00Z"
    export GIT_COMMITTER_DATE="2017-10-10T12:00:00Z"
    git commit --amend --no-edit --date "$GIT_AUTHOR_DATE"

    unset GIT_COMMITTER_DATE

    echo Repository created.
    git log --graph --format="%h - %s (authored) <%an> %ai (commited) <%cn> %ci"

