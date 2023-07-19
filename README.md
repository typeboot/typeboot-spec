# typeboot-spec
typeboot core specification

### build & test locally
```
./gradlew clean build testApp
docker build --no-cache -t spec .
docker run -v $(pwd)/output:/tmp/output -it spec
ls -lR $(pwd)/output
```
