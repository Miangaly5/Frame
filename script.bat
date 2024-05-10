@REM lib avy ao amn working dir 
set lib-dir="E:/DOCUMENT_NANCY/S4/Mr Naina/new_project/Test/lib"

@REM fichier de configuration avy ao amn working dir
set web-xml="E:/DOCUMENT_NANCY/S4/Mr Naina/new_project/Test/conf"

@REM nom du projet
set target-name=my_frame

@REM webapps chemin farany hi-deployena ny projet
set target-dir="C:\Program Files\Apache Software Foundation\Tomcat 10.1\webapps"

rmdir /q/s "temp"
mkdir "temp/WEB-INF/classes"
mkdir "temp/WEB-INF/lib"

@REM copie du fichier de configuration et du fichier jar dans temp
echo D | xcopy /q/s/y %web-xml% "temp/WEB-INF"
echo D | xcopy /q/s/y %lib-dir% "temp/WEB-INF/lib"

@REM creation du fichier war
jar -cvf %target-name%.war -C temp/ . 

xcopy /q/y %target-name%.war %target-dir%

del %target-name%.war
rmdir /q/s "temp"

echo Done