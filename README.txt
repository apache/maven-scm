to cut a release :
mvn release:prepare release:perform -B -Dusername=svnuid -Dpassword=svnpassword -DpreparationGoals="clean install"
The additionnal -DpreparationGoals="clean install" is fixed in 3.x core

