#!/bin/sh

# Gradle wrapper start up script for POSIX systems.
# This runs the gradle-wrapper.jar to download and launch Gradle.

APP_HOME=$(cd "$(dirname "$0")" || exit; pwd)

CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# Determine the Java command to use.
if [ -n "$JAVA_HOME" ]; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
