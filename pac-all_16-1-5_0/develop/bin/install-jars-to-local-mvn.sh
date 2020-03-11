#!/bin/bash
# ----------------------------------------------------------------------------------------
# Install all JARs that are not available at public Maven repositories to local repository
# ----------------------------------------------------------------------------------------

for pom_file in ../pom/parent/*.pom ; do
    pom_file_name=$(basename $pom_file)
    mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=$pom_file -DpomFile=$pom_file
done
for pom_file in ../pom/*.pom ; do
    pom_file_name=$(basename $pom_file)
    artifact_id=${pom_file_name%.pom}
    jar_file=../../lib/$artifact_id.jar
    javadoc_file=../javadoc/$artifact_id-javadoc.jar
    if [ -f $javadoc_file ] ; then
        mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=$jar_file -Djavadoc=$javadoc_file -DpomFile=$pom_file
    else
        mvn org.apache.maven.plugins:maven-install-plugin:3.0.0-M1:install-file -Dfile=$jar_file -DpomFile=$pom_file
    fi
done
