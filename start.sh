#!/bin/bash
pkill -f 'java -jar'
java -jar ./target/QTD_Bot-*-SNAPSHOT-shaded.jar $1