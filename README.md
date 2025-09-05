# EvoPlan: Healthcare Event Management Backend (Hexatech) 📋

![Java](https://img.shields.io/badge/Java-17+-red)
![Maven](https://img.shields.io/badge/Maven-3.x-blue)
![License](https://img.shields.io/badge/License-MIT-blue)
![Esprit](https://img.shields.io/badge/Esprit%20School-Web%20Technologies%202A-orange)

A native Java-based backend for the EvoPlan healthcare event management platform, developed at Esprit School of Engineering.

---

## Overview 🌟

EvoPlan (Hexatech) is the native Java-based backend for the EvoPlan project, created as part of the **Web Technologies 2A** course at **[Esprit School of Engineering](https://esprit.tn/)**. This platform streamlines the management of healthcare events, such as workshops, conferences, and training sessions, by providing a robust server-side API for web or mobile frontends. Built with **Java SE**, **Maven**, and **MySQL**, it ensures scalability and reliability for healthcare event management.

---

## Description 📝

EvoPlan (Hexatech) powers the EvoPlan healthcare event management platform by offering a secure backend for organizing and tracking events. It integrates with frontends like EvoPlanWeb to deliver a comprehensive solution for healthcare professionals and organizers.

- **Objective**: Simplify healthcare event planning and enhance collaboration through a Java-based API.
- **Problem Solved**: Addresses inefficiencies in managing healthcare events with a scalable backend.
- **Main Features**:
  - Manage healthcare events (e.g., workshops, conferences) via RESTful API endpoints.
  - Assign tasks and resources for event planning.
  - Track event progress and feedback.
  - Secure data storage with MySQL.

---

## Table of Contents 📑

- [Overview](#overview)
- [Description](#description)
- [Tech Stack](#tech-stack)
- [Installation](#installation)
- [Usage](#usage)
- [Contributions](#contributions)
- [Acknowledgements](#acknowledgements)
- [License](#license)

---

## Tech Stack 🛠️

- **Backend**: Java SE 17
- **Database**: MySQL
- **Build Tool**: Maven
- **Other Tools**: REST API, Postman (for testing), Git

---

## Installation ⚙️

Follow these steps to set up EvoPlan (Hexatech) locally:

1. **Clone the repository**:
   ```bash
   git clone https://github.com/MedAlizr/Hexatech.git
   cd Hexatech
   ```

2. **Install Java and Maven**:
   - Ensure **Java JDK 17** or later is installed:
     ```bash
     java -version
     ```
   - Install **Maven**:
     ```bash
     sudo apt update
     sudo apt install maven
     ```
     (For Windows, download Maven from [apache.org](https://maven.apache.org/download.cgi) and configure it.)

3. **Configure the database**:
   - Create a MySQL database (e.g., `evoplan_db`).
   - Import the SQL schema from `src/main/resources/schema.sql` (if available).
   - Update database credentials in the configuration file (e.g., `src/main/resources/config.properties`):
     ```properties
     db.url=jdbc:mysql://localhost:3306/evoplan_db
     db.username=your_username
     db.password=your_password
     ```

4. **Build the project**:
   ```bash
   mvn clean install
   ```

---

## Usage 🚀

To run and use EvoPlan :

1. **Start the application**:
   - Run the main class using Maven (replace `com.example.Main` with the actual main class):
     ```bash
     mvn exec:java -Dexec.mainClass="com.example.Main"
     ```
   - Alternatively, build a JAR and run it:
     ```bash
     mvn package
     java -jar target/hexatech-1.0-SNAPSHOT.jar
     ```
   - The backend runs on `http://localhost:8080` by default (adjust port if different).

2. **Access the API**:
   - Test API endpoints (e.g., `/api/events`) using **Postman** or **curl**.
   - Example request:
     ```bash
     curl http://localhost:8080/api/events
     ```
   - Check `src/main/resources/api-docs/` (if available) for API documentation.

3. **Integrate with frontend**:
   - Connect to the EvoPlanWeb frontend or a custom client to manage healthcare events via the API.

---

## Contributions 🤝

We welcome contributions to EvoPlan ! Thank you to all who have helped improve this project.

### Contributors
- [Mohamed Ali Zarai](https://github.com/MedAlizr) - Responsible for implementing user functionality and core app integration.
- [Mehdi Ayachi](https://github.com/mehdi5255) - Responsible for the Event and Event Planning module and its core features.
- [Mustapha Jerbi](https://github.com/Mustapha-who) - Creator of the Workshop module and features.
- [Selim Ishak](https://github.com/selimisaac) - Creator of the Feedback and Claim Management module.
- [Ghalia El Ouaer](https://github.com/ghaliaelouaer24) - Responsible for the Resources module.
- [Mohamed Amine Mezlini](https://github.com/aminemezlini321) - Responsible for the Partnerships and Contracts module.

### How to Contribute?

1. **Fork the project**:
   - Go to the [Hexatech repository](https://github.com/MedAlizr/Hexatech) and click **Fork**.

2. **Clone your fork**:
   ```bash
   git clone https://github.com/your-username/Hexatech.git
   cd Hexatech
   ```

3. **Create a new branch**:
   ```bash
   git checkout -b feature/your-feature-name
   ```

4. **Make changes and commit**:
   ```bash
   git add .
   git commit -m "Add your feature or fix"
   git push origin feature/your-feature-name
   ```

5. **Submit a pull request**:
   - Create a pull request to the `main` branch of the original repository.

---

## Acknowledgements 🙏

This project was developed as part of the **Web Technologies 2A** course at **Esprit School of Engineering**. We thank our instructors and peers for their guidance and support in building thisApplication.

---

## License 📜

This project is licensed under the **MIT License**. For more details, see the [LICENSE](LICENSE) file.
