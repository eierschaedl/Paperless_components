## Applikation starten
- Command ```docker-compose up --build``` im root Verzeichnis des Projektes ausführen.

RestAPI wird in eigenem Container auf ```Port 8081``` gestartet

## Ports
Frontend: 81
Datenbank: 5432
RabbitMQ Frontend: 15672
MinIO Frontend: 9090
Kibana Frontend: 5601

## Credentials "Username"/"Passwort"
Postgres Datenbank: "user"/"my_cool_password"
RabbitMQ: "rabbitmqadmin"/"rabbitmqadmin"
MinIO: "minioadmin"/"minioadmin"