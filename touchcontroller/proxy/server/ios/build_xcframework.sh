#!/bin/bash
# =============================================================================
# TouchController iOS XCFramework 构建脚本（TouchController 仓库版本）
# =============================================================================
# 功能：将 touchcontroller/proxy/server/ios/ios.c 与
#       touchcontroller/proxy/server/util/ringbuffer/ring_buffer.c 交叉编译为
#       iOS 设备 (arm64) 和 iOS 模拟器 (arm64 + x86_64) 静态库，并打包为
#       标准 XCFramework，供 Amethyst iOS Remastered 启动器集成使用。
#
# 用法：
#   ./build_xcframework.sh          # 执行完整构建
#   ./build_xcframework.sh --clean  # 清理中间文件和 XCFramework 产物
#
# 产物：
#   TouchController.xcframework/
#   ├── Info.plist                          # XCFramework 元数据
#   ├── ios-arm64/
#   │   ├── libproxy_server_ios.a           # iOS 设备 (arm64)
#   │   └── Headers/
#   │       ├── ios.h
#   │       └── ring_buffer.h
#   └── ios-arm64_x86_64-simulator/
#       ├── libproxy_server_ios_simulator.a # iOS 模拟器 (arm64 + x86_64)
#       └── Headers/
#           ├── ios.h
#           └── ring_buffer.h
# =============================================================================
set -e

# 切换到脚本所在目录的仓库根目录
# 脚本路径：touchcontroller/proxy/server/ios/build_xcframework.sh
# 从 ios/ 目录回退到仓库根需要 4 级 .. ：
#   ios/ -> server/ -> proxy/ -> touchcontroller/ -> <repo_root>
SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
REPO_ROOT="$(cd "${SCRIPT_DIR}/../../../.." && pwd)"
cd "${REPO_ROOT}"

# 源文件（仓库根相对路径）
# ios.c 通过相对路径 #include "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"
# 因此编译时必须以仓库根为工作目录，-I. 包含仓库根
SRC_IOS="touchcontroller/proxy/server/ios/ios.c"
SRC_RING="touchcontroller/proxy/server/util/ringbuffer/ring_buffer.c"

# 头文件目录（用于 XCFramework Headers 输出）
HEADER_IOS="touchcontroller/proxy/server/ios/ios.h"
HEADER_RING="touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h"

# 构建中间目录与产物目录
BUILD_DIR="build/ios_xcframework"
XCFW_DIR="TouchController.xcframework"

# -----------------------------------------------------------------------------
# 处理 --clean 参数：清理中间文件和 XCFramework 产物
# -----------------------------------------------------------------------------
if [ "$1" = "--clean" ]; then
  echo "===> Cleaning iOS XCFramework build artifacts..."
  rm -rf "${BUILD_DIR}"
  rm -rf "${XCFW_DIR}"
  echo "===> Clean complete."
  exit 0
fi

# -----------------------------------------------------------------------------
# 环境检测：必须在 macOS 上运行（依赖 xcrun / xcodebuild / lipo）
# -----------------------------------------------------------------------------
if ! command -v xcrun >/dev/null 2>&1; then
  echo "错误：未检测到 xcrun，请确认当前运行在 macOS 系统上并已安装 Xcode Command Line Tools。" >&2
  echo "      XCFramework 构建只能在 macOS 上完成。" >&2
  exit 1
fi

if ! command -v xcodebuild >/dev/null 2>&1; then
  echo "错误：未检测到 xcodebuild，请确认已安装完整 Xcode。" >&2
  exit 1
fi

# -----------------------------------------------------------------------------
# 检测 JDK include 路径（提供 jni.h 头文件）
# ios.c 通过 ios.h #include <jni.h>，需要 JDK 提供的头文件路径
# -----------------------------------------------------------------------------
if [ -z "${JAVA_HOME:-}" ] || [ ! -d "${JAVA_HOME}/include" ]; then
  # macOS 使用 /usr/libexec/java_home 查找默认 JDK
  if command -v /usr/libexec/java_home >/dev/null 2>&1; then
    JAVA_HOME="$(/usr/libexec/java_home 2>/dev/null || true)"
    export JAVA_HOME
  fi
fi

if [ -z "${JAVA_HOME:-}" ] || [ ! -d "${JAVA_HOME}/include" ]; then
  echo "错误：未找到 JDK（需要 jni.h 头文件）。请安装 JDK 或设置 JAVA_HOME 环境变量。" >&2
  echo "      macOS 上可通过 brew install --cask temurin 安装。" >&2
  exit 1
fi

JNI_INCLUDE_DIR="${JAVA_HOME}/include"
JNI_INCLUDE_OS_DIR="${JAVA_HOME}/include/darwin"
if [ ! -f "${JNI_INCLUDE_DIR}/jni.h" ]; then
  echo "错误：${JNI_INCLUDE_DIR}/jni.h 不存在，JDK 安装异常。" >&2
  exit 1
fi
if [ ! -d "${JNI_INCLUDE_OS_DIR}" ]; then
  echo "错误：${JNI_INCLUDE_OS_DIR} 不存在，JDK 安装异常（macOS 应有 darwin 子目录）。" >&2
  exit 1
fi

echo "===> Using JAVA_HOME: ${JAVA_HOME}"
echo "===> jni.h path:       ${JNI_INCLUDE_DIR}/jni.h"

# 获取 iOS 设备 SDK 路径
IOS_SDK_PATH="$(xcrun --sdk iphoneos --show-sdk-path)"
SIM_SDK_PATH="$(xcrun --sdk iphonesimulator --show-sdk-path)"

echo "===> Using iphoneos SDK:        ${IOS_SDK_PATH}"
echo "===> Using iphonesimulator SDK: ${SIM_SDK_PATH}"

