<a id="readme-top"></a>
<!--
*** Thanks for checking out the Best-README-Template. If you have a suggestion
*** that would make this better, please fork the repo and create a pull request
*** or simply open an issue with the tag "enhancement".
*** Don't forget to give the project a star!
*** Thanks again! Now go create something AMAZING! :D
-->



<!-- PROJECT SHIELDS -->
<!--
*** I'm using markdown "reference style" links for readability.
*** Reference links are enclosed in brackets [ ] instead of parentheses ( ).
*** See the bottom of this document for the declaration of the reference variables
*** for contributors-url, forks-url, etc. This is an optional, concise syntax you may use.
*** https://www.markdownguide.org/basic-syntax/#reference-style-links
-->
[![Build Status](https://img.shields.io/badge/Build-Passing-success?style=for-the-badge&logo=github-actions)](https://github.com/Mzk-Ali/app_ms_simswap/actions)
[![Docker Image](https://img.shields.io/badge/Container-GHCR-blue?style=for-the-badge&logo=docker)](https://github.com/Mzk-Ali/app_ms_simswap/pkgs/container/app_ms_simswap)
[![Java Version](https://img.shields.io/badge/Java-25-vert?style=for-the-badge&logo=openjdk)](https://adoptium.net/)
[![Spring Boot](https://img.shields.io/badge/Spring--Boot-3.5.x-6DB33F?style=for-the-badge&logo=springboot)](https://spring.io/projects/spring-boot)
[![LinkedIn][linkedin-shield]][linkedin-url]



<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="https://github.com/Mzk-Ali/app_ms_simswap">
    <img src="https://github.com/Mzk-Ali/img_git/blob/main/readMe.jpg" alt="Logo" width="400" height="400">
  </a>

  <h3 align="center">Backend FaceSwap MicroService</h3>

  <p align="center">
    Une solution robuste de permutation de visages propulsée par l'IA.
    <br />
    <a href="https://github.com/Mzk-Ali/app_ms_simswap"><strong>Explorer la doc. API</strong></a>
  </p>
</div>



<!-- TABLE OF CONTENTS -->
<details>
  <summary>Table des matières</summary>
  <ol>
    <li>
      <a href="#a-propos-du-projet">À propos du projet</a>
      <ul>
        <li><a href="#build-avec">Build avec</a></li>
      </ul>
    </li>
    <li>
      <a href="#commencons">Commençons</a>
      <ul>
        <li><a href="#pré-requis">Pré-requis</a></li>
        <li><a href="#installation">Installation & Lancement</a></li>
      </ul>
    </li>
    <li><a href="#usage">Utilisation</a></li>
    <li><a href="#roadmap">Roadmap</a></li>
    <li><a href="#license">DevOps</a></li>
    <li><a href="#contact">Contact</a></li>
  </ol>
</details>



<!-- ABOUT THE PROJECT -->
## A propos du project

Ce dépôt contient le **microservice Backend** de l'écosystème **FaceSwap**. Il s'agit du moteur central chargé de traiter les requêtes de transformation d'images via des modèles d'Intelligence Artificielle.

L'architecture repose sur **Spring Boot** pour la logique métier et utilise une approche asynchrone pour garantir la réactivité du système, même lors de traitements lourds.

**Points forts :**
* **Scalabilité :** Déployement facile via Docker.
* **Fiabilité :** Gestion des files d'attente pour ne perdre aucune requête.
* **Observabilité :** Monitoring intégré avec la Stack "Grafana Observability" pour suivre les performances de l'API .

<p align="right">(<a href="#readme-top">back to top</a>)</p>



### Build avec

* [![Spring][Spring]][Spring-url]
* [![Postgre][Postgre]][Postgre-url]
* [![RabbitMq][RabbitMq]][RabbitMq-url]
* [![Prometheus][Prometheus]][Prometheus-url]
* [![Grafana][Grafana]][Grafana-url]
* [![Swagger][Swagger]][Swagger-url]
* [![Postman][Postman]][Postman-url]


<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- GETTING STARTED -->
## Commencons ...

Suivez ces instructions pour configurer le projet dans votre environnement de développement local.

### Pré-requis

* **Docker & Docker Compose avec Docker Desktop sur Windows** (recommandé)
* **Maven** (pour compiler le `.jar` si vous n'utilisez pas Docker)
* **Java 25**

### Installation & Lancement

Il s'agit du lancement de l'application via l'utilisation de Docker 

1. Cloner le projet
   ```sh
   git clone [https://github.com/Mzk-Ali/app_ms_simswap.git](https://github.com/Mzk-Ali/app_ms_simswap.git)
   cd app_ms_simswap
   ```
2. Configuration de l'environnement
   ```sh
   cp .env.example .env
   ```
   Editez le fichier .env pour configurer les accès (DataBase, RabbitMQ, Ports, SMTP)
3. Lancement des outils de monitoring
   ```sh
   cd .\monitoring\
   docker-compose up -d
   cd ..
   ```
4. Lancement de l'application (API)
   ```js
   docker-compose up -d --build
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- USAGE EXAMPLES -->
## Utilisation (OpenAPI)

Le microservice utilise Swagger / OpenAPI pour documenter et tester les points de terminaison.

Une fois l'application lancée, accédez à la documentation interactive ici :
http://localhost:8888/swagger-ui.html

Vous y trouverez les schémas de données et pourrez tester les appels API directement depuis votre navigateur.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- ROADMAP -->
## Roadmap

- [x] Add Changelog
- [x] Add back to top links
- [ ] Add Additional Templates w/ Examples
- [ ] Add "components" document to easily copy & paste sections of the readme
- [ ] Multi-language Support
    - [ ] Chinese
    - [ ] Spanish

See the [open issues](https://github.com/othneildrew/Best-README-Template/issues) for a full list of proposed features (and known issues).

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- DEVOPS -->
## DevOps & Architecture Industrielle

###  Gestion des Artefacts & Images
Plutôt que de compiler le code localement, le projet utilise **GHCR (GitHub Container Registry)** comme registre central :
* **Images Docker Optimisées** : Les images sont construites, taguées et stockées sur le GHCR pour garantir des déploiements reproductibles.
* **Pulling Ready** : Vous pouvez déployer l'application sans même avoir le code source en local via `docker pull ghcr.io/mzk-ali/app_ms_simswap`.

###  CI/CD Workflow
Le workflow GitHub Actions automatise les étapes suivantes à chaque push sur la branche `main` :
1. **Compilation & Tests** : Validation du code avec Maven et Java 25.
2. **Dockerization** : Création de l'image Docker du microservice.
3. **Push GHCR** : Publication automatique de l'image mise à jour sur le registre GitHub.

###  Observabilité & Monitoring
Le projet intègre une stack complète pour surveiller la santé du microservice en temps réel :
* **Prometheus** : Collecte des métriques d'application (CPU, RAM, requêtes API).
* **Grafana** : Visualisation via des dashboards personnalisés pour suivre les performances du moteur de FaceSwap.
* **Loki & Tempo** : Centralisation des logs et tracage des requêtes

<p align="right">(<a href="#readme-top">back to top</a>)</p>


<!-- CONTACT -->
## Contact

Ali MARZAK - [LinkedIn](https://www.linkedin.com/in/ali-marzak-4ab4c4/) - ali.marzak@live.fr

Project Link: [https://github.com/Mzk-Ali/app_ms_simswap](https://github.com/Mzk-Ali/app_ms_simswap)

<p align="right">(<a href="#readme-top">back to top</a>)</p>




<!-- MARKDOWN LINKS & IMAGES -->
<!-- https://www.markdownguide.org/basic-syntax/#reference-style-links -->
[linkedin-shield]: https://img.shields.io/badge/-LinkedIn-black.svg?style=for-the-badge&logo=linkedin&colorB=555
[linkedin-url]: https://www.linkedin.com/in/ali-marzak-4ab4c4/
[product-screenshot]: images/screenshot.png
[Spring]: https://img.shields.io/badge/springBoot-000000?style=for-the-badge&logo=spring&logoColor=6DB33F
[Spring-url]: https://spring.io/
[Postgre]: https://img.shields.io/badge/PostgreSQL-000000?style=for-the-badge&logo=postgresql&logoColor=4169E1
[Postgre-url]: https://www.postgresql.org/
[RabbitMq]: https://img.shields.io/badge/RabbitMq-000000?style=for-the-badge&logo=rabbitmq&logoColor=FF6600
[RabbitMq-url]: https://www.rabbitmq.com/
[Docker]: https://img.shields.io/badge/Docker-000000?style=for-the-badge&logo=docker&logoColor=2496ED
[Docker-url]: https://www.docker.com/
[Prometheus]: https://img.shields.io/badge/Prometheus-000000?style=for-the-badge&logo=prometheus&logoColor=E6522C
[Prometheus-url]: https://prometheus.io/
[Grafana]: https://img.shields.io/badge/Grafana-000000?style=for-the-badge&logo=grafana&logoColor=F46800
[Grafana-url]: https://grafana.com/
[Swagger]: https://img.shields.io/badge/Swagger-000000?style=for-the-badge&logo=swagger&logoColor=85EA2D
[Swagger-url]: https://swagger.io/
[Postman]: https://img.shields.io/badge/Postman-000000?style=for-the-badge&logo=postman&logoColor=FF6C37
[Postman-url]: https://www.postman.com/
