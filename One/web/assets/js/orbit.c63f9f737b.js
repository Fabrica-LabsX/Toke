/* TOKE · órbita de Yape, Plin y fichas del proyecto alrededor del logo. No depende de WebGL. */
(function () {
  'use strict';
  var stage = document.getElementById('stage');
  if (!stage) return;
  var planets = [].slice.call(stage.querySelectorAll('[data-orbit]'));
  var reduce = matchMedia('(prefers-reduced-motion: reduce)').matches;

  function size() { stage.style.setProperty('--u', Math.max(46, stage.clientWidth * 0.115) + 'px'); }
  function place(t) {
    var W = stage.clientWidth, H = stage.clientHeight;
    planets.forEach(function (p) {
      var rx = +p.dataset.rx / 100 * W, ry = +p.dataset.ry / 100 * H, phi = +p.dataset.tilt * Math.PI / 180;
      var a = t * 2 * Math.PI / +p.dataset.period + +p.dataset.phase;
      var ex = Math.cos(a) * rx, ey = Math.sin(a) * ry;
      var x = W / 2 + ex * Math.cos(phi) - ey * Math.sin(phi);
      var y = +p.dataset.cy / 100 * H + ex * Math.sin(phi) + ey * Math.cos(phi);
      var front = Math.sin(a) > 0;                       /* delante o detrás del logo */
      p.style.transform = 'translate(' + x + 'px,' + y + 'px) scale(' + (front ? 1 : 0.82) + ')';
      p.style.zIndex = front ? 3 : 1;
    });
  }
  size(); place(0);
  addEventListener('resize', function () { size(); place(performance.now() / 1000); });
  if (reduce) return;
  (function loop(ms) { place(ms / 1000); requestAnimationFrame(loop); })(0);
})();
