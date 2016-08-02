# Compilazione

La compilazione di Pizza richiede 3 semplicissime fasi descritte di seguito.

Paradossalmente la fase più complicata è la preparazione dell'ambiente di lavoro!


## Preparazione

L'installazione del compilatore e runtime java è necessaria per lavorare con il progetto!

La versione utilizzata al momento della creazione del progetto è: [jdk 1.8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

Il processo di compilazione è automatizzato da [ant](http://ant.apache.org/bindownload.cgi): è necessario aggiungere la directory bin alla variabile d'ambiente PATH!

Per scaricare il progetto è necessario installare [git](https://git-scm.com/) con il plugin [git-lfs](https://git-lfs.github.com/)!


## Ottenere il codice sorgente

L'utilizzo di git è altamente consigliato per scaricare il codice sorgente:

```sh
git clone https://github.com/NeroReflex/Pizza.git
cd Pizza
```

Ora è possibile passare alla compilazione vera e propria!


## Compilazione

Il proecsso di compilazione è descritto nel file nbbuild.xml:

```sh
ant -f nbbuild.xml -Dnb.internal.action.name=build jar
```

Una corretta compilazione produrrà una directory "dist" contenente un file chiamato "Pizza.jar" e una directory "lib".