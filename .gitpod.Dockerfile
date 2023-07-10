FROM gitpod/workspace-full

USER gitpod

RUN bash -c ". /home/gitpod/.sdkman/bin/sdkman-init.sh && \
    sdk install java 20.0.1-tem && \
    sdk default java 20.0.1-tem && \
    sdk install java 22.1.0.1.r17-gln"
