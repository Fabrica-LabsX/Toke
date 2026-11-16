// TOKE · Playwright E2E tests para /descargas y /novedades
// Ejecutar: npx playwright test --config=One/tests/playwright.config.js
//
// Prerequisito: servir web/ localmente con wrangler pages dev

const { test, expect } = require('@playwright/test');

const BASE = process.env.TEST_URL || 'http://localhost:8788';

test.describe('/descargas — Centro de Descargas y Advertencias', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE + '/descargas');
  });

  test('carga y muestra versión desde releases.json', async ({ page }) => {
    await expect(page.locator('#dl-version')).not.toHaveText('Toke para Android', { timeout: 5000 });
    const version = await page.locator('#dl-version').textContent();
    expect(version).toMatch(/Toke \d+\.\d+\.\d+ para Android/);
  });

  test('muestra SHA-256 completo', async ({ page }) => {
    await page.waitForTimeout(1000);
    const sha = await page.locator('#dl-sha256').textContent();
    expect(sha).toMatch(/^[0-9a-f]{64}$/);
  });

  test('muestra advertencia en lenguaje de calle antes del botón', async ({ page }) => {
    const adv = page.locator('#advertencia-descarga');
    await expect(adv).toBeVisible();
    await expect(adv.locator('h3')).toHaveText('Antes de descargar, esto es lo que debes saber');
    await expect(adv).toContainText('TOKE aun no está en Play Store');
    await expect(adv).toContainText('1. TOKE lee las notificaciones de tu celular.');
    await expect(adv).toContainText('2. Las actualizaciones no pasan por Play Store.');
    await expect(adv).toContainText('3. Hoy esto lo afirmamos nosotros mismos.');
    await expect(adv).not.toContainText('100% seguro');
    await expect(adv).not.toContainText('SHA-256');
    await expect(adv).not.toContainText('firma RSA');
  });

  test('botón de descarga apunta a GitHub Releases y abre advertencia al pulsar', async ({ page }) => {
    await page.waitForTimeout(1000);
    const btn = page.locator('#dl-button');
    const href = await btn.getAttribute('href');
    expect(href).toContain('github.com/Fabrica-LabsX/toke-releases/releases/download/');
    expect(href).toContain('.apk');

    // Al tocar el botón asalta la advertencia modal
    await btn.click();
    const modal = page.locator('#modal-advertencia');
    await expect(modal).toHaveClass(/is-open/);
    await expect(modal.locator('#modal-adv-title')).toContainText('Antes de descargar, esto es lo que debes saber');

    // Confirmar en el modal tiene el enlace directo
    const confirmBtn = page.locator('#modal-adv-confirm');
    expect(await confirmBtn.getAttribute('href')).toContain('.apk');

    // Cerrar modal
    await page.locator('#modal-adv-close').click();
    await expect(modal).not.toHaveClass(/is-open/);
  });

  test('enlace VirusTotal apunta al hash correcto con badge limpio', async ({ page }) => {
    await page.waitForTimeout(1000);
    const href = await page.locator('#vt-link').getAttribute('href');
    expect(href).toContain('virustotal.com/gui/file/');
    expect(href).toMatch(/[0-9a-f]{64}/);
    const badge = await page.locator('#vt-badge').textContent();
    expect(badge).toContain('0 / 67');
  });

  test('capturas VirusTotal se adaptan según el tema (sólo 1 visible) y se amplían al tocar', async ({ page }) => {
    // Abrir desplegable de capturas
    await page.locator('.vt-preview-toggle summary').click();

    const shotLight = page.locator('.vt-shot-figure.vt-theme-light');
    const shotDark = page.locator('.vt-shot-figure.vt-theme-dark');

    // En tema claro (por defecto): sólo visible la clara
    await expect(shotLight).toBeVisible();
    await expect(shotDark).toBeHidden();

    // Tocar la captura para hacerla más grande
    await shotLight.click();
    await expect(shotLight).toHaveClass(/is-expanded/);
    // Tocar de nuevo para reducirla
    await shotLight.click();
    await expect(shotLight).not.toHaveClass(/is-expanded/);

    // Cambiar a tema oscuro
    await page.locator('.theme-btn').click();
    await expect(page.locator('html')).toHaveAttribute('data-theme', 'dark');

    // En tema oscuro: sólo visible la oscura
    await expect(shotDark).toBeVisible();
    await expect(shotLight).toBeHidden();

    // Tocar la captura oscura para hacerla más grande
    await shotDark.click();
    await expect(shotDark).toHaveClass(/is-expanded/);
    await page.keyboard.press('Escape');
    await expect(shotDark).not.toHaveClass(/is-expanded/);
  });

  test('resumen de privacidad explica nombres y montos claramente', async ({ page }) => {
    const callout = page.locator('.callout');
    await expect(callout).toContainText('nombres y montos');
    await expect(callout).toContainText('Qué datos se guardan');
    await expect(callout).toContainText('Dónde se guardan');
    await expect(callout).toContainText('Quién los ve');
    await expect(callout).toContainText('Cómo se borran');
    await expect(callout).toContainText('Supabase, São Paulo');
  });

  test('permisos se cargan dinámicamente con clasificaciones', async ({ page }) => {
    await page.locator('.permisos-section summary').click();
    await page.waitForTimeout(500);
    const body = await page.locator('#permisos-lista').textContent();
    expect(body).toContain('Acceso a notificaciones');
    expect(body).toContain('Acceso a internet');
    expect(body).toContain('Ubicación');
    expect(body).toContain('Cámara');
  });

  test('certificado muestra emisor oficial', async ({ page }) => {
    await page.waitForTimeout(1000);
    const subject = await page.locator('#cert-subject').textContent();
    expect(subject).toContain('TOKE');
    const sha = await page.locator('#cert-sha256').textContent();
    expect(sha).toMatch(/[0-9A-F:]{95}/);
  });
});

