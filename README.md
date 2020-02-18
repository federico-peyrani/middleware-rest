# REST-based image server

The server is broken down in two main modules: `http.HTTPManager` and `api.APIManager`, responsible of handling the 
requests to the static pages and the API endpoints respectively.

## Getting started

Launch the `main` method in the `Main` class, for the time being, the system stores all the information in primary 
storage only, meaning that any information about users and uploaded images is lost once the JVM is stopped.

## More info

Breakdown of the API endpoints: <https://app.swaggerhub.com/apis-docs/federico-peyrani/middleware-technologies>