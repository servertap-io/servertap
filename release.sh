#!/bin/bash

set -e

mvn release:prepare -DignoreSnapshots=true
mvn release:clean
