FROM gitpod/workspace-full
FROM gitpod/workspace-full-vnc


USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 23.0.2-amzn && \
    sdk default java 23.0.2-amzn"