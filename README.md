# MAS Négociation Syndicat & Direction
Système Multi-Agents modélisant la négociation d'un plan social entre un **Syndicat** et une **Direction** d'entreprise.

Construit en **Java** avec **JADE** (Java Agent DEvelopment Framework) pour la gestion des agents et
des échanges de messages FIPA-ACL, et **rAIson** pour le raisonnement à partir des
bases de connaissances privées de chaque agent.

## Principe
Deux agents aux rationalités opposées négocient sur six dimensions simultanées jusqu'à trouver
un accord ou déclarer une impasse après 7 tours maximum.

- **L'Agent Direction** défend des objectifs économiques (rentabilité, maîtrise des coûts, délais
  de déploiement) à partir d'une base de connaissances privée définissant ses limites acceptables.
- **L'Agent Syndicat** défend les intérêts des travailleurs (protection de l'emploi, conditions de
  reconversion, garanties sociales) à partir de sa propre base de connaissances privée.

Les deux agents ne partagent aucune base de faits commune. Le résultat de la négociation n'est pas
prédéterminé : il émerge des concessions consenties et des décisions retournées par rAIson.


## Architecture
```
src/main/java/app/
│
├── Main.java                 Instancie le conteneur JADE et lance les deux agents
│
├── model/                    Structures de données immuables
│ ├── Dimension.java          Énumération des 6 dimensions négociables
│ ├── Offer.java              Offre multi-dimensionnelle (Builder Pattern + Serializable)
│ └── KnowledgeBase.java      Interface commune aux deux bases de connaissances
│
├── knowledge/                Bases de connaissances privées
│ ├── DirectionKB.java        Limites et position initiale de la Direction
│ └── SyndicatKB.java         Limites et position initiale du Syndicat
│
├── agents/                   Cycle de vie JADE
│ ├── DirectionAgent.java     Initiateur : interroge le DF puis lance la négociation
│ └── SyndicatAgent.java      Répondeur : s'enregistre dans le DF au démarrage
│
├── behaviours/                 Logique de négociation
│ ├── NegotiationBehaviour.java CyclicBehaviour gérant le protocole multi-tours FIPA-ACL
│ └── ConcessionStrategy.java   Calcul des contre-offres (concession uniforme ou ciblée)
│
└── raison/                   Intégration de l'API rAIson
├── ElementBuilder.java       Traduit l'état d'une offre en éléments rAIson actifs
├── RaisonClient.java         Client HTTP (HttpURLConnection) vers l'API
├── RaisonElement.java        Élément soumis à l'API (identifié par son ID)
├── RaisonOption.java         Option de décision évaluée par rAIson (id + label)
├── RaisonRequest.java        Body du POST (elements + options)
└── RaisonResult.java         Réponse de l'API (option + isSolution + explanation)
```

## Modélisation

### Dimensions négociées
| Dimension              | Type                  | Favorable Syndicat | Favorable Direction |
|------------------------|-----------------------|--------------------|---------------------|
| Postes supprimés       | int                   | Minimiser          | Maximiser           |
| Durée requalification  | int (mois)            | Maximiser          | Minimiser           |
| Compensation           | int (mois de salaire) | Maximiser          | Minimiser           |
| Rythme de déploiement  | int (mois)            | Maximiser          | Minimiser           |
| Priorité recrutement   | boolean               | Vrai               | Concédable          |
| Comité de suivi        | boolean               | Vrai               | Faux                |

### Modèle de données : *app.model*
***Offer*** est immuable par construction (Builder Pattern + champs finals). Elle implémente
*Serializable*, condition obligatoire pour transiter dans les **ACLMessage** de JADE via
*setContentObject()* ou *getContentObject()*.

Chaque ***KnowledgeBase*** expose une méthode *brider()* qui plafonne mathématiquement toute offre
calculée aux limites de l'agent, garantissant qu'aucune concession ne peut franchir ses lignes rouges.

***Dimension*** est une énumération des six axes de négociation, utilisée par ***ConcessionStrategy***
pour identifier la dimension ciblée lors d'une concession orientée.

### Découverte des agents : *app.agents*
Aucun identifiant n'est codé en dur. Le ***SyndicatAgent*** s'enregistre comme service dans le
**Directory Facilitator (DF)** de JADE au démarrage. Le ***DirectionAgent*** interroge ce DF via un
*TickerBehaviour* toutes les 500 ms jusqu'à trouver le Syndicat, puis lance la négociation.

### Protocole de négociation : *app.behaviours*
***NegotiationBehaviour*** est un ***CyclicBehaviour*** commun aux deux agents. À chaque tour :
1. L'offre reçue est évaluée par *KnowledgeBase.estAcceptable()*. Un accord est immédiat si satisfaite *(ACCEPT_PROPOSAL)*.
2. Si le nombre de tours maximum est atteint alors c'est impasse *(FAILURE)*.
3. Sinon, la décision est déléguée à rAIson (si plusieurs dimensions sont en échec, pour identifier la concession la 
plus pertinente parmi plusieurs conflits.) ou traitée directement en Java (si une seule dimension est en échec, pour 
éviter un appel API inutile). ***ConcessionStrategy*** calcule alors la contre-offre *(PROPOSE)* : uniforme à 30% 
(via *conceder*) ou ciblée à 40% sur la dimension identifiée par rAIson et 10% sur les autres (via *concederSur*).

### Intégration rAIson : *app.raison*
rAIson est un moteur d'argumentation externe consulté pour identifier la concession la plus pertinente lorsque plusieurs 
dimensions sont simultanément en conflit. Chaque agent dispose de son propre projet rAIson (scénarios et options 
configurés indépendamment sur la plateforme).

***ElementBuilder*** traduit l'état de l'offre en liste d'éléments actifs (dimensions satisfaites ou non par rapport aux 
seuils de la KB) soumis à l'API.
Le reste du package assure la sérialisation et désérialisation JSON via Gson : ***RaisonRequest*** structure le body du 
POST avec les éléments actifs et la liste des options du projet, ***RaisonResult*** mappe la réponse de l'API, et
***RaisonOption*** expose le champ *label* rempli par Gson à la désérialisation, qui contient le nom
de la décision retournée (ex. *counter_propose_compensation*).
Notons aussi que ***RaisonClient*** utilise *HttpURLConnection*. En cas d'erreur réseau ou de réponse vide, il 
retourne *reject_offer* comme fallback.

## Prérequis & Lancement
**JDK 17+** et **Maven 3.6+**

```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="app.Main"
```

## Auteur
Aidoudi Aaron

Projet du cours **Agents Intelligents**, encadré par Pavlos Moraitis - Master 1 Intelligence Artificielle Distribuée 
à l'Université Paris Cité

Année Universitaire 2025-2026
