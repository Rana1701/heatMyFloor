# HeatMyFloor - Équipe 43

## Description
Application Java pour la modélisation et la planification de l'installation de planchers chauffants.

## Prérequis
- Java 21

## Installation et exécution

### Exécution du fichier JAR
bash
java -jar equipe43.jar
Le projet a été testé avec Java 21 sous NetBeans

## Utilisation de l'application

- Démarrage
Au lancement de l'application, une pièce rectangulaire par défaut s'affiche.

- Création d'un meuble SANS drain
1. Utiliser le panneau d'édition (situé à gauche de la fenêtre)
2. Définir les dimensions et la position du meuble dans les champs appropriés, si vous entrez des valeurs non valides, vous aurez un message d'erreur.
3. Choisir le type de meuble sans drain (placard ou armoire)
4. Sélectionner l'option "Ajouter"

- Création d'un meuble AVEC drain
1. Utiliser le panneau d'édition (situé à gauche de la fenêtre)
2. Définir les dimensions et la position du meuble dans les champs appropriés, si vous entrez des valeurs non valides, vous aurez un message d'erreur.
3. Choisir le type de meuble avec drain (douche, bain, vanité, toilette)
4. Sélectionner l'option "Ajouter"


- Modification d'un meuble
1. Cliquer sur le meuble dans la vue de la pièce pour le sélectionner, vous devez d'abord sélectionner le meuble que vous souhaitez modifier avant de le faire, sinon, vous aurez un message qui vous indique qu'aucun meuble n'est sélectionné.
2. Dans le panneau d'édition, modifier les propriétés souhaitées :
   - Position (coordonnées x, y) puis cliquer sur l'option 'Déplacer'
   - Dimensions (largeur, longueur) puis cliquer sur l'option 'Ajouter'

- Modification du drain d'un meuble
1. Sélectionner le drain que vous souhaitez modifier.
2. Dans le panneau d'édition, modifier les propriétés souhaitées :
   - Position (coordonnées x, y) puis cliquer sur l'option 'Déplacer'
   - Diamètre, puis cliquer sur l'option 'Modifier'

- Déplacement d'un meuble
1. Sélectionner le meuble dans la vue
2. Modifier les coordonnées de position dans le panneau d'édition puis cliquer sur l'option 'Déplacer'. Si vous déplacez le meuble ailleurs de la pièce, vous aurez un message d'avertissement. Si vous déplacez le meuble à une position ou existe deja un meuble, vous aurez un message d'avertissement.


- Modification des dimensions d'un meuble
1. Sélectionner le meuble dans la vue
2. Modifier la largeur et/ou la longueur dans le panneau d'édition puis cliquer sur l'option 'Modifier' 

- Suppression d'un meuble
1. Sélectionner le meuble dans la vue
2. Cliquer sur le bouton "Supprimer" dans le panneau d'édition
3. Le meuble est retiré de la pièce

- Création d'un élément chauffant
1. Utiliser le panneau d'édition (situé à gauche de la fenêtre)
2. Définir les dimensions et la position d'élément chauffant dans les champs appropriés, si vous entrez des valeurs non valides, vous aurez un message d'erreur. L'élément chauffant est situé au milieu de mur par defaut.
3. Choisir le mur ou vous souhaitez ajouter un élément chauffant (Nord, sud, est, ouest)
4. Sélectionner l'option "Ajouter element chauffant"

- Déplacement d'un élément chauffant
1. Sélectionner l'élément chauffant dans la vue
2. Modifier les coordonnées de position dans le panneau d'édition, ou le mur puis cliquer sur l'option 'Déplacer'.


- Modification des dimensions d'un élément chauffant
1. Sélectionner l'élément chauffant dans la vue
2. Modifier la largeur dans le panneau d'édition puis cliquer sur l'option 'Modifier'

- Zoom
1. Mettre la souris là ou vous voulez faire un zoom.
2. Utilisez la molette de la souris pour zoomer (molette vers le haut) ou dézoomer (molette vers le bas)
3. Le zoom s'effectue autour de la position du curseur
4. Pour revenir au niveau de zoom par défaut (100%), utiliser Menu : `File > Zoom reset`, ou simplement le raccourci `CTRL+NumPad+0` (Reset zoom)

- Visualisation de la pièce
1. La vue de dessus de la pièce s'affiche dans la zone centrale
2. Les meubles apparaissent avec leurs nom et id 
3. Sélectionner le meuble dans la vue pour voir ses dimensions et sa position, un message est également affiché dans la console avec ces informations.


## Fonctionnalités disponibles (Livrable 4)
- ✅ Création de meubles sans drain
- ✅ Création de meubles avec drain
- ✅ Affichage des meubles dans la pièce
- ✅ Sélection de meubles
- ✅ Modification des dimensions des meubles
- ✅ Déplacement des meubles
- ✅ Suppression des meubles
- ✅ Zoomer et dezoomer autours de la souris


## Architecture
L'application respecte le patron MVC (Modèle-Vue-Contrôleur) avec :
- Un contrôleur qui ne retourne pas d'objets complexes du domaine
- Des DTO pour la communication entre la vue et le contrôleur
- Une séparation claire des responsabilités

## Auteurs
Équipe 43
- Abouzeid Hana
- Azemdroub Rana
- Boubechiche Youssouf
- Chadid Douae
- Semani Melissa

## Notes
- Création et redimensionnement de la pièce rectangulaire
- **Avec le panneau d'édition :**
  1. Sélectionner le type de pièce "Rectangulaire"
  2. Entrer les dimensions (longueur et largeur) dans les champs appropriés
  3. Par défaut, les valeurs affichées correspondent aux dimensions de la pièce par défaut
  4. Modifier ces valeurs en les remplaçant par celles souhaitées

- **Avec la souris :**
  - Cliquer sur le coin bas droit de la pièce et le déplacer
  - Les valeurs de longueur et largeur s'inscriront automatiquement dans les champs

- **Nouvelle pièce prédéfinie :**
  - Menu : `File > Nouvelle pièce`
  - Raccourci : `CTRL+N`
  - Dimensions par défaut : Longueur 400, Largeur 300

- Annulation et rétablissement d'opérations (Undo/Redo)
Durant toute l'utilisation de l'application :
- Menu : `Edit > Undo` / `Edit > Redo`
- Raccourcis : `CTRL+Z` (annuler) / `CTRL+Y` (refaire)
- Jusqu'à 9999999 opérations peuvent être annulées/refaites