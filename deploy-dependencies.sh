#!/usr/bin/env sh

mvn install:install-file -Dfile=lib/opencv-2411.jar -DgroupId=org.opencv -DartifactId=opencv-java -Dversion=2.4.1.1 -Dpackaging=jar
