# Secure-Tasks_API
Practice project for safe developement and cybersecurity tools


## Configuración del Entorno

Este proyecto utiliza variables de entorno para la configuración. Antes de ejecutar la aplicación, crea los archivos `.env` y `/secure-tasks-api/.env.porperties` basado en `.env.example`.

### 1. Crear el archivo de entorno

Copia el archivo de ejemplo:

```bash
cp .env.example .env
```

O crea manualmente un archivo `.env` en la raíz del proyecto.

### 2. Configurar las variables de entorno

Completa los valores requeridos en `.env`.

Ejemplo:

```env
POSTGRES_USER=postgres
POSTGRES_PASSWORD=tu_contraseña_segura
POSTGRES_DB=base_de_datos_app
POSTGRES_PORT=5432
POSTGRES_HOST=localhost
NVD_API_KEY=tu_api_key_nvd
JWT_SECRET_KEY=tu_secreto_largo_y_seguro
```

### Variables de Entorno

| Variable | Requerida | Descripción | Ejemplo |
|-----------|------------|-------------|----------|
| `POSTGRES_USER` | Sí | Nombre de usuario de PostgreSQL | `postgres` |
| `POSTGRES_PASSWORD` | Sí | Contraseña de PostgreSQL | `miContraseñaSegura123` |
| `POSTGRES_DB` | Sí | Nombre de la base de datos PostgreSQL | `security_db` |
| `POSTGRES_PORT` | Sí | Puerto de la base de datos PostgreSQL | `5432` |
| `POSTGRES_HOST` | Sí | Host de la base de datos PostgreSQL | `localhost` |
| `NVD_API_KEY` | Opcional* | Clave API de National Vulnerability Database (mejora los límites de uso) | `xxxxxxxx` |
| `JWT_SECRET_KEY` | Sí | Clave secreta utilizada para firmar tokens JWT | `un_secreto_largo_y_aleatorio` |

> **Nota:** `NVD_API_KEY` puede ser opcional dependiendo de tu implementación. Sin ella, las solicitudes podrían estar limitadas por tasa (*rate-limited*).

### Recomendación para la Clave JWT

Utiliza una clave aleatoria y segura para `JWT_SECRET_KEY`.

Comandos de ejemplo para generarla:

**Linux / macOS**
```bash
openssl rand -base64 32
```

**Windows PowerShell**
```powershell
[Convert]::ToBase64String((1..32 | ForEach-Object { Get-Random -Maximum 256 }))
```

### 3. Configuración de la Base de Datos

Asegúrate de que exista una instancia de PostgreSQL en ejecución y que coincida con los valores configurados.

Ejemplo de configuración de conexión:

```text
Host: localhost
Puerto: 5432
Base de datos: base_de_datos_app
Usuario: postgres
Contraseña: tu_contraseña_segura
```

### 4. Ejecutar la Aplicación

Con docker:

```bash
docker-compose up -d
```

Desde la carpeta del proyecto de maven ```secure-tasks-api```:

Usando Maven:

```bash
mvn spring-boot:run
```

O compilar y ejecutar:

```bash
mvn clean install
java -jar target/<nombre-de-tu-app>.jar
```

