# Movie Review and Rating Service

Below are the microservices involved in the project

## Movie-Info-Service

Description: The Movie Review Service manages movie information.                                                            
Configuration:
port: 8080

## Movie-Review-Service

Description: The Movie Review Service manages user reviews and ratings for movies.                                                            
Configuration:
port: 8081

## Movie-Service

Description: The Movie Service handles movie-related operations and communicates with the Movie Info Service and Movie Review Service.
Configuration:
port: 8082

## Prerequisites

Before you start, make sure you have the following prerequisites:

1. Java Development Kit (JDK)
2. Gradle

## Configuration

Each microservice has its own configuration settings specified in their respective application.yaml files. Review and update these files based on your requirements.

## Local Deployment

To deploy the microservices locally, follow these steps:
1. Build the microservices using Gradle.
2. Ensure that the microservices' configurations(application.yaml) match your local environment.
3. Start the services locally using the SpringBootApplication file located in src/main/com/ractivespring directory.

## Usage

Here's how to use the movie review and rating system:
1. Access the Movie Info Service to browse and search for movie information.
2. Use the Movie Review Service to leave ratings and reviews for movies.
3. Utilize the Movie Service to access both movie information and user reviews.