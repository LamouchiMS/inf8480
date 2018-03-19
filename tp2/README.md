# Supposition

Serveur calcule une operation a la fois

## Config generale

Repertoire des noms => IP fixe
Load balancer => IP fixe
Serveurs => m et p aleatoires
Load balancer => Mode securise ou pas

## Creation serveur

## Serveur

Repertoire des noms existe?
Non:
    Creer le repertoire (avec IP fixe)
Notifier le repertoire que le serveur existe

## Repertoire

Enregistrer le serveur dans un fichier texte (pour construire la liste des IPs)

## Creation Load balancer

### Repariteur

Repertoire des noms existe?
Non:
    Creer le repertoire (avec IP fixe)
S'authentifier (avec son IP et password)
Recuperer la liste des serveurs

## Creation du client

Load balancer existe?
Non:
    Creer le load balancer (avec IP fixe)

## Exemple execution

Creer client
Client envoi chemin fichier au repariteur
Si repartiteur est authentifie
    BOUCLE
        Calculer le taux de refus/acceptation du n'ieme serveur
        Determiner si le n'ieme serveur est disponible
        Oui:
            Decouper "q" operations pour qu'il les calcule
            Envoyer au seveur pour qu'il calcule
    => Arret quand il reste plus rien du fichier
    Somme des resultats
    Renvoi au client
