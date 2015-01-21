@echo off


REM …Ë÷√Castor¬∑æ∂
set CASTOR_HOME=E:\DevDocuments\castor

set JAVA=%JAVA_HOME%\bin\java
set JAVAC=%JAVA_HOME%\bin\javac
@echo castor %CASTOR_HOME%
@echo Create the classpath

SetLocal EnableDelayedExpansion 
FOR %%i IN ("%CASTOR_HOME%\*.jar") DO SET CLASSPATH=%%~fi;!CLASSPATH!
:loop

@echo Using classpath: %CLASSPATH%
@echo Generating classes...
@rem Java 2 style collection types

"%JAVA%" -cp ".;%CLASSPATH%;%JDK_BIN%\lib\tools.jar" org.exolab.castor.builder.SourceGeneratorMain -i .\server.xsd -package jef.codegen.schema.model -dest ..\..\java -types j2

@echo.
@pause