#!/bin/bash
# Compila con tope de 3 minutos. Mata antes los procesos Java colgados (la causa de las compilaciones eternas).
# Uso: .artifacts/compilar.sh assembleRelease   (tests aparte: .artifacts/compilar.sh :app:testDebugUnitTest). Omite lintVital (lento); correrlo solo antes de publicar.
cd "$(dirname "$0")/.."
export JAVA_HOME="C:/Program Files/Android/Android Studio/jbr"
powershell -NoProfile -Command "Get-Process java -ErrorAction SilentlyContinue | Stop-Process -Force" 2>/dev/null
sleep 2
S=$(date +%s)
timeout 180 ./gradlew "${@:-assembleRelease}" -x lintVitalRelease -x lintVitalAnalyzeRelease -x lintVitalReportRelease --console=plain > .artifacts/build.log 2>&1
R=$?
echo "salida=$R tiempo=$(( $(date +%s)-S ))s"
[ $R -eq 124 ] && echo "SE PASO DE 3 MIN: ultima tarea:" && grep "^> Task" .artifacts/build.log | tail -1
grep -E "^e: |FAILED|BUILD" .artifacts/build.log | head -20
cp .artifacts/compilar.sh /c/Users/OneProyect/Documents/TOKE-privado/scripts/compilar.sh 2>/dev/null
exit $R
