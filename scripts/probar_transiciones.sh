#!/usr/bin/env bash
# Prueba de transiciones de TOKE en un teléfono real (pensada para el Redmi 9A, 720x1600, 60 Hz).
# Mide con `dumpsys gfxinfo` los cuadros dibujados mientras se navega y compara con un estándar.
#
# Uso:  scripts/probar_transiciones.sh [serial] [repeticiones]
# Requisitos: teléfono con la app abierta en Inicio de un DUEÑO, en vertical, pantalla encendida.
# Pone las escalas de animación en 1.0 durante la prueba y las restaura al final.
#
# ESTÁNDAR (gama baja, 60 Hz):
#   APRUEBA  cuadros lentos <= 10 %  y  percentil 90 <= 24 ms  y  ningún cuadro congelado (p99 <= 700 ms)
#   ALERTA   cuadros lentos <= 25 %  y  percentil 90 <= 40 ms
#   FALLA    todo lo demás
set -u
export MSYS_NO_PATHCONV=1
ADB="${ADB:-$LOCALAPPDATA/Android/Sdk/platform-tools/adb.exe}"
SERIAL="${1:-}"
REP="${2:-3}"
PKG=com.flabs.cobrate
A() { if [ -n "$SERIAL" ]; then "$ADB" -s "$SERIAL" "$@"; else "$ADB" "$@"; fi; }
SALIDA="${SALIDA:-.artifacts/transiciones}"
mkdir -p "$SALIDA"
CSV="$SALIDA/resultado.csv"
echo "transicion;cuadros;lentos_pct;p50;p90;p95;p99;congelados;veredicto" > "$CSV"

ESC_V=$(A shell settings get global window_animation_scale | tr -d '\r')
ESC_T=$(A shell settings get global transition_animation_scale | tr -d '\r')
ESC_A=$(A shell settings get global animator_duration_scale | tr -d '\r')
restaurar() {
  A shell settings put global window_animation_scale "$ESC_V"
  A shell settings put global transition_animation_scale "$ESC_T"
  A shell settings put global animator_duration_scale "$ESC_A"
}
trap restaurar EXIT
A shell settings put global window_animation_scale 1.0
A shell settings put global transition_animation_scale 1.0
A shell settings put global animator_duration_scale 1.0

tocar() { A shell input tap "$1" "$2"; }
atras() { A shell input keyevent 4; }

# valor_ms "texto de gfxinfo" "prefijo"  -> número entero de la línea "<prefijo> percentile: NNms"
valor_ms() { echo "$1" | grep -m1 "^$2th percentile" | sed 's/.*: *//; s/ms.*//'; }

# medir NOMBRE "comando que dispara la transición" [espera_s]
medir() {
  local nombre="$1" accion="$2" espera="${3:-1.2}"
  A shell dumpsys gfxinfo $PKG reset > /dev/null
  eval "$accion"
  sleep "$espera"
  local g; g=$(A shell dumpsys gfxinfo $PKG | tr -d '\r')
  local total lentos p50 p90 p95 p99
  total=$(echo "$g" | grep -m1 "Total frames rendered" | sed 's/.*: *//')
  lentos=$(echo "$g" | grep -m1 "Janky frames:" | sed 's/.*(//; s/%.*//')
  p50=$(valor_ms "$g" 50); p90=$(valor_ms "$g" 90); p95=$(valor_ms "$g" 95); p99=$(valor_ms "$g" 99)
  total=${total:-0}; lentos=${lentos:-0}; p50=${p50:-0}; p90=${p90:-0}; p95=${p95:-0}; p99=${p99:-0}
  local lentos_i=${lentos%.*}; lentos_i=${lentos_i:-0}
  local cong=0; [ "$p99" -gt 700 ] && cong=1
  local v="FALLA"
  if [ "$lentos_i" -le 10 ] && [ "$p90" -le 24 ] && [ "$cong" -eq 0 ]; then v="APRUEBA"
  elif [ "$lentos_i" -le 25 ] && [ "$p90" -le 40 ]; then v="ALERTA"; fi
  [ "$total" -lt 5 ] && v="SIN_DATOS"
  printf "%-36s cuadros=%-3s lentos=%-6s%% p50=%-3sms p90=%-3sms p95=%-3sms p99=%-4sms  %s\n" "$nombre" "$total" "$lentos" "$p50" "$p90" "$p95" "$p99" "$v"
  echo "$nombre;$total;$lentos;$p50;$p90;$p95;$p99;$cong;$v" >> "$CSV"
}

captura() { A exec-out screencap -p > "$SALIDA/$1.png"; }

echo "== $(A shell getprop ro.product.model | tr -d '\r')  $(A shell wm size | tr -d '\r')  animaciones en 1.0 =="
A shell am start -n $PKG/.MainActivity > /dev/null; sleep 3

for i in $(seq 1 "$REP"); do
  echo "--- vuelta $i ---"
  medir "Inicio -> Operaciones"             "tocar 576 1450"
  medir "Operaciones -> Inicio"             "tocar 144 1450"
  medir "Inicio -> Recibir"                 "tocar 360 1400"
  medir "Recibir -> atras"                  "atras"
  medir "Inicio -> Mis QRs"                 "tocar 190 560"
  medir "Mis QRs -> atras"                  "atras"
  medir "Inicio -> Mis Negocios"            "tocar 520 560"
  medir "Mis Negocios -> atras"             "atras"
  medir "Inicio -> Ver todas"               "tocar 630 716"
  medir "Ver todas -> atras"                "atras"
  medir "Abrir menu Mas"                    "tocar 475 107"
  [ "$i" = "1" ] && captura mas_abierto
  medir "Cerrar menu Mas"                   "atras"
  medir "Abrir selector de negocio"         "tocar 70 95"
  [ "$i" = "1" ] && captura selector_negocio
  medir "Cerrar selector de negocio"        "atras"
done

echo
echo "== Resumen (promedio de $REP vueltas) =="
awk -F';' 'NR>1{n[$1]++; if($9=="APRUEBA")a[$1]++; if($9=="ALERTA")w[$1]++; if($9=="FALLA")f[$1]++; if($9=="SIN_DATOS")s[$1]++; l[$1]+=$3; p[$1]+=$5}
  END{for(k in n){v="APRUEBA"; if(w[k]>0)v="ALERTA"; if(f[k]>0)v="FALLA"; if(s[k]==n[k])v="SIN_DATOS";
      printf "%-36s lentos_prom=%5.1f%%  p90_prom=%4.1fms  %s (%d/%d ok)\n",k,l[k]/n[k],p[k]/n[k],v,a[k]+0,n[k]}}' "$CSV" | sort
echo "CSV: $CSV"
