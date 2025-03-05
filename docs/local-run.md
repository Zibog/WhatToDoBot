# Local Run

## Setup

- Copy `env/.env.template` to `env/.env` and fill in the required values. Do not commit this file.

## Docker

- Build the Docker image: `docker build -t what-to-do-bot .`
- Run the Docker container: `docker run -d --name what-to-do-bot --env-file env/.env what-to-do-bot`