FROM gitpod/workspace-full
FROM gitpod/workspace-full-vnc


USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 17.0.12-amzn && \
    sdk default java 17.0.12-amzn"