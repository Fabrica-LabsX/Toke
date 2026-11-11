/* TOKE · comportamiento común. Sin secretos: solo lee la versión pública. */
(function () {
  'use strict';

  /* Proyecto Supabase público (la URL no es un secreto). Fuente única de versiones: check-app-release. */
  var RELEASE_URL = 'https://ianiulmdyzsqtrqpujkx.supabase.co/functions/v1/check-app-release?channel=stable';

  /* Tema claro/oscuro: por defecto claro; se recuerda lo que elija la persona */
  var temaBtn = document.querySelector('.theme-btn');
  function aplicarTema(t) {
    document.documentElement.setAttribute('data-theme', t);
    if (temaBtn) temaBtn.setAttribute('aria-label', t === 'dark' ? 'Cambiar a modo claro' : 'Cambiar a modo oscuro');
  }
  if (temaBtn) {
    aplicarTema(document.documentElement.getAttribute('data-theme') || 'light');
    temaBtn.addEventListener('click', function () {
      var nuevo = document.documentElement.getAttribute('data-theme') === 'dark' ? 'light' : 'dark';
      aplicarTema(nuevo);
      try { localStorage.setItem('toke-theme', nuevo); } catch (e) {}
    });
  }

  /* Carrusel vertical de "Cómo funciona": marca el paso visible y mueve los puntos */
  var vc = document.querySelector('[data-vcar]');
  if (vc && 'IntersectionObserver' in window) {
    var pista = vc.querySelector('.vtrack');
    var diapos = vc.querySelectorAll('.vslide');
    var puntos = vc.querySelectorAll('[data-go]');
    var marcar = function (n) {
      diapos.forEach(function (d, k) { d.classList.toggle('on', k === n); });
      puntos.forEach(function (p, k) { p.classList.toggle('on', k === n); });
    };
    var io = new IntersectionObserver(function (entradas) {
      entradas.forEach(function (e) {
        if (e.isIntersecting && e.intersectionRatio >= 0.6) marcar(Number(e.target.getAttribute('data-i')));
      });
    }, { root: pista, threshold: [0.6] });
    diapos.forEach(function (d) { io.observe(d); });
    puntos.forEach(function (p) {
      p.addEventListener('click', function () {
        var n = Number(p.getAttribute('data-go'));
        pista.scrollTo({ top: n * pista.clientHeight, behavior: 'smooth' });
      });
    });
    marcar(0);
  } else if (vc) {
    vc.querySelectorAll('.vslide').forEach(function (d) { d.classList.add('on'); });
  }

  /* Menú en celular */
  var btn = document.querySelector('.menu-btn');
  var nav = document.getElementById('nav');
  if (btn && nav) {
    btn.addEventListener('click', function () {
      var open = nav.classList.toggle('open');
      btn.setAttribute('aria-expanded', open ? 'true' : 'false');
    });
    nav.addEventListener('click', function (e) {
      if (e.target.tagName === 'A') { nav.classList.remove('open'); btn.setAttribute('aria-expanded', 'false'); }
    });
  }

  /* Versión publicada */
  var boxes = document.querySelectorAll('[data-release]');
  if (!boxes.length) return;

  function fmtDate(iso) {
    var d = new Date(iso);
    if (isNaN(d)) return '';
    return d.toLocaleDateString('es-PE', { day: 'numeric', month: 'long', year: 'numeric' });
  }
  function fmtMb(bytes) {
    return bytes > 0 ? (bytes / 1048576).toFixed(1).replace('.', ',') + ' MB' : '';
  }
  function safeUrl(u) {
    try { var x = new URL(u); return x.protocol === 'https:' ? x.href : ''; } catch (e) { return ''; }
  }
  function fill(box, r) {
    box.querySelectorAll('[data-f]').forEach(function (el) {
      var k = el.getAttribute('data-f'), v = '';
      if (k === 'version') v = 'Versión ' + r.latestVersionName + ' (' + r.latestVersionCode + ')';
      if (k === 'date') v = fmtDate(r.publishedAt);
      if (k === 'size') v = fmtMb(r.apkSizeBytes);
      if (k === 'sha') v = r.sha256 || '';
      el.textContent = v || 'No disponible';
    });
    var apk = safeUrl(r.apkUrl);
    box.querySelectorAll('[data-apk]').forEach(function (a) {
      if (apk) { a.setAttribute('href', apk); a.removeAttribute('aria-disabled'); } else { a.setAttribute('aria-disabled', 'true'); }
    });
    var ok = box.querySelector('[data-state="ok"]'), loading = box.querySelector('[data-state="loading"]');
    if (loading) loading.hidden = true;
    if (ok) ok.hidden = false;
  }
  function fail(box, empty) {
    var loading = box.querySelector('[data-state="loading"]');
    var msg = box.querySelector(empty ? '[data-state="empty"]' : '[data-state="error"]');
    if (loading) loading.hidden = true;
    if (msg) msg.hidden = false;
  }

  fetch(RELEASE_URL, { headers: { Accept: 'application/json' } })
    .then(function (res) {
      if (res.status === 404) { var e = new Error('empty'); e.empty = true; throw e; }   /* la función responde 404 si aún no hay versión activa */
      if (!res.ok) throw new Error('http ' + res.status);
      return res.json();
    })
    .then(function (r) { boxes.forEach(function (b) { fill(b, r); }); })
    .catch(function (err) { boxes.forEach(function (b) { fail(b, err && err.empty); }); });

  /* Copiar SHA-256 */
  document.addEventListener('click', function (e) {
    var c = e.target.closest('[data-copy]');
    if (!c) return;
    var target = document.querySelector(c.getAttribute('data-copy'));
    if (!target || !navigator.clipboard) return;
    navigator.clipboard.writeText(target.textContent.trim()).then(function () {
      var old = c.textContent; c.textContent = 'Copiado';
      setTimeout(function () { c.textContent = old; }, 1600);
    });
  });

  /* Aviso al pulsar el botón de descarga */
  document.addEventListener('click', function (e) {
    var a = e.target.closest('[data-apk]');
    if (!a) return;
    if (a.getAttribute('aria-disabled') === 'true') { e.preventDefault(); return; }
    var t = document.getElementById('thanks'); if (t) t.classList.add('show');
  });
})();