# 准备中间目录
mkdir -p "${BUILD_DIR}/device"
mkdir -p "${BUILD_DIR}/simulator-arm64"
mkdir -p "${BUILD_DIR}/simulator-x86_64"
mkdir -p "${BUILD_DIR}/headers"

# 通用编译选项
# -I. 包含仓库根，使 ios.c 能通过 "touchcontroller/proxy/server/util/ringbuffer/ring_buffer.h" 路径找到头文件
# -I${JNI_INCLUDE_DIR} -I${JNI_INCLUDE_OS_DIR} 提供 jni.h 及其平台相关头文件
COMMON_CFLAGS="-O2 -fPIC -c -I. -I${JNI_INCLUDE_DIR} -I${JNI_INCLUDE_OS_DIR}"

# -----------------------------------------------------------------------------
# 第 1 步：编译 iOS 设备目标 (arm64)
# -----------------------------------------------------------------------------
echo "===> Compiling for iOS device (arm64)..."
clang ${COMMON_CFLAGS} \
  -target arm64-apple-ios14.0 \
  -isysroot "${IOS_SDK_PATH}" \
  -o "${BUILD_DIR}/device/ios.o" \
  "${SRC_IOS}"

clang ${COMMON_CFLAGS} \
  -target arm64-apple-ios14.0 \
  -isysroot "${IOS_SDK_PATH}" \
  -o "${BUILD_DIR}/device/ring_buffer.o" \
  "${SRC_RING}"

# 用 ar 创建设备静态库
echo "===> Creating device static library (libproxy_server_ios.a)..."
ar rcs "${BUILD_DIR}/libproxy_server_ios.a" "${BUILD_DIR}/device/"*.o

# -----------------------------------------------------------------------------
# 第 2 步：编译 iOS 模拟器目标 (arm64 + x86_64)
# -----------------------------------------------------------------------------
# 模拟器 arm64
echo "===> Compiling for iOS simulator (arm64)..."
clang ${COMMON_CFLAGS} \
  -target arm64-apple-ios14.0-simulator \
  -isysroot "${SIM_SDK_PATH}" \
  -o "${BUILD_DIR}/simulator-arm64/ios.o" \
  "${SRC_IOS}"

clang ${COMMON_CFLAGS} \
  -target arm64-apple-ios14.0-simulator \
  -isysroot "${SIM_SDK_PATH}" \
  -o "${BUILD_DIR}/simulator-arm64/ring_buffer.o" \
  "${SRC_RING}"

# 模拟器 x86_64
echo "===> Compiling for iOS simulator (x86_64)..."
clang ${COMMON_CFLAGS} \
  -target x86_64-apple-ios14.0-simulator \
  -isysroot "${SIM_SDK_PATH}" \
  -o "${BUILD_DIR}/simulator-x86_64/ios.o" \
  "${SRC_IOS}"

clang ${COMMON_CFLAGS} \
  -target x86_64-apple-ios14.0-simulator \
  -isysroot "${SIM_SDK_PATH}" \
  -o "${BUILD_DIR}/simulator-x86_64/ring_buffer.o" \
  "${SRC_RING}"

# 创建模拟器 arm64 静态库
echo "===> Creating simulator arm64 static library..."
ar rcs "${BUILD_DIR}/libproxy_server_ios_simulator_arm64.a" "${BUILD_DIR}/simulator-arm64/"*.o

# 创建模拟器 x86_64 静态库
echo "===> Creating simulator x86_64 static library..."
ar rcs "${BUILD_DIR}/libproxy_server_ios_simulator_x86_64.a" "${BUILD_DIR}/simulator-x86_64/"*.o

# -----------------------------------------------------------------------------
# 第 3 步：用 lipo 合并模拟器双架构为通用静态库
# -----------------------------------------------------------------------------
echo "===> Lipo merging simulator arm64 + x86_64..."
lipo -create \
  "${BUILD_DIR}/libproxy_server_ios_simulator_arm64.a" \
  "${BUILD_DIR}/libproxy_server_ios_simulator_x86_64.a" \
  -output "${BUILD_DIR}/libproxy_server_ios_simulator.a"

# 验证通用库架构
echo "===> Simulator library architectures:"
lipo -info "${BUILD_DIR}/libproxy_server_ios_simulator.a"

# -----------------------------------------------------------------------------
# 第 4 步：准备 Headers 目录（XCFramework -headers 参数需要）
# -----------------------------------------------------------------------------
echo "===> Preparing Headers directory..."
cp "${HEADER_IOS}" "${BUILD_DIR}/headers/"
cp "${HEADER_RING}" "${BUILD_DIR}/headers/"

# -----------------------------------------------------------------------------
# 第 5 步：用 xcodebuild 打包为 XCFramework
# -----------------------------------------------------------------------------
echo "===> Creating XCFramework..."
# 若已存在旧产物，先清理
rm -rf "${XCFW_DIR}"

xcodebuild -create-xcframework \
  -library "${BUILD_DIR}/libproxy_server_ios.a" \
  -headers "${BUILD_DIR}/headers" \
  -library "${BUILD_DIR}/libproxy_server_ios_simulator.a" \
  -headers "${BUILD_DIR}/headers" \
  -output "${XCFW_DIR}"

# -----------------------------------------------------------------------------
# 完成
# -----------------------------------------------------------------------------
echo ""
echo "===> XCFramework 构建完成！"
echo "     产物路径: ${XCFW_DIR}"
echo "     中间文件: ${BUILD_DIR} (可用 --clean 清理)"
echo ""
echo "集成到 Amethyst iOS Remastered 启动器："
echo "  cp -r ${XCFW_DIR} <Amethyst>/Natives/TouchController/"
echo "  CMake 会自动检测并使用 Level 1 XCFramework 集成方式"
