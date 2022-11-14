# Deployment

The workflow bot is a Java application built with
the [BDK Spring Boot integration](https://symphony-bdk-java.finos.org/spring-boot/core-starter.html).
The [Getting started](./getting-started.md) guide explains how the Symphony Generator can be used to create a sample
project with configuration files.

By default, the WDK does not persist state except in memory, so restarting the bot means all running
workflow's state is lost. In order to have a persistent database, you need to change Camunda specific configuration (see [Persistent database part](./deployment.md#camunda-specific-configuration)).

_Running multiple instances of the bot for high availability is not yet supported._

## Configuration

As the Spring Boot integration of the BDK is used, most of
the [principles](https://docs.spring.io/spring-boot/docs/current/reference/html/features.html#features.external-config)
from the framework applies here.

### Workflow bot specific configuration

`wdk.workflows.path`: The path to the folder containing SWADL files to load on startup and to watch for changes. Defaults
to _./workflows_, relative to the working directory when starting the bot.

`wdk.properties.monitoring-token`: The token to authenticate requests to the [monitoring api](#monitoring). Defaults to an empty
String. It can be set as an environment variable in the run configuration. Not setting the monitoring-token and keeping
its default value disables the monitoring api.

`wdk.properties.management-token`: The token to authenticate requests to the [management api](#management). Defaults to an empty
String. It can be set as an environment variable in the run configuration. Not setting the management-token and keeping
its default value disables the management api.

### BDK specific configuration

Symphony backend URL and credentials are configured as any bot. The BDK documentation applies here:

- https://symphony-bdk-java.finos.org/spring-boot/core-starter.html
- https://symphony-bdk-java.finos.org/configuration.html

The BDK configuration is under the `bdk` key.

### Camunda specific configuration

As Camunda is used internally to run workflows, properties for the Camunda Engine can be configured too. The available
properties are listed
in [Camunda's documentation](https://docs.camunda.org/manual/latest/user-guide/spring-boot-integration/configuration/#camunda-engine-properties).

#### Job execution
We are mainly interested in the `camunda.bpm.job-execution` properties to configure the background process running
workflows, for instance the wait time to detect new events to process. It is configured with a low value by default to
ensure the bot is reactive.

#### Retry on activity/task errors
Camunda is configured to retry on activity/task errors. Part of the error handling is done via the BDK that already
support retrying on failed API calls and then error handling can also be done when writing workflows with
the [activity-failed event](./reference.md#activity-failed).

#### Persistent database
In order to use a persistent database, you need to change `spring.datasource.url` to `jdbc:h2:file:./data/wdk_database;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`.

In this case, a local disk file will be used to store data. It can also be changed to any database supported by Camunda (see [Camunda Database Configuration documentation](https://docs.camunda.org/manual/7.15/user-guide/process-engine/database/database-configuration/)).

In case JDBC driver is missing, you might need to add it to the bot classpath in folder _/lib_ . 

### Spring Boot specific configuration

The [common application properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
from Spring Boot apply here too.

The logging levels can be interesting to tune for troubleshooting:

- `logging.level.com.symphony` by default set to DEBUG
- `logging.level.org.camunda` by default set to WARN

The bot is listening on port 8080 by default to receive requests to trigger workflows. Exposing such service can be
disabled with:

- `spring.main.web-application-type` can be set to _none_ to prevent the bot from listening on any port

Monitoring can be disabled too via properties, by default it is enabled on port 8081 and endpoints are accessible to
retrieve metrics.

- `management.endpoints.web.exposure` can be used to control which endpoints to expose

## Monitoring

By default, metrics are available on port 8081 under the `/actuator` path.

For instance:

- http://127.0.0.1:8081/actuator/metrics to list the available metrics
- http://127.0.0.1:8081/actuator/prometheus for metrics that can be used Prometheus

Workflow specific metrics are exposed, to get basic statistics on the number of deployed or running workflows,
including:

- workflow.activity.completed
- workflow.activity.running
- workflow.deployed
- workflow.process.completed
- workflow.process.running

More metrics are exposed by the WDK public api under /wdk. See [http://localhost:8080/wdk/swagger-ui/](http://localhost:8080/wdk/swagger-ui/#)

## Management

Users can deploy, update and delete a workflow through WDK management API under `/manangement/workflows` path.

Note: 

- the API requests require the management token in the header [X-Management-Token](#workflow-bot-specific-configuration)
- remember to use [publish property](./reference.md#properties) in order to create a workflow without deploying.

For more details, see [http://localhost:8080/wdk/swagger-ui/](http://localhost:8080/wdk/swagger-ui/#)

## Logging

By default, logs produced by the bot goes to the console and we [recommend](https://12factor.net/logs) to keep it that
way. For production deployments you might want to consider putting centralized log aggregation in place.

Log levels can be tuned with property such as:

- `logging.level.com.symphony` by default set to DEBUG
- `logging.level.org.camunda` by default set to WARN
- `logging.level.root` to
  configure [all loggers](https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/#features.logging.log-levels)
  at once

[MDC](http://logback.qos.ch/manual/mdc.html) entries are also set when executing activities:

- PROCESS_ID
- ACTIVITY_ID

### Audit trail

A specific logger named `audit-trail` is provided as a way to trace who started which workflow and activities and when.
It uses a comma separated format, so it can easily be parsed and analyzed.

For instance:

```
2021-09-15 11:52:18.121  INFO 71942 --- [   scheduling-1] audit-trail: event=deploy_workflow, deployment=9cf775d5-160a-11ec-9580-d26323b87524, deployment_name=clear, process_key=clear-27900169-c66b-4640-a51f-b819b2806c6f
```

Setting `logging.level.audit-trail` to WARN would disable such audit trails.

## Troubleshooting

To troubleshoot the workflow bot we recommend running it locally in a test environment if possible.

Here are a few steps to help:

- Is the workflow bot process starting?
  - Is Java (11 required) installed?
  - Is the command line to start it valid? The referenced JAR file accessible?
- If the workflow bot starts, does the datafeed loop start successfully?
  - The first step would be authenticating against the Symphony backend (pod) and the KeyManager, does it work?
    - Are the provided credentials accessible and configured in ACP?
  - Then the workflow bot contacts the Agent to listen to datafeed events, does it work?
  - Any networking issues could be caused by firewall, DNS or proxy misconfiguration. Make sure those are set according
    to your environment.
- Then the workflow bot tries to load workflows from the workflows folder (`wdk.workflows.path` property)
  - Is the folder accessible to the bot?
- The deployment of workflows can fail if those are invalid
  - Make sure to configure the [JSON Schema](./concepts.md#swadl) to check the workflow's validity from your editor
- In every situation the logs produced by the bot are your main source of information, make sure to enable the DEBUG
  level if needed

Good Luck!
