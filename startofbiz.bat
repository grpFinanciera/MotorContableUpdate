@echo off
rem #####################################################################
rem Licensed to the Apache Software Foundation (ASF) under one
rem or more contributor license agreements. See the NOTICE file
rem distributed with this work for additional information
rem regarding copyright ownership. The ASF licenses this file
rem to you under the Apache License, Version 2.0 (the
rem "License"); you may not use this file except in compliance
rem with the License. You may obtain a copy of the License at
rem
rem http://www.apache.org/licenses/LICENSE-2.0
rem
rem Unless required by applicable law or agreed to in writing,
rem software distributed under the License is distributed on an
rem "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
rem KIND, either express or implied. See the License for the
rem specific language governing permissions and limitations
rem under the License.
rem #####################################################################

rem ### Configuración del archivo de log
set OFBIZ_LOG=runtime\logs\console.log

rem ### Eliminar el último log si existe
if exist %OFBIZ_LOG% del %OFBIZ_LOG%

rem ### Parámetros de la máquina virtual Java (JVM)
rem Configuración de memoria para la JVM (Java 8 y superior)
set MEMIF=-Xms512M -Xmx1536M
rem Configuración de idioma
set MISC=-Duser.language=en -noverify
rem Combina las configuraciones de JVM
set VMARGS=%MEMIF% %MISC%

rem ### Configuración de Worldpay (si es necesario)
set VMARGS=-Xbootclasspath/p:applications\accounting\lib\cryptix.jar %VMARGS%

rem ### Rutas específicas
rem Especificar la ruta del archivo JAR de OFBiz
set OFBIZ_JAR=ofbiz.jar

rem ### Diferentes formas de iniciar OFBiz ##############################
rem ### Ejecución estándar con parámetros de la JVM
"%JAVA_HOME%\bin\java" %VMARGS% -jar %OFBIZ_JAR% > %OFBIZ_LOG%

rem ### Modo de depuración (opcional)
rem "%JAVA_HOME%\bin\java" -Xms128M -Xmx512M -Xdebug -Xnoagent -Djava.compiler=NONE ^
rem -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5005 -jar %OFBIZ_JAR% > %OFBIZ_LOG%

rem ### Línea simple y fácil de leer (opción estándar)
echo on
"%JAVA_HOME%\bin\java" -Xms4096M -Xmx8192M -jar %OFBIZ_JAR% > %OFBIZ_LOG%
