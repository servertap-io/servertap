#!/bin/bash

set -e

mvn release:prepare
mvn release:clean
