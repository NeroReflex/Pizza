# Plugins

Pizza puo' essere esteso tramite diversi tipi di plugins.

Ogni plugin e' eseguito in un thread a se: questo significa che se il plugin dovesse interrompersi o generare un errore questo sarebbe interrotto senza
compromettere la funzionalita' degli altri plugin e del bot stesso!

Questa architettura rende Pizza estendibile a piacere, senza troppe preoccupazioni relative a plugin malfunzionanti!


## Creazione

Per creaare un uovo plugin sono necessari due step:

   1. Creare una classe che erediti dalla classe base pizza.Trancio
   2. Registrare il plugin come "di sistema" inserendolo nella funzione loadInternalPlugins in Pizza.java
   
A questo punto il plugin sara' funzionante, anche se non sara' in grado di fare nulla!

Ogni plugin ha due modi per essere eseguito: il primo e' a comando, il secondo e' a ciclo continuo.

Ogni plugin puo' presentare entrambi i metodi/componenti di esecuzione.


## Estensione

Facendo l'override di uno o pie' dei metodi descritti di seguito si estendera' il plugin:

    - protected void onInitialize(): metodo chiamato nel main thread, al momento della attivazione del plugin, quindi *PERICOLOSO*
	- protected void onShutdown(): metodo chiamato nel main thread, al momento della attivazione del plugin, quindi *PERICOLOSO*
	- protected onCall(String user, String channel, Vector<String> args): metodo chiamato nel plugin thread, quando l'utente invoca il plugin
	- protected onPoll(): metodo invocato nel plugin thread continuamente nel minor tempo possibile
	
In onCall(String user, String channel, Vector<String> args) user e' il nick dell'utente che ha chiamato il plugin, channel e' il
canale usato per richiedere l'esecuzione del plugin e args e' l'elenco di argomenti passati al plugin.

## API

Il plugin dovra' interagire attivamente con gli utenti della chat IRC oltre che a svolgere semplici interrogazioni del tipo:
che canali occupa il bot? A che server e' connesso?

Per fare questo TUTTI i plugin hanno accesso ad una serie di metodi che formano l'interfaccia con cui i plugin si rapportano al bot.

Tali API possono essere consultate nella documentazione della classe pizza.Trancio, ma fornisco qui una lista delle piu' comuni
funzioni:

    - String[] getChannels()
	- void sendMessage(Message msg)
	- void joinChannel(String channel, String key)