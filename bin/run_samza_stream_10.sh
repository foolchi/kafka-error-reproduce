#!/bin/sh
$SAMZA_HOME/bin/run-job.sh --config-factory=org.apache.samza.config.factories.PropertiesConfigFactory --config-path=file://$PWD/config/samza_stream_10.properties
