# Plugins

Pizza puo' essere esteso tramite plugins.

Ogni plugin e' eseguito in un thread a se: questo significa che se il plugin dovesse interrompersi o generare un errore questo sarebbe interrotto senza
compromettere la funzionalita' degli altri plugin e del bot stesso!

Questa architettura rende Pizza estensibile a piacere, senza troppe preoccupazioni relative a plugin malfunzionanti!


## Creazione

Per creare un uovo plugin e' necessario creare una classe che erediti dalla classe base *com.neroreflex.pizza.Trancio*.
   
A questo punto il plugin sara' funzionante, anche se non sara' in grado di fare nulla!

Ogni plugin ha due modi per essere eseguito: il primo e' a comando, il secondo e' a ciclo continuo.

Ogni plugin puo' presentare entrambi i metodi/componenti di esecuzione.


## Estensione

Facendo l'override di uno o piu' dei metodi descritti di seguito si estendera' il plugin:

    - protected void onInitialize(): metodo chiamato nel main thread, al momento della attivazione del plugin, quindi _*PERICOLOSO*_
	- protected void onShutdown(): metodo chiamato nel main thread, al momento della attivazione del plugin, quindi _*PERICOLOSO*_
	- protected void onCall(Request): metodo chiamato nel onCalls thread, quando l'utente invoca il plugin
	- protected void onPoll(): metodo invocato nel onPolls thread (timer)
	- protected String onHelp(): metodo invocato quando l'utente richiede l'help per lo specifico plugin ed invocato dal thread principale, quindi _*PERICOLOSO*_


Vedere il [Javadoc](javadoc.md) per maggiorni informazioni.


## API

Il plugin dovra' interagire attivamente con gli utenti della chat IRC oltre che a svolgere semplici interrogazioni del tipo:
che canali occupa il bot? A che server e' connesso?

Per fare questo TUTTI i plugin hanno accesso ad una serie di metodi che formano l'interfaccia con la quale i plugin si rapportano al bot.

Tali API possono essere consultate nella documentazione della classe *com.neroreflex.pizza.Trancio* (vedere [Javadoc](javadoc.md) per una lista completa).