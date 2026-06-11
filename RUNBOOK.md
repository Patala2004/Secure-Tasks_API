# Runbook

Este documento busca detallar las acciones a tomar en caso de filtración de secretos del proyecto.

Primero se va a listar los secretos que forman el proyecto, definiendo la severidad de la posible filtración. Luego se explicará que accición tomar para cada secreto o grupo de secretos.

## Secretos del proyecto

El proyecto actualmente tiene 7 secretos:

- JWT_SECRET_KEY: La clave que se utiliza para firmar los JWTs (JSON Web Tokens) utilizados por la API. Con esta clave se puede validar los tokens enviados por los usuarios y crear tokens nuevos.
- NVD_API_KEY: Llave de la API de la NVD (National Vulnerability Database) del NIST. Sirve para tener más prioridad a la hora de realizar peticiones a la base de datos. Se utiliza a la hora de realizar comporbaciones de vulnerabilidades conocidas en los componentes de la aplicación. Sin esta clave el sistema sigue funcionando, pero el SCA (Análisis estático de componentes) tardaría más.
- POSTGRES_USER: El usuario de postgres con el que se accede a la base de datos.
- POSTGRES_DB: El nombre de la base de datos de postgres utilizada por la aplicación.
- POSTGRES_HOST: La dirección o IP del servidor de postgres donde se encuentra la base de datos utilizada por la aplicación. En caso de estar en el mismo servidor que la aplicación suele ser `localhost`, `127.0.0.1` o `0.0.0.0`.
- POSTGRES_PORT: El puerto del servidor de postgres. Suele ser `5432`.

Estos secretos se pueden dividir en dos grupos principales:

- *Secretos de postgres:* Incluye `POSTGRES_PASSWORD`, `POSTGRES_USER`, `POSTGRES_DB`, `POSTGRES_HOST` y `POSTGRES_PORT`. Son las variables que se utilizan para acceder a la base de datos desde la aplicación y, en caso de despliegue por docker, inicializar la base de datos.
- *Resto:* Incluye `JWT_SECRET_KEY` y `NVD_API_KEY` . Son el resto de variables. A diferencia de las variables de secretos de psotgres, estas variables tienen todas diferentes remedios o acciones a tomar en caso de filtrado.

La gravedad de la filtración de las claves es la siguiente:

| Secreto            | Severidad en caso de filtrado| Motivo |
| ------------------- | -----------------: | :--|
| `JWT_SECRET_KEY`    |         Critical | Permite crear tokens the autenticación forjados |
| `POSTGRES_PASSWORD` |             High | Compromete la base de datos |
| `POSTGRES_USER`     |             Medium | Compromete la base de datos | 
| `POSTGRES_DB`       |                Low | Facilita acceso a la base de datos pero se necesita la clave y el usuario para accederla |
| `POSTGRES_HOST`     |                Low | Facilita acceso a la base de datos pero se necesita la clave y el usuario para accederla |
| `POSTGRES_PORT`     |                Low | Facilita acceso a la base de datos pero se necesita la clave y el usuario para accederla |
| `NVD_API_KEY`       |                Low | No se puede poner en riesgo la aplicación con esta clave |

## Acciones

### Filtrado del secreto JWT

**Impacto:** Atacantes pueden forjar tokens de autenticación y descifrar tokens de usuarios reales para intentar exfiltrar información.

**Acciones:**

1. Generar una clave JWT nueva:

```bash
openssl rand -base64 32
```

2. Actualizar la aplicación y el sistema:
	- en el fichero `.env`
	- en las variables CI/CD de Github

3. Reiniciar la aplicación para que tomen efecto los cambios. Esto invalidará todas las sesiones actuales.
4. Minitorizar logs de la aplicación para detectar actividad sospechosa e intentos de rojado de tokens JWT.

### Filtrado de la NVD API Key

**Impacto:** Otra persona puede enviar peticiones a la NVD bajo la identidad del dueño de la clave API. Esto podría causar baneaos de la API de la NVD del perfil proporcionado. No afecta al funcionamiento del sistema en absoluto.

**Acciones:**

1. Conseguir una clave nueva
	- Acceder a [la página de petición de claves API de NVD](https://nvd.nist.gov/developers/request-an-api-key) y pedir una nueva clave
	- Conseguir la clave accediendo a través del enlace enviado por correo electrónico. Esto invalidará la clave antigua que ha sido filtrada.
2. Actualizar la clave en:
	- ficheros `.env`
	- variables CI/CD de Github
	- Si se usa la misma llave para otros proyectos, también ahí, ya que la clave antigua ha sido invalidada.

### Filtrado de contraseña o usuario de postgres


**Impacto:** Pone en peligro la integridad, la disponibilidad y la confidencialidad de la base de datos, ya que con estas claves se puede conseguir acceso a la base de datos con permisos de lectura y escritura.

**Acciones:**

1. Rotar secretos

En caso de la contraseña, esto se puede hacer con el comando:

```SQL
ALTER USER your_user WITH PASSWORD 'new_secure_password';
```

En caso del nombre del usuario se puede lograr con:

```SQL
ALTER ROLE old_user RENAME TO new_user;
```

2. Actualizar:
	- `.env`
	- Variables CI/CD de Github actions
	- Contenedores
3. Reiniciar servicios y contenedores
4. Revisar logs. Buscar:
	- Accesos de login a la base de datos fallidos
	- Queries inesperadas
	- Exportaciones del contenido de la base de datos


## Detección de filtrados de secretos

Para revisar si se ha filtrado un secreto se pueden revisar los siguientes puntos:

- Commit de git incluye `.env` o un secreto hardcodeado
- Scanner de secretos (i.e. gitleaks) lanza una alerta
- Uso de API inesperado o inusual
- Logs de la CI exponen secretos

Estas son revisiones de formas comunes en las que se pueden filtrar accidentalmente los secretos. Se pueden filtrar de multiples formas no contempladas en esta lista y la filtración podría no ser detectada. Si se sospecha una filtración hay que analizar la filtración sospechada para ver como se puede comprobar si la filtración ha podido ocurrir o no. En caso de sospecha siempre es mejor rotar los secretos, aunque no se esté seguro.

## Checklist de prevención

Para prevenir el filtrado de secretos hay que asegurarse de incluir:

- `.env` en el fichero `.gitignore`
- sistema de escaneo de secretos. Idealmente un gitleaks a nivel local para detectarlo antes del commit, pero además otro en la CI para detectar secretos ya filtrados y actuar acorde a ello.
- asegurarse de que no se añadan secretos a logs
- no compartir `.env` por chat o correos
- rotar secretos periodicamente

