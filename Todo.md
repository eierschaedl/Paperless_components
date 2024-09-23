## Sprint 1

- [x]  1. Java Project Setup
- [x]  2. Remote Repository setup, all team members are able to commit/push
- [ ]  3. REST Server created  
          Endpoints defined by the team
- [ ]  4. Requests to endpoints return a hardcoded result
- [ ]  5. Docker:
    - [ ] Dockerfile für API erstellen
    - [ ] Initial docker-compose.yml, used to run the REST-server inside a container

---
### Code snippets How-To: File ablegen/abspeichern
import java.io.File;

// Fixer Pfad zur Dateiablage am Server  
private static String UPLOAD_DIR = "/path/to/upload/directory/";

// Filename vom Upload holen  
String fileName = file.getOriginalFilename();

// Speicherort der Datei definieren  
File destination = new File(UPLOAD_DIR + fileName);

// File am Server abspeichern  
file.transferTo(destination);

---
## Sprint 2
- [ ] 1. Webserver Nginx lokal installieren
- [ ] 2. React aufsetzen
    - `npm run build`
- [ ] simple Dashboard
- [ ] Webpage communication with the REST server at least some "Hello World" message
- [ ] 3. Docker:
    - [ ] Dockerfile für Webserver erstellen
    - [ ] docker-compose.yml anpassen (zusätzlicher service "fronend")