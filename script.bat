set "file=FrontController" 
set "package=classes"
javac -d . java/*.java
md jar
jar -cvf jar\%file%.jar %package%\*.class 

pause