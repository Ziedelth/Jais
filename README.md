<img align="right" src="https://dev.ziedelth.fr/images/jais.jpg" height="200" width="200" alt="Jaïs logo">

# Jaïs

Jaïs est un programme permettant d'être notifié de toutes les nouveautés en rapport avec les animes.

_Version de développement, pas encore accès au public_

## Sommaire

1. [Réseaux sociaux](#réseaux-sociaux)
2. [Plateformes d'animes prises en charges](#plateformes-danimes-prises-en-charges)
3. [Mises à jours](#mises-à-jours-ddmmyyyy)
4. [Dépendances](#dépendances)

## Réseaux sociaux

- [x] [Discord](https://discord.com/)
- [x] [Twitter](https://twitter.com/Jaiss_B_)
- [x] [Instagram](https://www.instagram.com/jais_zie/)
- [ ] Reddit

## Plateformes d'animes prises en charges

- [x] [Anime Digital Network](https://animedigitalnetwork.fr/)
- [x] [Crunchyroll](https://www.crunchyroll.com/)
- [x] [MangaScan](https://mangascan.cc/)
- [x] [Wakanim](https://www.wakanim.tv/)

## Mises à jours (dd/mm/yyyy)

- 31/08/2021
    - Optimisation de la détection des épisodes
    - Optimisation des ressources
    - Suppression des méthodes non utilisées
    - Ajout d'Instagram
    - Ajout de la détection de spam
    - Migration vers les nouvelles versions des dépendances
- 18/08/2021
    - Correction de l'enregistrement des saisons dans la base de données
    - Correction de l'affichage du temps de l'épisode pour Discord & Twitter
    - Passage de la version de _**jsoup**_ de **1.14.1** vers **1.14.2**

## Dépendances

Ce projet nécessite **Java 11+**.<br>
Toutes les dépendances sont gérés automatiquement par Maven.

* Apache Commons Logging
    * Version : **1.2**
    * [Site Web](https://commons.apache.org/proper/commons-logging/)
* Gson
    * Version : **2.8.8**
    * [GitHub](https://github.com/google/gson)
* SLF4J Simple Binding
    * Version : **1.7.32**
    * [Site Web](http://www.slf4j.org/)
    * [GitHub](https://github.com/qos-ch/slf4j)
* jsoup Java HTML Parser
    * Version : **1.14.2**
    * [Site Web](https://jsoup.org/)
    * [GitHub](https://github.com/jhy/jsoup/)
* mariadb-java-client
    * Version : **2.7.3**
    * [Site Web](https://mariadb.com/kb/en/about-mariadb-connector-j/)
    * [GitHub](https://github.com/mariadb-corporation/mariadb-connector-j)
* Selenium
    * Version : **3.141.59**
    * [Site Web](https://www.selenium.dev/)
    * [GitHub](https://github.com/SeleniumHQ/selenium)
* Jackson
    * Version : **2.12.5**
    * [GitHub](https://github.com/FasterXML/jackson-dataformat-xml)
* PF4F
    * Version : **3.6.0**
    * [GitHub](https://github.com/pf4j/pf4j)