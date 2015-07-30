#!/bin/sh
project_lib=target/kafka-error-reproduce-1.0-SNAPSHOT.jar
echo "========add lib to samza========"
cp $project_lib $SAMZA_HOME/lib
cd $SAMZA_HOME
tar -zcf $SAMZA_HOME/samzatest.tar.gz bin/ config/ lib/