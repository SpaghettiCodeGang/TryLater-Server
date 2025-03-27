Testzeile
# TryLater-Serveranwendung
Die TryLater-Serveranwendung verwaltet die Nutzerdaten und Empfehlungen der TryLater-App.

---

## 🔗 Project-Links
- **[Project-Wiki](https://github.com/SpaghettiCodeGang/TryLater-Server/wiki)**
- **[Project-Board](https://github.com/orgs/SpaghettiCodeGang/projects/1)**

---

## 🧰 Voraussetzungen
- **JDK 21 (Java Development Kit)**
- Maven wird über den mitgelieferten Maven Wrapper (`./mvnw`) verwendet – keine separate Installation nötig.
- Eine zusätzliche Datenbank ist nicht erforderlich – es wird die integrierte **H2-Datenbank** verwendet.

---

## ⚙️ Setup
- Projekt clonen
- main() ausführen

---

## 🛠️ Entwicklung
- Den aktuellen `main`-Branch pullen
- Einen neuen `feature`-Branch aus `main` erstellen
- Implementieren, testen, committen
- Regelmäßig pushen
- ✅ Pull Request stellen, wenn fertig
- 🧃 Spaß haben (stay hydrated 😉)

---

## 🛢️ Datenbank (lokal)
- http://localhost:8080/h2-console
> 💡 **Hinweis:** Die H2-Datenbank ist im Entwicklungsmode aktiv und speichert die Daten lokal als Datei.

---

## 🌐 API-Anbindung

Die Client-Anwendung kommuniziert über `/api/...` mit dem Backend.
Eine vollständige Übersicht aller verfügbaren Routen, Parameter und Datenformate befindet sich im [TryLater-Server Wiki](https://github.com/SpaghettiCodeGang/TryLater-Server/wiki).
