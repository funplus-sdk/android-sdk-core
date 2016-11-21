#!/usr/bin/env bash

# go to project root.
if [[ $(pwd) == *Scripts ]]; then
    cd ..
fi

ver=$(grep "VERSION = " FunPlusSDK/src/main/java/com/funplus/sdk/FunPlusSDK.java | sed "s/public static final String VERSION = //g" | tr -d ' ;"')
f=funplus-android-sdk-$ver.jar

src=Release/funplus-android-sdk-$ver/$f
target_dir=../../unity/sdk-core/Assets/FunPlusSDK/Plugins/Android

if [ -f $target_dir/funplus-android-sdk*.jar ]; then
    rm $target_dir/funplus-android-sdk*
fi

dst=$target_dir/funplus-android-sdk.jar

cp $src $dst

echo Copied $src to $dst
