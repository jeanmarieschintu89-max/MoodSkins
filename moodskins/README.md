# MoodSkins

Plugin Azuriom pour afficher les vrais skins Bedrock sur moodcraft.fr.

## Installation

1. Copier le dossier `moodskins` dans le dossier `plugins` de ton installation Azuriom.
2. Dans l admin Azuriom, va dans Extensions / Plugins.
3. Active MoodSkins.
4. Vide le cache Azuriom si necessaire.

## URLs de test

- `/moodskins/data/.Moody6034`
- `/moodskins/avatar/.Moody6034`
- `/moodskins/skin/.Moody6034`
- `/moodskins/head/.Moody6034`

## Utilisation dans un theme

Remplacer l avatar par :

```html
<img src="/moodskins/avatar/{{ $user->name }}" alt="{{ $user->name }}">
```

## Fonctionnement

- `.Moody6034` est reconnu comme Bedrock.
- Le point est retire pour obtenir `Moody6034`.
- MoodSkins appelle l API Geyser pour recuperer le XUID.
- MoodSkins appelle l API Geyser pour recuperer le skin Bedrock.
- Le site redirige vers la texture Minecraft.
