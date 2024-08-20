# Location Service

PositionPal service for location tracking.
**Still, work in progress!**

## Structure

The project is structured by implementing hexagonal architecture, mapping layers to modules, leveraging Gradle submodules.

![repo-structure](http://www.plantuml.com/plantuml/svg/TL5DJm8n4BttLpHuCoaNFMt81eGBYJ7SZ376bDbPQEXscdRMZxZyTyEs2wXuQVlUlFbudMba7RXjh0peIKKEBN0RK8YuCyxhe1i9r7Xow5X-2IcblDEmsmHgNJKdIs4omG_6uBGjI_cuDOp_gH1QrckBPIcl0EeQuBAICN9PIlZKJkpF1bberciTyLU2l3URBJ-XjVH-fdwWweyoZITEZaQ59T1ioAc8uSq0ZhBBvmAeQSZ55wcZd6UkeaORrrEBJ6dlhKarUducSi07ZEZu55cbWA4nc01OzAS22lXwqmjwGfv_w35Z4poewrvrUFuKvOR6rK4VLzXhbob4OJFUCllRMAGhbqhWGh65LArNNPcDpmQOJAy6EEjX7hjJ5cjMsMQQibFXi9o1N0w92GxjEoMNOY_455tLDlOR "repo-structure")

## Installation

To build the project, please use Gradle.

For the correct operation of the service it is necessary to have a `.env` file in the root of the project with the following variables:

```env
MAPBOX_API_KEY=<your_mapbox_api_key>
```
