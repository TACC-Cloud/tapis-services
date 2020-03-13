#!/bin/sh
export VER=`cat target/classes/tapis.version`
export TAG="tapis/systems:${VER}"
# docker run -d --rm --network="host" -p 7070:8080 -p 7000:8000 ${TAG}
# Running with network=host exposes ports directly. Only works for linux
docker run -d --rm --network="host" ${TAG}