# Compilazione

La compilazione di Pizza richiede 3 semplicissime fasi descritte di seguito.

Paradossalmente la fase piu' complicata e' la preparazione dell'ambiente di lavoro -.-"


## Preparazione

L'installazione del compilatore e runtime java e' necessaria per lavorare con il progetto.

La versione utilizzata al momento della creazione del progetto e': [jdk 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Il processo di compilazione e' automatizzato da [maven](https://maven.apache.org/):
e' altamente consigliato aggiungere la directory bin alla variabile d'ambiente PATH,
in caso contrario dovrete ogni volta far riferimento al percorso completo.

Per scaricare il progetto e' necessario installare [git](https://git-scm.com/) o scaricare il progetto come zip da GitHub!


## Ottenere il codice sorgente

L'utilizzo di git e' altamente consigliato per scaricare il codice sorgente:

```sh
git clone https://github.com/NeroReflex/Pizza.git
cd Pizza
```

Ora e' possibile passare alla compilazione vera e propria!


## Compilazione

Il processo di compilazione e' descritto nel file bom.xml, destinato al build system maven,
e va azionato con il comando:

```sh
mvn compile && mvn package
```

Una corretta compilazione produrra' una directory "target" contenente un file chiamato "PizzaBot-1.0-SNAPSHOT.jar".