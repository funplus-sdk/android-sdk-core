#!/usr/bin/env bash

# go to project root.
if [[ $(pwd) == *Scripts ]]; then
    cd ..
fi

ver=$(grep "VERSION = " FunPlusSDK/src/main/java/com/funplus/sdk/FunPlusSDK.java | sed "s/public static final String VERSION = //g" | tr -d ' ;"')
out=$(echo Release/funplus-android-sdk-$ver | tr -d ' ')

echo SDK version: $ver
echo Output directory: $out

# check output directory.
if [ -d $out ]; then
    read -p 'Directory exists. This action will erase the existing directory, are you sure? [yN] ' yn
    if [[ $yn != 'y' && $yn != 'Y' ]]; then
        echo exit
        exit
    else
        rm -rf $out
    fi
fi

echo

# prepare output directory.
mkdir $out

# copy docs
cp {README,CHANGELOG}.md $out/

gradle build

build_dir=FunPlusSDK/build/libs
cp $build_dir/funplus-android-sdk.jar $out/funplus-android-sdk-$ver.jar
