# Proyecto_CAPSTONE
Inicio de la Fase 1 del proyecto, subir todos los documentos a la carpeta documentos capstone.
Comandos necesarios para la manipulacion del codigo
.\mvnw.cmd clean compile (para limpiar la cache y poder actualizar cambios) 
.\mvnw.cmd spring-boot:run (para poder iniciar el proyecto y que corra el programa)
http://localhost:8080 (para poder ver la pagina)
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"( del caso que el puerto 8080 este en uso por otro dispositivo).


Credenciales de Cliente y Administrador por default para hacer pruebas.
Cuenta de Administrador        Cuenta de Cliente
Usuario:admin                 Usuario:cliente 
Contraseña: admin123           Contraseña:cliente123
Codigo de registro admin:DUALISTA2026
comandos para ejecutar el código dentro de otro dispositivo instalando las dependencias:
winget install --id EclipseAdoptium.Temurin.17.JDK -e --silent --accept-package-agreements --accept-source-agreements

$env:Path = [System.Environment]::GetEnvironmentVariable("Path","Machine") + ";" + [System.Environment]::GetEnvironmentVariable("Path","User")
$env:JAVA_HOME = Split-Path (Split-Path (Get-Command java).Source)

java -version 

Del caso que no funcione el anterior comando ejecutar este en la terminal.

$mvnw = Get-ChildItem -Filter "mvnw.cmd" -Recurse -ErrorAction SilentlyContinue | Select-Object -First 1
if ($mvnw) {
    cd $mvnw.DirectoryName
    .\mvnw.cmd spring-boot:run
} else {
    Write-Host "No se encontró mvnw.cmd en esta ruta ni en subcarpetas." -ForegroundColor Yellow
}