test.describe('/descargas — Verificador de APK local', () => {

  test.beforeEach(async ({ page }) => {
    await page.goto(BASE + '/descargas');
    await page.waitForTimeout(1000);
  });

  test('rechaza archivo con tamaño incorrecto sin leer contenido', async ({ page }) => {
    const wrongContent = Buffer.alloc(1000, 0x41);
    await page.locator('#verify-file').setInputFiles({
      name: 'fake.apk',
      mimeType: 'application/vnd.android.package-archive',
      buffer: wrongContent
    });
    await expect(page.locator('#verify-title')).toHaveText(/no coincide/i, { timeout: 3000 });
    await expect(page.locator('#verify-detail')).toContainText('tamaño');
  });

  test('muestra estado de error para hash incorrecto', async ({ page }) => {
    const correctSize = 4774155;
    const fakeContent = Buffer.alloc(correctSize, 0x42);
    await page.locator('#verify-file').setInputFiles({
      name: 'tampered.apk',
      mimeType: 'application/vnd.android.package-archive',
      buffer: fakeContent
    });
    await expect(page.locator('#verify-title')).toHaveText(/no coincide/i, { timeout: 30000 });
    await expect(page.locator('#verify-detail')).toContainText('hash');
  });

  test('botón reset permite verificar otro archivo', async ({ page }) => {
    const wrongContent = Buffer.alloc(500, 0x41);
    await page.locator('#verify-file').setInputFiles({
      name: 'wrong.apk',
      mimeType: 'application/vnd.android.package-archive',
      buffer: wrongContent
    });
    await expect(page.locator('#verify-title')).not.toBeEmpty({ timeout: 3000 });
    await page.locator('#verify-reset').click();
    await expect(page.locator('#verify-result')).not.toHaveClass(/show/);
  });
});

test.describe('/novedades — Historial de versiones', () => {

  test('carga releases desde releases.json', async ({ page }) => {
    await page.goto(BASE + '/novedades');
    await page.waitForTimeout(1500);
    const history = page.locator('#releases-history');
    await expect(history).not.toContainText('Cargando', { timeout: 5000 });
    const releases = await history.locator('.release').count();
    expect(releases).toBeGreaterThanOrEqual(1);
  });

  test('versión vigente tiene badge correcto', async ({ page }) => {
    await page.goto(BASE + '/novedades');
    await page.waitForTimeout(1500);
    const badge = await page.locator('.release-badge.current').first().textContent();
    expect(badge).toMatch(/obligatoria|vigente/i);
  });
});

test.describe('Adaptabilidad y Responsividad (9:16 móvil, 4:3 tablet)', () => {

  test('adaptado para teléfonos 9:16 (390x844)', async ({ page }) => {
    await page.setViewportSize({ width: 390, height: 844 });
    await page.goto(BASE + '/descargas');
    await expect(page.locator('.dl-hero')).toBeVisible();
    await expect(page.locator('#advertencia-descarga')).toBeVisible();
    await expect(page.locator('#dl-button')).toBeVisible();

    // Menú móvil
    const menuBtn = page.locator('.menu-btn');
    await expect(menuBtn).toBeVisible();
    await menuBtn.click();
    await expect(page.locator('.nav')).toHaveClass(/open/);
  });

  test('adaptado para tablets 4:3 vertical (768x1024)', async ({ page }) => {
    await page.setViewportSize({ width: 768, height: 1024 });
    await page.goto(BASE + '/descargas');
    await expect(page.locator('.dl-hero')).toBeVisible();
    await expect(page.locator('#advertencia-descarga')).toBeVisible();
    await expect(page.locator('#revisa-ia')).toBeVisible();
  });

  test('adaptado para tablets 4:3 horizontal (1024x768)', async ({ page }) => {
    await page.setViewportSize({ width: 1024, height: 768 });
    await page.goto(BASE + '/descargas');
    await expect(page.locator('.dl-hero')).toBeVisible();
    await expect(page.locator('#revisa-ia')).toBeVisible();
  });
});

test.describe('Seguridad y privacidad', () => {

  test('no hay tokens ni secretos en el HTML', async ({ page }) => {
    await page.goto(BASE + '/descargas');
    const content = await page.content();
    expect(content).not.toMatch(/ghp_[a-zA-Z0-9]{36}/);
    expect(content).not.toMatch(/sk-[a-zA-Z0-9]{32}/);
    expect(content).not.toMatch(/SUPABASE_SERVICE_ROLE/i);
    expect(content).not.toMatch(/eyJ[a-zA-Z0-9_-]{30,}/);
  });

  test('no hay solicitudes a servidores externos durante la carga o verificación', async ({ page }) => {
    const externalRequests = [];
    page.on('request', (req) => {
      const url = req.url();
      if (!url.startsWith(BASE) && !url.startsWith('data:')) {
        externalRequests.push(url);
      }
    });
    await page.goto(BASE + '/descargas');
    await page.waitForTimeout(2000);
    expect(externalRequests).toHaveLength(0);
  });
});
