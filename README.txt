To run this demo, you will need a linux VM installed with Docker and
docker-compose. In my testing, I used a CentOS 7 VM.

You will not be able to use Docker for Windows or Docker for Mac because the
OneAgent will not install on those linux VMs.

To get started,

1. Create a new custom application by going to:
Deploy Dynatrace / Digital touchpoint monitoring / Create custom application.

2. Copy the application ID and beacon URL, and set these in the .env file.

3. Install the OneAgent in a linux host running Docker.

4. Start up the client and server by executing:
docker-compose up

5. Turn off deep monitoring of the client process group (com.kwkoo.Temp
using "openkitdemo/client"). If you don't do this, the application screen will
not show anything in "Called services" (the Java agent will override the HTTP
header set by OpenKit).

