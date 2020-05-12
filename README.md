# REST-based image server

The server is broken down in two main modules: `http.HTTPManager` and `api.APIManager`, responsible of handling the
requests to the static pages and the API endpoints respectively.

## Getting started

Launch the `main` method in the `common.Main` class. If you want to use an external database to permanently store the information
of the system (created users and images), use the environment variables provided for this purpose.
If no argument is passed, the application will store all the information in primary storage only, meaning that any information
about users and uploaded images is lost once the JVM is stopped.

Connect to <http://localhost:4567/login> with a browser to test the functionalities of the system.

### Launch modules separately

Both modules (HTTP server and API server) can be launched independently of one another by using the respective `main`
methods, as they do not strictly rely on one another and can effectively run on separate JVMs.

## SQL Database

The application will automatically create the necessary tables upon startup, so no further action is needed beside the
creation of the database file itself.

## Deployment

The deployment is made using [Docker](https://docs.docker.com/engine/docker-overview/) and
[Docker Compose](https://docs.docker.com/compose/). It is based on three containers:
one provides the HTTP service, other provides de API service and the lastone
is the database server which is connected to both containers.

```shell
docker-compose up
```

If some changes are made in the code, it is needed to rebuild the image. It could be done
at the same time as the deployment by using the following command.

```shell
docker-compose up --build
```

If the user wants to build the image without running it, the following command can be used.

```shell
docker build -t middleware/rest .
```

## Environment variables

Some environment variables are provided to have a minimum configuration for the service.

| Variable | Default value | Purpose |
| -------- | ------------- | ------- |
| `PORT`          | 4567 | Port where server listens to |
| `DATABASE_HOST` | localhost | Hostname or IP of the database server |
| `DATABASE_PORT` | 3306 | Port of database server |
| `DATABASE_NAME` | "" | Database name (mandatory) |
| `DATABASE_USER` | "" | User for connecting to the database |
| `DATABASE_PASS` | "" | Password for connecting to the database |

## Scaling

Thanks to the NGINX integration, multiple instances of the API server can be deployed to balance the overall load of the 
system across different replicas. The file `/src/main/resources/nginx.conf` contains the necessary configurations to 
support exactly 4 replicas (as of now, the only way to define replicas is by statically defining them in the config file):
launching any number of replicas different from 4 will result in the load balancing server halting itself.
To automatically deploy the system, the following command can be used.
 
```shell
docker-compose -f docker-compose.loadbalancer.yaml up --scale api=4
```

Then simply connect to port `80` to access the web app: <http://localhost>.

### Load Balancing Policy

In order to guarantee that the requests from a client are always forwarded to the same instance (needed so that session
states can be stored across multiple requests), the policy employed by the load balancing system is _ip address hashing_. 