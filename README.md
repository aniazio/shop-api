# Shop API

## Final project for "Web Development with Java and Spring" course

### Starting the app

Before starting the app you need to start docker containers with `docker-compose up -d` command run in the terminal.
After this step app is ready to start.

There are two profiles:

- `local` with H2 embedded database (but required docker container for redis)
- `dev` with PostgreSQL database in docker container

To restart data in `dev` profile run `docker-compose down -v` and then `docker-compose up -d` again.

### Documentation

OpenAPI documentation can be found in `docs` folder, along with entity diagram and demo Postman collection.
