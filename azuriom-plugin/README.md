# MoodSkins Azuriom Plugin

Ce dossier contient le module site MoodSkins pour Azuriom.

But : afficher les vrais skins Bedrock sur moodcraft.fr.

## Logique

1. Si le pseudo commence par un point, le joueur est considere comme Bedrock.
2. Le point est retire pour obtenir le gamertag Xbox.
3. Le XUID est recupere avec l API Geyser.
4. Le skin Bedrock converti est recupere avec l API Geyser.
5. Le site utilise cette texture pour afficher le bon avatar.

## Endpoints prevus

- GET /moodskins/avatar/{name}
- GET /moodskins/skin/{name}
- GET /moodskins/data/{name}

## Exemple

.Moody6034 devient Moody6034, puis MoodSkins demande son XUID et son skin Bedrock.
