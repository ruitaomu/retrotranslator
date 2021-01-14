@ECHO OFF

IF EXIST srcdir	rmdir /S /Q srcdir
IF EXIST destdir rmdir /S /Q destdir
IF EXIST preverified rmdir /S /Q preverified

IF NOT EXIST srcdir mkdir srcdir
IF NOT EXIST destdir mkdir destdir

IF EXIST retrotranslator-runtime13-1.2.9-min.jar del retrotranslator-runtime13-1.2.9-min.jar

IF EXIST scrlist.txt del srclist.txt

dir src\*.java /s /b > srclist.txt
javac -target 1.5 -d srcdir @srclist.txt
java -jar bin\retrotranslator-transformer-1.2.9.jar -target 1.3 -classpath bin\retrotranslator-runtime13-1.2.9.jar;bin\JOSH\classes.zip -srcdir srcdir -destdir destdir
bin\JOSH\preverify.exe -classpath bin\JOSH\classes.zip -d preverified destdir
jar cvf retrotranslator-runtime13-1.2.9-min.jar -C preverified .



IF EXIST testsrcdir	rmdir /S /Q testsrcdir
IF EXIST testdestdir rmdir /S /Q testdestdir
IF EXIST testpreverified rmdir /S /Q testpreverified

IF NOT EXIST testsrcdir mkdir testsrcdir
IF NOT EXIST testdestdir mkdir testdestdir

IF EXIST testsrclist.txt del testsrclist.txt

dir test\*.java /s /b > testsrclist.txt
javac -target 1.5 -d testsrcdir @testsrclist.txt
java -jar bin\retrotranslator-transformer-1.2.9.jar -target 1.3 -classpath bin\JOSH\classes.zip;bin\retrotranslator-runtime13-1.2.9.jar; -srcdir testsrcdir -destdir testdestdir
bin\JOSH\preverify.exe -classpath bin\JOSH\classes.zip -d testpreverified testdestdir
bin\JOSH\cldc_vm.exe -cp retrotranslator-runtime13-1.2.9-min.jar;testpreverified Test
bin\JOSH\cldc_vm.exe -cp retrotranslator-runtime13-1.2.9-min.jar;testpreverified GenericsDemo30