#! /bin/bash
# Run API REST service.

JAVA_CLASS_PATH=$1

if [ -z "${JAVA_JAR_FILENAME}" ]; then
  echo Looking for project metadata...
  JAVA_PROJECT_NAME=$(mvn exec:exec -Dexec.executable=echo -Dexec.args='${project.name}' -q)
  JAVA_PROJECT_VERSION=$(mvn exec:exec -Dexec.executable=echo -Dexec.args='${project.version}' -q)
  export JAVA_JAR_FILENAME=$JAVA_PROJECT_NAME-$JAVA_PROJECT_VERSION.jar
  unset JAVA_PROJECT_NAME JAVA_PROJECT_VERSION
fi

echo '>>' Jar file: $JAVA_JAR_FILENAME

if [ -z "${JAVA_CLASS_PATH}" ]; then
  # RUn java jarfile
  java -jar target/$JAVA_JAR_FILENAME
else
  # Search path class on jarfile
  echo '>>' Searching for $JAVA_CLASS_PATH...
  java -cp target/$JAVA_JAR_FILENAME $JAVA_CLASS_PATH
fi
