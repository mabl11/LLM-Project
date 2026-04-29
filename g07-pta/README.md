# Java Projekt-Template fuer PTA

## Zweck
Dieses Projekt dient im Modul PTA als 
Vorlage für Java-Projekte. Es nutzt einen auf 
[Apache Maven](https://maven.apache.org/) basierenden (und somit IDE-unabhängigen)
Build und definiert die dafür notwendige, standardisierte Verzeichnisstruktur.
Es basiert auf dem OOP_MAVEN_TEMPLATE (welches z.B. im Modul OOP verwendet wird) und den RAG Examples von [langChain4j](https://github.com/langchain4j/langchain4j-examples).

Damit man das NaiveOllamaRAGExample Beispiel starten kann, muss eine lokales LLM im Ollama Container unter http://localhost:11434 verfügbar sein.

Dazu muss [Ollama](https://ollama.com) installiert sein, ein LLM Modell heruntergeladen und auch gestartet sein.
Hinweise dazu, wie das geht, findet man unter https://docs.ollama.com/quickstart. 

Wir empfehlen das Modell **mistral:7b**, da es klein genug ist auf einem Notebook zu laufen.

Mehr Informationen zu LangChain4J, LLM und RAG findet man hier:
* [LangChain4J Doku](https://docs.langchain4j.dev) 

* [LangChain4J Doku Bot](https://chat.langchain4j.dev)


## Verwendung
Das Projekt kann einfach kopiert und umbenannt werden. In NetBeans, IntelliJ und
Visual Studio Code kann es **direkt** geöffnet werden, in Eclipse ist
ein **Import** des Projektes (als `Existing Maven Project`) notwendig.
Die beste (weil vollständige) Integration ist in NetBeans gegeben, in allen
anderen IDEs kann der Maven Build manuell ausgelöst werden. Nach dem Kopieren
in ein neues Verzeichnis (für ein neues Projekt) wird sehr empfohlen:
* den Namen des Verzeichnisses,
* den Namen des Projektes in der IDE, sowie
* die `ArtifactID` und `Name` (zwei Elemente im `pom.xml`)
zwecks Vereinfachung und Konsistenz **synchron** zu halten.
In NetBeans erreichen Sie das durch ein einfaches "Rename"-Refactoring.
Sie können es aber auch von Hand mit einem Texteditor erledigen.

Hinweis: Sobald Sie eigene Klassen und Testfälle ergänzt haben, ergibt es Sinn
die für Demozwecke enthaltenen Demo-Klassen zu entfernen.

## REST API Verwendung

Dieses Projekt beinhaltet eine einfache REST-API, welche eine Anfrage an das konfigurierte LLM weiterleitet.  
Der Einstiegspunkt der API ist die Klasse **ApiApplication**, der REST-Endpunkt wird in **ApiController** bereitgestellt.

### **Standard-Endpunkt**
```curl
curl --location 'http://localhost:8085/api/request' \
  --header 'Content-Type: application/json' \
  --data '{
    "request": "hello"
  }'
```

### Port-Konfiguration
Die REST-API läuft standardmässig auf Port 8080.
Der Port kann in der Datei `src/main/resources/application.properties` angepasst werden:
```properties
server.port=8085
```

## Enthaltene Libraries (Dependencies)
* Simple Logging Facade (SLF4J) - https://www.slf4j.org/
* LogBack - https://logback.qos.ch/ (Default)

## Enthaltene Test-Libraries (Test Dependencies)
* AssertJ - https://assertj.github.io/doc/
* EqualsVerifier - https://jqno.nl/equalsverifier/
* Console Captor - https://github.com/Hakky54/console-captor
* JUnit 5 - https://junit.org/junit5/

## Integrierte Analysewerkzeuge (Code Qualität)
* Checkstyle - https://checkstyle.sourceforge.io/
* PMD - https://pmd.github.io/
* JaCoCo - https://www.eclemma.org/jacoco/
* Spotbugs - https://spotbugs.github.io/

## Weitere Integrationen (benoetigen ggf. Konfiguration/Account)
* [AsciiDoctor-Plugin](https://asciidoctor.org/) fuer [AsciiDoc](https://asciidoc.org/)
* Deployment in Package Repository (Maven Repo) von http://gitlab.com vorbereitet
* Dockerfile für den Bau eines [Docker-Images](https://www.docker.com/)
* [Fabric/Docker-Plugin](https://dmp.fabric8.io/) fuer Build und Deploy auf [DockerHub](https://hub.docker.com/)
* [GitLab CI/CD](https://docs.gitlab.com/ee/ci/) (.gitlab-ci.yml) inklusive Coverage-Auswertung fuer Java.
* [JIB-Plugin](https://github.com/GoogleContainerTools/jib/tree/master/jib-maven-plugin) (Google) fuer Build und Deploy auf DockerHub (alternative).



## Nützliche Ollama Commands unter macOS:
* _brew install ollama_ (installiert ollama mit Hilfe von brew)
* _brew services start ollama_ (startet den ollama container als service)
* _ollama help_ (zeigt die verfügbaren commands der ollama CLI an)
* _ollama pull mistral:7b_ (startet den Download des LLM Modells mistral)
* _ollama run mistral:7b_ (startet das LLM)
* _ollama ps_ (zeigt alle laufenden LLMs an)
* _ollama stop mistral:7b_ (stoppt das LLM)
* _brew services stop ollama_ (stoppt den Ollama Container)



Feedback und Fehlermeldungen willkommen: alexandra.junghansbaumann@hslu.ch
