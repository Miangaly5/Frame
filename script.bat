mkdir "mybin"
set mybin="D:\DOCUMENT_NANCY\S4\Mr Naina\Sprint\fw\mybin"
set ref="D:\DOCUMENT_NANCY\S4\Mr Naina\Sprint\fw\lib\*"

@REM Compilation des fichiers dans le répertoire src et ses sous-répertoires
for /r ".\src" %%f in (*.java) do (
   copy "%%f" "mybin\%%~nf.java"
)
cd mybin
javac -cp %ref% -d "../mybin" *.java
for /r "." %%f in (*.java) do (
   del "%%f" 
)
cd .. 

@REM Définition des chemins
set bin="D:\DOCUMENT_NANCY\S4\Mr Naina\Sprint\fw\bin"
set mylib="D:\DOCUMENT_NANCY\S4\Mr Naina\Sprint\Test\lib"
set jar=sprint0.jar

@REM Création du fichier JAR
jar -cvf %jar% -C %mybin% .

echo D | xcopy /q/y %jar% %mylib%
@echo Done
