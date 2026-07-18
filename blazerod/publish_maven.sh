#!/usr/bin/env bash

source "$(dirname "$0")/../shell/publish_maven.bash"

for module in model-base model-loader model-bedrock model-assimp model-gltf model-pmd model-pmx model-vmd model-formats
do
    publish //blazerod/model/$module:$module.publish
done
publish //blazerod/render:render_fabric.publish
publish //blazerod:blazerod_fabric_without_jij.publish
