/* TOKE · comportamiento común. Sin red: la web no se conecta con la app ni con ningún servicio. */
(function () {
  'use strict';

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
