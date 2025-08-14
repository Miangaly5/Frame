@REM Définition des chemins existants
@REM Les fichiers compilés
set bin-dir="D:\Documents\S4\Mr Naina\Sprint\Sprint_Nouveau\fw\bin"
@REM Librairies nécessaires
set lib-dir="D:\Documents\S4\Mr Naina\Sprint\Sprint_Nouveau\fw\lib\*"
@REM Codes source du framework
set src-dir="D:\Documents\S4\Mr Naina\Sprint\Sprint_Nouveau\fw\src"
@REM Librairies de l'utilisateur
set target-lib-dir="D:\Documents\S4\Mr Naina\Sprint\Sprint_Nouveau\Test\lib"

@REM Création d'un dossier temporaire pour stocker les fichiers .java
mkdir temp
set temp="D:\Documents\S4\Mr Naina\Sprint\Sprint_Nouveau\fw\temp"

@REM Copie des fichiers .java dans un même répertoire
for /r %src-dir% %%f in (*.java) do (
   copy "%%f" %temp%
)

@REM Compilation des fichiers .java et les stockés dans le dossier classes
javac -parameters -cp %lib-dir% -d %bin-dir% %temp%\*.java

@REM Création du fichier JAR
set jar=sprint0.jar
jar -cvf %jar% -C %bin-dir% .

@REM Copie des librairies nécessaire pour le côté utilisateur
echo D | xcopy /q/y %lib-dir% %target-lib-dir%
echo D | xcopy /q/y %jar% %target-lib-dir%

@REM Suppression du dossier temporaire
rmdir /q/s "temp"

@REM Fin du script
@echo Done
