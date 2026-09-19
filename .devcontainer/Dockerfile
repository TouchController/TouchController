FROM mcr.microsoft.com/devcontainers/base:debian13

ENV ANDROID_HOME=/opt/android-sdk
ENV ANDROID_NDK_HOME=${ANDROID_HOME}/ndk/27.3.13750724
ENV PATH=${PATH}:${ANDROID_HOME}/cmdline-tools/latest/bin:${ANDROID_HOME}/platform-tools

RUN apt-get update && apt-get install -y openjdk-21-jdk curl unzip zip git build-essential && rm -rf /var/lib/apt/lists/*

RUN mkdir -p ${ANDROID_HOME}/cmdline-tools \
    && curl -L https://dl.google.com/android/repository/commandlinetools-linux-9477386_latest.zip -o /tmp/tools.zip \
    && unzip /tmp/tools.zip -d ${ANDROID_HOME}/cmdline-tools \
    && mv ${ANDROID_HOME}/cmdline-tools/cmdline-tools ${ANDROID_HOME}/cmdline-tools/latest \
    && rm /tmp/tools.zip

RUN yes | sdkmanager --licenses && sdkmanager "platform-tools" "platforms;android-36" "build-tools;33.0.2" "ndk;27.3.13750724"

RUN wget https://github.com/bazelbuild/bazelisk/releases/download/v1.27.0/bazelisk-amd64.deb -O bazelisk.deb && dpkg -i bazelisk.deb
RUN wget https://github.com/bazelbuild/buildtools/releases/download/v10.0.1/buildifier-linux-amd64 -O /usr/local/bin/buildifier && chmod a+x /usr/local/bin/buildifier
RUN wget https://github.com/bazelbuild/buildtools/releases/download/v10.0.1/buildozer-linux-amd64 -O /usr/local/bin/buildozer && chmod a+x /usr/local/bin/buildozer
