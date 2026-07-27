#!/bin/bash
export JAVA_HOME="C:/Program Files/Java/jdk-17.0.12"
export PATH="$JAVA_HOME/bin:$PATH"
cd "$(dirname "$0")"
nohup java -jar target/nginx-cert-tool-1.0.0.jar > /dev/null 2>&1 &
echo "SSLCertTools started."
