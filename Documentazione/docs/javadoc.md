# Javadoc

Il progetto e' configurato per la generazione della documentazione partendo dal codice sorgente.

Maven per svolgere il compito si affida a javadoc:

```sh
mvn javadoc:javadoc
```

Tuttavia ad ogni push nel branch master la documentazione javadoc viene aggiornata __AUTOMATICAMENTE__ da
wercker.

Potete visualizzare il risultato dell'analisi automatica del sorgente da parte di javadoc online a [questo indirizzo](javadoc/apidocs/).


## Ulteriori opzioni

Per chi ha specifiche necessita' riguardo alla generazione della documentazione il plugin maven utilizzato e'
il seguente: [javadoc for mvn](https://maven.apache.org/plugins/maven-javadoc-plugin/usage.html).