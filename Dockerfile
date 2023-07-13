FROM cloudnativek8s/microservices-java17-alpine-u10k:v1.0.27 

RUN mkdir -p /opt/app && mkdir -p /opt/app/conf && mkdir /opt/app/libs

COPY --chown=user:app database /opt/app/examples
COPY --chown=user:app build/libs/*uber.jar /opt/app/libs/typeboot-spec-uber.jar
COPY --chown=user:app docker-typeboot.yaml /opt/app/conf/.typeboot.yaml
COPY --chown=user:app entrypoint.sh /opt/app/entrypoint.sh


WORKDIR "/opt/app"

ENTRYPOINT ["/opt/app/entrypoint.sh"]

CMD ["/opt/app/conf/.typeboot.yaml"]
