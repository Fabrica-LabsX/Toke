/* TOKE · verificador local de APK. Opera 100% en el navegador. Nunca envía el archivo a ningún servidor. */
(function () {
  'use strict';

  var RELEASE_DATA_URL = '/assets/data/releases.json';
  var MAX_SIZE_TOLERANCE = 1; /* bytes exactos */
  var MAX_ALLOWED_SIZE = 100 * 1024 * 1024; /* 100 MB hard limit */

  /* ── Estado ────────────────────────────────────────────────────── */
  var releaseData = null;
  var currentRelease = null;

  /* ── Elementos del DOM ─────────────────────────────────────────── */
  function $(id) { return document.getElementById(id); }

  var dropZone        = $('verify-drop');
  var fileInput       = $('verify-file');
  var resultBox       = $('verify-result');
  var resultIcon      = $('verify-icon');
  var resultTitle     = $('verify-title');
  var resultDetail    = $('verify-detail');
  var progressBar     = $('verify-progress');
  var progressFill    = $('verify-fill');
  var resetBtn        = $('verify-reset');
  var vtSection       = $('vt-section');
  var vtStatus        = $('vt-status');
  var vtLink          = $('vt-link');
  var vtDate          = $('vt-date');
  var vtDisclaimer    = $('vt-disclaimer');

  /* ── Cargar datos de la release ─────────────────────────────── */
  function loadReleaseData(cb) {
    if (releaseData) return cb(null, releaseData);
    var xhr = new XMLHttpRequest();
    xhr.open('GET', RELEASE_DATA_URL, true);
    xhr.responseType = 'json';
    xhr.onload = function () {
      if (xhr.status === 200 && xhr.response) {
        releaseData = xhr.response;
        var tag = releaseData.current;
        for (var i = 0; i < releaseData.releases.length; i++) {
          if (releaseData.releases[i].tag === tag) {
            currentRelease = releaseData.releases[i];
            break;
          }
        }
        cb(null, releaseData);
      } else {
        cb(new Error('No se pudieron cargar los datos de la versión.'));
      }
    };
    xhr.onerror = function () { cb(new Error('Error de red al cargar datos.')); };
    xhr.send();
  }

  /* ── Rellenar datos dinámicos ──────────────────────────────── */
  function populatePage() {
    if (!currentRelease) return;
    var r = currentRelease;

    /* Bloque A: descarga principal */
    var elVersion = $('dl-version');
    var elDate    = $('dl-date');
    var elSize    = $('dl-size');
    var elAndroid = $('dl-android');
    var elSha     = $('dl-sha256');
    var elBtnDl   = $('dl-button');
    var elBadge   = $('dl-badge');

    if (elVersion) elVersion.textContent = 'Toke ' + r.version + ' para Android';
    if (elDate) elDate.textContent = formatDate(r.date);
    if (elSize) elSize.textContent = formatSize(r.apk.sizeBytes) + ' (' + r.apk.sizeBytes.toLocaleString('es') + ' bytes)';
    if (elAndroid) elAndroid.textContent = 'Android ' + r.android.minVersion + ' o superior';
    if (elSha) elSha.textContent = r.apk.sha256;
    if (elBtnDl) {
      elBtnDl.href = r.apk.downloadUrl;
      elBtnDl.textContent = '';
      var svg = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
      svg.setAttribute('viewBox', '0 0 24 24');
      svg.setAttribute('fill', 'none');
      svg.setAttribute('stroke', 'currentColor');
      svg.setAttribute('stroke-width', '2.5');
      svg.setAttribute('stroke-linecap', 'round');
      svg.setAttribute('stroke-linejoin', 'round');
      svg.setAttribute('aria-hidden', 'true');
      svg.style.cssText = 'width:20px;height:20px;margin-right:6px';
      var path = document.createElementNS('http://www.w3.org/2000/svg', 'path');
      path.setAttribute('d', 'M12 4v11M7 11l5 5 5-5M5 20h14');
      svg.appendChild(path);
      elBtnDl.appendChild(svg);
      elBtnDl.appendChild(document.createTextNode(' Descargar APK v' + r.version));
    }
    if (elBadge) elBadge.textContent = r.mandatory ? 'Actualización obligatoria' : 'Versión oficial vigente';

    /* VirusTotal */
    var vtVersion = $('vt-version');
    var vtBadge = $('vt-badge');
    if (vtSection && r.virusTotal) {
      vtLink.href = r.virusTotal.url;
      vtLink.textContent = 'Ver informe original en VirusTotal';
      if (vtVersion) vtVersion.innerHTML = '<strong>Versión analizada:</strong> Toke ' + r.version + ' (SHA-256 oficial)';
      if (r.virusTotal.status === 'confirmed' && r.virusTotal.detections !== undefined) {
        if (vtBadge) {
          vtBadge.textContent = r.virusTotal.detections + ' / ' + r.virusTotal.totalEngines + ' detecciones';
          vtBadge.style.background = r.virusTotal.detections === 0 ? '#00C06D' : '#DC2626';
          vtBadge.style.color = r.virusTotal.detections === 0 ? '#000000' : '#FFFFFF';
        }
        vtStatus.textContent = r.virusTotal.detections === 0 
          ? 'Análisis limpio: ninguno de los ' + r.virusTotal.totalEngines + ' motores antivirus detectó amenazas.' 
          : r.virusTotal.detections + ' de ' + r.virusTotal.totalEngines + ' motores detectaron el archivo.';
        vtDate.textContent = r.virusTotal.analysisDate ? 'Fecha del análisis: ' + r.virusTotal.analysisDate : '';
      } else if (r.virusTotal.status === 'pending_confirmation') {
        if (vtBadge) vtBadge.textContent = 'Informe en proceso';
        vtStatus.textContent = 'Informe disponible en VirusTotal para esta versión.';
        vtDate.textContent = '';
      } else {
        if (vtBadge) vtBadge.style.display = 'none';
        vtStatus.textContent = 'Todavía no hay un informe de análisis publicado para esta versión.';
        vtDate.textContent = '';
      }
    }

    /* Certificado */
    var elCert = $('cert-subject');
    var elCertSha = $('cert-sha256');
    if (elCert && r.signature) elCert.textContent = r.signature.subject;
    if (elCertSha && r.signature) elCertSha.textContent = r.signature.sha256Fingerprint;

    /* Modal advertencia al tocar descargar */
    var modalAdv = $('modal-advertencia');
    var modalAdvConfirm = $('modal-adv-confirm');
    var modalAdvClose = $('modal-adv-close');
    if (elBtnDl && modalAdv) {
      modalAdvConfirm.href = r.apk.downloadUrl;
      elBtnDl.addEventListener('click', function (e) {
        e.preventDefault();
        modalAdv.classList.add('is-open');
      });
      if (modalAdvConfirm) {
        modalAdvConfirm.addEventListener('click', function () {
          modalAdv.classList.remove('is-open');
        });
      }
      if (modalAdvClose) {
        modalAdvClose.addEventListener('click', function () {
          modalAdv.classList.remove('is-open');
        });
      }
      modalAdv.addEventListener('click', function (e) {
        if (e.target === modalAdv) modalAdv.classList.remove('is-open');
      });
    }

    /* Ficha técnica JSON */
    var elFicha = $('ficha-json');
    if (elFicha) {
      elFicha.textContent = JSON.stringify(buildTransparencyCard(r), null, 2);
    }
  }

  function buildTransparencyCard(r) {
    return {
      fichaVersion: '1.0',
      generada: new Date().toISOString().slice(0, 10),
      identidad: {
        nombre: 'TOKE',
        paquete: r.package,
        version: r.version,
        versionCode: r.versionCode,
        fechaPublicacion: r.date,
        androidMinimo: r.android.minVersion + ' (SDK ' + r.android.minSdk + ')',
        targetSdk: r.android.targetSdk
      },
      apk: {
        nombre: r.apk.name,
        tamanoBytes: r.apk.sizeBytes,
        sha256: r.apk.sha256,
        descarga: r.apk.downloadUrl
      },
      firma: r.signature,
      virusTotal: {
        enlace: r.virusTotal.url,
        estado: r.virusTotal.status
      },
      permisos: releaseData ? releaseData.permissions : null,
      privacidad: releaseData ? releaseData.privacy : null,
      nota: 'Esta ficha facilita una revisión técnica independiente, pero no demuestra por sí sola todo el comportamiento de la aplicación durante su ejecución.'
    };
  }

  /* ── Formateo ──────────────────────────────────────────────── */
  function formatDate(iso) {
    var meses = ['enero', 'febrero', 'marzo', 'abril', 'mayo', 'junio', 'julio', 'agosto', 'septiembre', 'octubre', 'noviembre', 'diciembre'];
    var d = new Date(iso + 'T12:00:00');
    return d.getDate() + ' de ' + meses[d.getMonth()] + ' de ' + d.getFullYear();
  }

  function formatSize(bytes) {
    if (bytes < 1024) return bytes + ' B';
    if (bytes < 1048576) return (bytes / 1024).toFixed(1) + ' KB';
    return (bytes / 1048576).toFixed(1) + ' MB';
  }

  /* ── Verificación ──────────────────────────────────────────── */
  function showResult(type, title, detail) {
    resultBox.className = 'verify-result show ' + type;
    resultTitle.textContent = title;
    resultDetail.textContent = detail || '';
    progressBar.hidden = true;
    resetBtn.hidden = false;

    var icons = {
      success: 'M5 12.5l4.5 4.5L19 7.5',
      error: 'M6 6l12 12M18 6L6 18',
      warning: 'M12 8v5M12 16.5v.01'
    };
    resultIcon.innerHTML = '<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" stroke-linejoin="round" aria-hidden="true"><path d="' + (icons[type] || icons.warning) + '"/></svg>';
  }

  function verifyFile(file) {
    if (!currentRelease) {
      showResult('warning', 'No pudimos completar la verificación.', 'Los datos de la versión oficial no están disponibles. Inténtalo nuevamente.');
      return;
    }

    var expected = currentRelease.apk;

    /* Tamaño demasiado grande para ser un APK razonable */
    if (file.size > MAX_ALLOWED_SIZE) {
      showResult('error', 'El archivo no es compatible con esta comprobación.', 'El archivo es demasiado grande (' + formatSize(file.size) + '). El APK oficial pesa ' + formatSize(expected.sizeBytes) + '.');
      return;
    }

    /* Comprobar tamaño exacto antes de leer el contenido */
    if (file.size !== expected.sizeBytes) {
      showResult('error', 'El archivo no coincide. No lo instales.', 'El tamaño del archivo (' + file.size.toLocaleString('es') + ' bytes) no coincide con el APK oficial (' + expected.sizeBytes.toLocaleString('es') + ' bytes). Descarga nuevamente desde el enlace oficial.');
      return;
    }

    /* El tamaño coincide. Calcular SHA-256. */
    if (!window.crypto || !window.crypto.subtle) {
      showResult('warning', 'Tu navegador no permite verificar el archivo.', 'Se requiere Web Crypto API (SHA-256). Prueba con un navegador actualizado como Chrome, Firefox o Edge.');
      return;
    }

    progressBar.hidden = false;
    progressFill.style.width = '0%';
    resultBox.className = 'verify-result show computing';
    resultTitle.textContent = 'Calculando integridad del archivo…';
    resultDetail.textContent = 'El archivo NO se envía a ningún servidor. Todo se calcula en tu dispositivo.';
    resultIcon.innerHTML = '<svg class="spin" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2.5" stroke-linecap="round" aria-hidden="true"><path d="M12 2a10 10 0 0 1 10 10"/></svg>';
    resetBtn.hidden = true;

    var reader = new FileReader();
    reader.onprogress = function (e) {
      if (e.lengthComputable) {
        var pct = Math.round((e.loaded / e.total) * 100);
        progressFill.style.width = pct + '%';
      }
    };
    reader.onerror = function () {
      showResult('warning', 'No pudimos completar la verificación.', 'Ocurrió un error al leer el archivo. Inténtalo nuevamente.');
    };
    reader.onload = function () {
      progressFill.style.width = '100%';
      crypto.subtle.digest('SHA-256', reader.result).then(function (hash) {
        var hex = Array.from(new Uint8Array(hash)).map(function (b) {
          return b.toString(16).padStart(2, '0');
        }).join('');

        if (hex === expected.sha256) {
          showResult('success',
            'El archivo coincide con la versión oficial publicada.',
            'SHA-256 verificado: ' + hex.slice(0, 16) + '…' + hex.slice(-16) + '. Este archivo es idéntico al publicado por Fábrica Labs. Una coincidencia SHA-256 acredita igualdad bit a bit con el archivo de referencia.');
        } else {
          showResult('error',
            'El archivo no coincide. No lo instales.',
            'El hash SHA-256 calculado no coincide con el publicado. Esto significa que el archivo ha sido modificado o no proviene de la fuente oficial. Descarga nuevamente desde el enlace oficial de arriba.');
        }
      }).catch(function () {
        showResult('warning', 'No pudimos completar la verificación.', 'Error al calcular el hash. Inténtalo nuevamente.');
      });
    };
    reader.readAsArrayBuffer(file);
  }

  /* ── Eventos ───────────────────────────────────────────────── */
  if (fileInput) {
    fileInput.addEventListener('change', function () {
      if (fileInput.files && fileInput.files[0]) verifyFile(fileInput.files[0]);
    });
  }

  if (dropZone) {
    dropZone.addEventListener('click', function (e) {
      if (e.target === resetBtn || e.target.closest('#verify-reset')) return;
      if (resultBox && !resultBox.classList.contains('show') && fileInput) fileInput.click();
    });

    dropZone.addEventListener('dragover', function (e) {
      e.preventDefault();
      dropZone.classList.add('dragover');
    });
    dropZone.addEventListener('dragleave', function () {
      dropZone.classList.remove('dragover');
    });
    dropZone.addEventListener('drop', function (e) {
      e.preventDefault();
      dropZone.classList.remove('dragover');
      if (e.dataTransfer.files && e.dataTransfer.files[0]) verifyFile(e.dataTransfer.files[0]);
    });
  }

  if (resetBtn) {
    resetBtn.addEventListener('click', function (e) {
      e.stopPropagation();
      resultBox.className = 'verify-result';
      resultTitle.textContent = '';
      resultDetail.textContent = '';
      progressBar.hidden = true;
      resetBtn.hidden = true;
      fileInput.value = '';
    });
  }

  /* ── Copiar consulta para IA ──────────────────────────────── */
  var copyFicha = $('copy-ficha');
  if (copyFicha) {
    copyFicha.addEventListener('click', function () {
      var promptEl = $('prompt-ia') || $('ficha-json');
      if (!promptEl || !navigator.clipboard) return;
      navigator.clipboard.writeText(promptEl.textContent.trim()).then(function () {
        var old = copyFicha.innerHTML;
        copyFicha.textContent = '¡Copiado para tu IA!';
        setTimeout(function () { copyFicha.innerHTML = old; }, 2000);
      });
    });
  }

  /* ── Zoom en capturas de VirusTotal ────────────────────────── */
  var vtBackdrop = $('vt-backdrop');
  var vtFigures = document.querySelectorAll('.vt-shot-figure');
  vtFigures.forEach(function (fig) {
    fig.addEventListener('click', function () {
      var expanded = fig.classList.toggle('is-expanded');
      if (vtBackdrop) vtBackdrop.classList.toggle('is-active', expanded);
    });
    fig.addEventListener('keydown', function (e) {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        var expanded = fig.classList.toggle('is-expanded');
        if (vtBackdrop) vtBackdrop.classList.toggle('is-active', expanded);
      }
    });
  });
  if (vtBackdrop) {
    vtBackdrop.addEventListener('click', function () {
      vtFigures.forEach(function (fig) { fig.classList.remove('is-expanded'); });
      vtBackdrop.classList.remove('is-active');
    });
  }
  document.addEventListener('keydown', function (e) {
    if (e.key === 'Escape') {
      vtFigures.forEach(function (fig) { fig.classList.remove('is-expanded'); });
      if (vtBackdrop) vtBackdrop.classList.remove('is-active');
      var m = $('modal-advertencia');
      if (m) m.classList.remove('is-open');
    }
  });

  /* ── Permisos: gaveta ─────────────────────────────────────── */
  function renderPermissions() {
    if (!releaseData || !releaseData.permissions) return;
    var container = $('permisos-lista');
    if (!container) return;

    var p = releaseData.permissions;
    var html = '';

    function permBlock(items, label) {
      if (!items || items.length === 0) return '';
      var s = '<h4 class="perm-cat">' + label + '</h4>';
      for (var i = 0; i < items.length; i++) {
        var it = items[i];
        s += '<div class="perm-item">';
        s += '<h5>' + it.name;
        if (it.source && it.source !== 'app') {
          s += ' <span class="perm-lib">(biblioteca externa)</span>';
        }
        s += '</h5>';
        s += '<p>' + it.description + '</p>';
        if (it.detail) s += '<p class="perm-detail">' + it.detail + '</p>';
        if (it.important) s += '<p class="perm-important">' + it.important + '</p>';
        s += '</div>';
      }
      return s;
    }

    html += permBlock(p.special, 'Accesos especiales');
    html += permBlock(p.dangerous, 'Permisos de ejecución');
    html += permBlock(p.normal, 'Permisos normales');

    if (p.notRequested && p.notRequested.length > 0) {
      html += '<h4 class="perm-cat">Lo que TOKE no solicita</h4>';
      html += '<div class="perm-not-list">';
      for (var j = 0; j < p.notRequested.length; j++) {
        html += '<span class="perm-not">' + p.notRequested[j] + '</span>';
      }
      html += '</div>';
    }

    container.innerHTML = html;
  }

  /* ── Novedades: historial ──────────────────────────────────── */
  function renderHistory() {
    var container = $('releases-history');
    if (!container || !releaseData) return;

    var releases = releaseData.releases;
    var html = '';

    for (var i = 0; i < releases.length; i++) {
      var r = releases[i];
      var isCurrent = r.tag === releaseData.current;
      html += '<div class="release' + (isCurrent ? '' : ' release-old') + '">';
      html += '<div class="release-head">';
      html += '<h2>Versión ' + r.version + '</h2>';
      if (isCurrent) {
        html += '<span class="release-badge current">' + (r.mandatory ? 'Actualización obligatoria' : 'Versión vigente') + '</span>';
      } else {
        html += '<span class="release-badge old">Versión histórica · no recomendada</span>';
      }
      html += '</div>';
      html += '<p class="state">Publicada el ' + formatDate(r.date) + ' · Tamaño: ' + formatSize(r.apk.sizeBytes) + ' (' + r.apk.sizeBytes.toLocaleString('es') + ' bytes) · Android ' + r.android.minVersion + ' o superior</p>';
      html += '<p class="state">SHA-256: <code class="mono">' + r.apk.sha256 + '</code></p>';

      if (r.notes && r.notes.length > 0) {
        html += '<h3>Novedades y optimizaciones</h3><ul>';
        for (var n = 0; n < r.notes.length; n++) {
          html += '<li>' + r.notes[n] + '</li>';
        }
        html += '</ul>';
      }

      html += '<div class="actions" style="margin-top:20px">';
      html += '<a class="btn btn-primary btn-sm" href="' + r.apk.downloadUrl + '">Descargar APK v' + r.version + '</a>';
      html += '<a class="btn btn-secondary btn-sm" href="https://github.com/Fabrica-LabsX/toke-releases/releases/tag/' + r.tag + '" target="_blank" rel="noopener">Ver release en GitHub</a>';
      if (r.virusTotal && r.virusTotal.url) {
        html += '<a class="btn btn-secondary btn-sm" href="' + r.virusTotal.url + '" target="_blank" rel="noopener">VirusTotal</a>';
      }
      html += '</div></div>';
    }

    container.innerHTML = html;
  }

  /* ── Inicialización ────────────────────────────────────────── */
  loadReleaseData(function (err) {
    if (err) {
      if (resultBox) showResult('warning', 'No se pudieron cargar los datos.', err.message);
      return;
    }
    populatePage();
    renderPermissions();
    renderHistory();
  });

})();
