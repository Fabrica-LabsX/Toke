/* TOKE · tema inicial. Va en <head> para evitar el destello: por defecto, claro. */
(function () {
  var t = 'light';
  try { var g = localStorage.getItem('toke-theme'); if (g === 'dark' || g === 'light') t = g; } catch (e) {}
  document.documentElement.setAttribute('data-theme', t);
})();
