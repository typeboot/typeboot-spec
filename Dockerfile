FROM openjdk:11-jre-slim

RUN mkdir /opt/app

RUN groupadd --system --gid=1000 app\
    && useradd --system --no-log-init --gid app --uid=1000 app \
    && chown -R app:app /opt/app
USER app

COPY --chown=app:app database /opt/app/examples
COPY --chown=app:app build/libs/*uber.jar /opt/app/typeboot-spec-uber.jar
COPY --chown=app:app docker-typeboot.yaml /opt/app/.typeboot.yaml
COPY --chown=app:app entrypoint.sh /opt/app/entrypoint.sh


WORKDIR "/opt/app"

ENTRYPOINT ["/opt/app/entrypoint.sh"]

CMD ["/opt/app/.typeboot.yaml"]