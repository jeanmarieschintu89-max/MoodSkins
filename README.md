# MoodSkins

Plugin Paper MoodCraft pour synchroniser les vrais skins Bedrock en jeu.

## Fonctionnement

- detecte les joueurs Bedrock avec le prefixe `.`
- retire le prefixe pour obtenir le gamertag Xbox
- recupere le XUID via l API Geyser
- recupere la texture Bedrock via l API Geyser
- applique le skin via une commande SkinsRestorer configurable

## Commandes

- `/moodskins reload`
- `/moodskins debug <joueur>`
- `/moodskins apply <joueur>`

## Build

Le repo contient un `pom.xml` a la racine pour etre construit par le workflow global MoodCraftBridge.

Le JAR final attendu est :

```text
MoodSkins.jar
```
