# Directa24 Back-End Developer Challenge 

In this challenge, the REST API contains information about a collection of movie released after the year 2010, directed by acclaimed directors.  
Given the threshold value, the goal is to use the API to get the list of the names of the directors with most movies directed. Specifically, the list of names of directors with movie count strictly greater than the given threshold.   
The list of names must be returned in alphabetical order.  

To access the collection of users perform HTTP GET request to:
https://directa24-movies.wiremockapi.cloud/api/movies/search?page=<pageNumber>
where <pageNumber> is an integer denoting the page of the results to return.

For example, GET request to:
https://directa24-movies.wiremockapi.cloud/api/movies/search?page=2
will return the second page of the collection of movies. Pages are numbered from 1, so in order to access the first page, you need to ask for page number 1.
The response to such request is a JSON with the following 5 fields:

- page: The current page of the results  
- per_page: The maximum number of movies returned per page.  
- total: The total number of movies on all pages of the result.  
- total_pages: The total number of pages with results.  
- data: An array of objects containing movies returned on the requested page  

Each movie record has the following schema:  
- Title: title of the movie  
- Year: year the movie was released  
- Rated: movie rating  
- Released: movie release date  
- Runtime: movie duration time in minutes  
- Genre: move genre  
- Director: movie director  
- Writer: movie writers  
- Actors: movie actors  

## Posible Solutions
### Clone repository and complete the Function
  
Complete the function:

    List<String> getDirectors(int threshold)

getDirectors has the following parameter:
- threshold: integer denoting the threshold value for the number movies a person has directed

The function must return a list of strings denoting the name of the directors whose number of movies directed is strictly greater than the given threshold. 
The directors name in the list must be ordered in alphabetical order.


#### Sample Input For Custom Testing
    4  
#### Sample Output
    Martin Scorsese
    Woody Allen
    
The threshold value is 4, so the result must contain directors names with more than 4 movies directed.   
There are 2 such directors and names in the alphabetical order listed in Sample Output.

### Alternative: Spring Boot solution

An alternative solution to clonning the repository and implementing the function can be creating a new Spring Boot project with Rest endpoint:       
```
/api/directors?threshold=4
```
The solution can be shared via .zip file or sharing a github repository url via email. 

-

# Instrucciones para la Prueba:

Objetivo: Desarrollar una API REST que obtenga una lista de directores con más películas dirigidas que un umbral dado, ordenada alfabéticamente. La API proporcionada devuelve películas lanzadas después de 2010.

### Detalles:

Realizar solicitudes GET a https://eroninternational-movies.wiremockapi.cloud/.
Devolver una lista de directores con un número de películas dirigido mayor al umbral especificado, ordenada alfabéticamente.
Opciones de solución:

Clonar el repositorio de GitHub e implementar la función: List<String> getDirectors(int threshold).
Desarrollar la solución con Spring Boot exponiendo un endpoint: /api/directors?threshold=X. Ejemplo: dado un threshold=4, la respuesta debería ser un JSON con directores como Martin Scorsese y Woody Allen.

### Criterios de Evaluación

La API debe obtener y mostrar correctamente los datos de películas desde la API proporcionada.
El código debe estar bien estructurado, organizado y ser mantenible.
Seguir las mejores prácticas y principios de Java Spring.
Implementar características adicionales o mejores prácticas.
Incluir en el archivo README.md cualquier mejora que no se haya implementado y explicar por qué.

### Recomendaciones

#### Estructura:
	Mantén una estructura clara y lógica para tus clases y paquetes. Por ejemplo, separa las capas de controladores, servicios y repositorios.
#### Testing:
	Asegúrate de incluir pruebas unitarias y de integración. Utiliza mock para evitar hacer llamadas a la API real en los tests.
	Ejemplo de librerías para tests: JUnit, Mockito.
#### Configuración y variables:
	No uses URLs hardcoded. Utiliza variables de entorno para configuraciones como la base URL de la API externa.
#### Manejo de errores:
	Implementa un manejo de errores adecuado. Evita bloques try-catch excesivos y asegura que los errores sean informativos y manejados correctamente.
#### Dependencias:
	Utiliza solo las dependencias necesarias. Evita incluir dependencias que no se usen en el proyecto.
#### Limpieza y legibilidad del código:
	Mantén el código limpio y legible. Evita el abuso de lambdas si comprometen la legibilidad del código.
	Usa Lombok para reducir el boilerplate, pero asegúrate de que se utilice correctamente si se incluye.
#### Documentación y Swagger:
	Incluye documentación para tu API. Utiliza Swagger para generar documentación automática de los endpoints.
#### Formato de respuesta JSON:
	Asegúrate de que el formato del JSON de respuesta sea correcto y cumpla con los requisitos.
#### Readme.md:
	Explica claramente cómo configurar y ejecutar tu aplicación.

#### Adiciono más información por si el link inicial no funciona:
	Hice una prueba y la API está funcionando correctamente. Amplíe el enlace para recibir datos, usé este: https://eroninternational-movies.wiremockapi.cloud/api/movies/search?page=2
	Es el mismo solo que apunté a las películas, y sin la paginación sería este: https://eroninternational-movies.wiremockapi.cloud/api/movies/search?