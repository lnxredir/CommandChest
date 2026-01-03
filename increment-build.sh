#!/bin/bash
if [ -f build.number ]; then
    BUILD_NUM=$(cat build.number)
else
    BUILD_NUM=0
fi
NEW_BUILD=$((BUILD_NUM + 1))
echo $NEW_BUILD > build.number
echo $NEW_BUILD
