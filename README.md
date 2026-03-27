# MAS Négociation Syndicat & Direction
Système Multi-Agents argumentatif modélisant la négociation entre un **Syndicat**
et une **Direction d'entreprise**, dans différents contextes de dialogue social.

Construit via Java avec **JADE** (Java Agent DEvelopment Framework) pour la gestion des agents
et des échanges de messages FIPA-ACL, et **rAIson** pour la génération et l'évaluation des arguments depuis les bases 
de connaissances (KB) privées de chaque agent.

## Principe
Deux agents aux rationalités différentes négocient sur plusieurs dimensions simultanées :
- **L'Agent Direction** défend des objectifs économiques (rentabilité, délais, compétitivité, ...) à partir d'une base 
de connaissances privée (projections financières, données concurrentielles, ...).
- **L'Agent Syndicat** défend les intérêts des travailleurs (protection de l'emploi, conditions de reconversion, 
garanties sociales, ...) à partir d'une base de connaissances privée (profils des employés, statistiques sectorielles, 
précédents, ...).

Les deux agents ne partagent aucune base de faits commune. Le résultat de la négociation n'est pas prédéterminé mais 
émerge des arguments échangés et des concessions consenties.


## Architecture
```
NegociationSocialeMultiAgent/
├── pom.xml
├── README.md
│
└── src/main/app/
    ├── Main.java                         Instancie le conteneur JADE et lance les deux agents
    │
    ├── model/                            Structures de données immuables
    │   ├── Dimension.java                Enumération des 6 dimensions négociables
    │   ├── Offer.java                    Offre (avec Builder Pattern)
    │   ├── Argument.java                 Argument (affirmation + preuve + justification + force)
    │   └── KnowledgeBase.java            Interface commune aux deux KB
    │
    ├── knowledge/                        Bases de connaissances privées
    │   ├── DirectionKB.java              Données financières, ROI, ...
    │   └── SyndicatKB.java               Profils travailleurs, stats sectorielles, ...
    │
    ├── agents/                           Cycle de vie JADE
    │   ├── DirectionAgent.java           Initiateur, interroge le DF avant de lancer la négociation
    │   └── SyndicatAgent.java            Répondeur, s'enregistre dans le DF au démarrage
    │
    └── behaviours/                       Logique de négociation
        ├── NegotiationBehaviour.java     CyclicBehaviour, protocole multi-tours FIPA-ACL
        └── ConcessionStrategy.java       Calcul des contre-offres
```

## JADE & Modélisation

Le projet est structuré de manière modulaire afin de séparer l'infrastructure de communication (JADE), 
la logique décisionnelle, et le modèle de données.

L'architecture Java s'articule autour de 4 packages principaux :

### 1. Modèle de Données : `app.model` 
Définit les structures d'échange entre les agents.
- **Les 6 dimensions négociables :** Postes supprimés, Plan de requalification, Compensation financière, 
Rythme de déploiement, Priorité recrutement, et Comité de suivi.
- **Immuabilité & Sérialisation :** Les classes `Offer` et `Argument` sont conçues de manière immuable *(en suivant le 
Design Pattern Builder)*. Cela garantit qu'aucune donnée ne peut être altérée par effet de bord lors de son transit 
sur le réseau JADE. Aussi, ces deux classes implémentent l'interface Serializable, condition obligatoire pour que JADE 
puisse les convertir en flux d'octets lors de l'appel à *setContentObject()* et les reconstruire à l'identique chez 
l'agent destinataire via *getContentObject()*.

### 2. Bases de Connaissances : `app.knowledge`
Encapsule les données privées, les contraintes et les limites de chaque camp. Les agents ne partagent pas ces 
informations.
- `DirectionKB` : Base ses limites sur des projections financières (délai de rentabilité) et la capacité budgétaire
pour maximiser le ROI.
- `SyndicatKB` : Base ses exigences sur les profils des travailleurs impactés et les droits légaux
pour protéger les emplois.

Chaque KB possède une méthode de bridage définissant la zone d'un possible accord qui empêche mathématiquement l'agent 
de proposer une offre dépassant ses lignes rouges absolues.

### 3. Agents JADE : `app.agents`
 Gère le cycle de vie des agents sur la plateforme multi-agents. L'architecture n'utilise pas d'identifiants codés en 
 dur. L'agent Syndicat (`SyndicatAgent`) s'enregistre en tant que service dans le **Directory Facilitator (DF)** de JADE. 
 L'agent Direction (`DirectionAgent`, initiateur de la négociation) interroge alors ce DF pour trouver le syndicat sur le réseau avant 
 de lancer le protocole.

### 4. Moteur de Négociation : `app.behaviours`
 Implémente la logique de négociation multi-tours entre les deux agents.
-  `NegotiationBehaviour` : Basé sur un `CyclicBehaviour`, il reçoit les messages, met à jour l'état de la négociation, 
et gère le choix des speech acts (PROPOSE, ARGUE, ATTACK, ACCEPT, ...).
- **`ConcessionStrategy` :** Stratégie de calcul des contre-offres. Actuellement basée sur un rapprochement 
mathématique, elle sera amenée à moduler les concessions en fonction de la force des arguments adverses.

## Prérequis & Lancement
**JDK 11+** & **Maven 3.6+** (dépendance JADE déclarée dans `pom.xml`)

Le lancement de la simulation se fait via la commande
```bash
mvn clean compile exec:java -Dexec.mainClass="app.Main"
```

## Auteur
Aidoudi Aaron

Projet du cours **Agents Intelligents**, encadré par Pavlos Moraitis - Master 1 Intelligence Artificielle Distribuée 
à l'Université Paris Cité

Année Universitaire 2025-2026
