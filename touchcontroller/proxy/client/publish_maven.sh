#!/usr/bin/env bash

source "$(dirname "$0")/../../../shell/publish_maven.bash"

publish //touchcontroller/proxy/client/core:core.publish core
publish //touchcontroller/proxy/client/android:android.publish android
