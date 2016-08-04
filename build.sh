#!/bin/sh
ant -f nbbuild.xml -Dnb.internal.action.name=build jar
mkdir dist/lib
cp jython-standalone-2.7.0.jar dist/lib/jython-standalone-2.7.0.jar
cp sqlite-jdbc-3.8.11.2.jar dist/lib/sqlite-jdbc-3.8.11.2.jar
cp pircbot.jar dist/lib/pircbot.jar