# typeboot-spec
typeboot core specification

### build & test locally
```
./gradlew clean build testApp
docker build --no-cache -t spec .
docker run -v $(pwd)/output:/tmp/output -it spec

docker run -v $(pwd)/database/postgres:/tmp/input -v $(pwd):/tmp/conf -v $(pwd)/output:/tmp/output -it typeboot/typeboot-spec:0.11 /tmp/conf/docker-typeboot.yaml

ls -lR $(pwd)/output
```
