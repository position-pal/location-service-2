# Location Service

PositionPal service for location tracking.
**Still, work in progress!**

## Structure

The project is structured by implementing hexagonal architecture, mapping layers to Gradle submodules.

![repo-structure](http://www.plantuml.com/plantuml/svg/TL71QXin4BthAqHo2yYNzhI49QvzCPIGMocXXCburXRMQiHejas8y-zbLzh4QToBfFVcZVJUw2OhY0vzi4A9NLOPK0SXO_B1nmG2lKNk4qUAQb-CjTP-0ppiX8UuNN5WkRwc9oM94DhM_jXxfV-f20U5nq1jQoyHkmxnl58fQzjQV8PeNa-Ch47X4JBzqs2_-zrUVr88_ET4VSlRJyfYpxmdkcW28wZdw3B9RuzqKLFUDyXzpp7_if3jgd9Rxi77YLfN2b8AljmnyKjJ2qaeKzJtobqfU5wW5wHcMXsoqCENIJ0HzdIWRnqxIUYk9jLu_2Yg3_hO2RnzuQJTb1BBRh5NJLzpKxPwLG5dNaJk5_gSA4D72pWSe1aNXs78IbAxH-gCaRLrGtB7jWvjESCoiBiPBYTO8nzTe4vT7L59U7GoVknCRkR60cVhHrT3RknO73sPLsDkA6w7Flm5 "repo-structure")

## Installation

To build the project, please use Gradle.

For the correct operation of the service it is necessary to have a `.env` file in the root of the project with the following variables:

```env
MAPBOX_API_KEY=<your_mapbox_api_key>
```
