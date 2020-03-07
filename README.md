# REST-based image server

The server is broken down in two main modules: `http.HTTPManager` and `api.APIManager`, responsible of handling the 
requests to the static pages and the API endpoints respectively.

## Getting started

Launch the `main` method in the `Main` class. If you want to use an external database to permanently store the information
of the system (created users and images), pass as the only argument the absolute path to the SQL database to user.
If no argument is passed, the application will store all the information in primary storage only, meaning that any information 
about users and uploaded images is lost once the JVM is stopped.

Connect to <http://localhost:4567/login> with a browser to test the functionalities of the system.

### Launch modules separately

Both modules (HTTP server and API server) can be launched independently of one another by using the respective `main` 
methods, as they do not strictly rely on one another and can effectively run on separate JVMs.

## API endpoints

Breakdown of the API endpoints: <https://app.swaggerhub.com/apis-docs/federico-peyrani/middleware-technologies> (deprecated)

## SQL Database

The application will automatically create the necessary tables upon startup, so no further action is needed beside the
creation of the database file itself.