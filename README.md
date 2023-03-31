# Running the application locally

Run both the frontend Vue.js application and the Micronaut backend in two terminal windows.

## Run the Vue.js frontend

In the `vue-frontend` directory, to install the dependencies:

```bash
npm install
```

Then to run the frontend local server:

```bash
npm run dev
```

Open your browser at http://localhost:5173/
The Vue.js frontend will proxy calls to the Micronaut backend service.

## Run the Micronaut backend

In the `micronaut-backend` directory:

```bash
./gradlew run
```

# Building the standalone JAR

In the `micronaut-backend` directory:

```bash
./gradlew shadowJar
```

## Assemble the front and the backend

In project base directory:

```bash
./gradlew assembleFrontAndBack
```

## Build the container image with Docker

In project base directory:

```bash
docker build . --tag eu.gcr.io/stootistics/micronaut-backend --platform linux/amd64
```

## Run the built container locally

In project base directory:

```bash
docker run -it --rm -p8080:8080 eu.gcr.io/stootistics/micronaut-backend
```

# Deploy to Cloud Run

## Preparation of your project with `gcloud`

```bash
gcloud init
gcloud config set run/region europe-west1
gcloud config set run/platform managed
gcloud auth configure-docker
gcloud components install docker-credential-gcr
```

## Pushing the container to the registry

```bash
docker push eu.gcr.io/stootistics/micronaut-backend
```

## Service deployment

```bash
gcloud run deploy stootistics --image gcr.io/stootistics/micronaut-backend --allow-unauthenticated
```

# Miscellaneous

This project is not an official Google project.

It is released under the [Apache License 2.0](LICENSE).

It was developed as a demonstration for a conference by [@glaforge](https://github.com/glaforge).

