# Utilizzo

Prima di poter interagire con il bot e' necessario avviarlo :)

Quando il bot viene avviato si unira' in maniera automatica ad un solo canale:
quello specificato dopo l'host

```sh
cd target
java -jar PizzaBot-1.0-SNAPSHOT.jar "BotName" "server[:port]" "#channel"
```

E' possibile specificare se si vuole un pesante output su console riguardo lo stato di funzionamento
del bot nel seguente modo:

```sh
java -jar PizzaBot-1.0-SNAPSHOT.jar "BotName" "server[:port]" "#channel" "--verbose" "on"
```

E' altrettanto possibile specificare come verificare l'identita' del bot tramite NickSrv:

```sh
java -jar PizzaBot-1.0-SNAPSHOT.jar "BotName" "server[:port]" "#channel" "--verbose" "off" "--identity" "password"
```

## Esempio

Perche' non provare il bot in una stanza sperduta di freenode?

```sh
java -jar PizzaBot-1.0-SNAPSHOT.jar "my_bot" "irc.freenode.net" "#pizzabot" "--verbose" "off"
```

Quando il bot sara' avviato potremmo interagire con esso tramite la chat, ovvero come
faranno *TUTTI* gli altri utenti della chat!
