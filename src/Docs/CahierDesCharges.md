#Cahier des Charges — Projet 1 : Gestion Concurrente d’un Aéroport
##1. Introduction

Ce projet consiste à simuler la gestion d’un aéroport en mettant l’accent sur les problèmes de concurrence, les ressources partagées et les mécanismes de synchronisation en Java.
L’objectif est de comparer trois mécanismes de contrôle de concurrence :

-Sémaphores

-Moniteurs Java (synchronized, wait, notify)

-ReentrantLock + Condition

La simulation doit intégrer une interface graphique commune et trois implémentations distinctes du noyau logique.

##2. Objectifs du Projet
###Objectifs pédagogiques

-Manipuler des ressources limitées de manière sûre et concurrente.

-Comprendre les différences entre les mécanismes de synchronisation Java.

-Appliquer des solutions robustes pour éviter :

	les interblocages (deadlocks),

	les famines (starvation),

	les conditions de course.

###Objectifs fonctionnels

-Simuler un aéroport avec :

	un nombre limité de pistes d’atterrissage/décollage,

	un nombre limité de portes d’embarquement.

-Gérer plusieurs catégories d’avions :

	avions en arrivée,

	avions en stationnement,

	avions en départ.

-Afficher en temps réel :

	l’état des pistes et des portes,

	les files d’attente,

	les logs des événements.

## 3. Fonctionnalités attendues
###3.1 Ressources partagées
Ressource     |	Description
------------------------------------
Pistes         |	utilisées pour l’atterrissage/décollage. Nombre configurable (ex. 2).
Portes         |
d’embarquement |	utilisées pour stationnement/débarquement/embarquement. Nombre 
               |    configurable (ex. 4).

-Contraintes :

Une piste/porte = un seul avion à la fois.

Les arrivées sont prioritaires sur les départs.

Les avions attendent si aucune ressource n’est disponible.

###3.2 Gestion des avions (threads)

Chaque avion suit un cycle :

1.Avion en arrivée

Attend une piste libre.

Atterrit → rejoint une porte (si disponible) → sinon entre en file d’attente.

2.Avion en stationnement

Occupe une porte durant un temps simulé.

Se prépare pour le départ.

3.Avion en départ

Attend une piste libre.

Décolle puis se termine.

###3.3 Mécanismes de synchronisation à implémenter

Deux versions minimum (trois si temps disponible) :

-SemaphoreVersion/

Sémaphores binaires et comptables.

-MonitorVersion/

synchronized, wait/notify/notifyAll.

-ReentrantLockVersion/

ReentrantLock, Condition, priorité manuelle.

Chaque version doit avoir la même interface pour être interchangeable avec la GUI.

##4. Interface Graphique (GUI)
###4.1 Affichage

La fenêtre doit montrer :

✔️ État des pistes (libre / occupée)

✔️ État des portes (libre / occupée)

✔️ Files d’attente :

avions en arrivée,

avions en départ.

✔️ Journal des événements (logs)

ex. “Avion A12 → Atterrissage accepté sur piste 1”

###4.2 Contrôles utilisateur

Choix du mécanisme de synchronisation utilisé.

Ajouter un avion (arrivée ou départ).

Paramètres ajustables :

nombre de pistes,

nombre de portes,

vitesse de simulation.

##5. Architecture du projet (GitHub)

```
/AirportManagement/
│
├── SemaphoreVersion/
│   └── src/...
│
├── MonitorVersion/
│   └── src/...
│
├── ReentrantLockVersion/
│   └── src/...
│
├── GUI/
│   └── src/
│
├── Docs/
│   ├── RapportComparatif.pdf
│   └── CahierDesCharges.md
│
└── README.md

```

## 6. Contraintes techniques

Langage : Java 17+

Interface graphique : Swing 

###Concurrence :

éviter deadlocks et starvation,

garantir la priorité aux avions en arrivée,

garantir la cohérence des ressources.

##7. Critères d’évaluation
###7.1 Fonctionnalités

Simulation correcte et cohérente.

Gestion des ressources sans erreurs de synchronisation.

###7.2 Concurrence

Implémentation propre, sans blocage.

Respect des priorités.

###7.3 Interface graphique

Lisible, interactive, réactive.

###7.4 Rapport comparatif

Analyse des différences entre :

Sémaphore,

Moniteur,

ReentrantLock.

###Comparaison sur :

facilité d’implémentation,

performance,

clarté,

risques de blocage.

## 8. Livrables

Deux versions minimales basées sur deux techniques de synchronisation.

Une GUI commune.

README.md (installation, lancement, description).

Docs/

CahierDesCharges.md

RapportComparatif.pdf