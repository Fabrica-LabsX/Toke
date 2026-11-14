import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_IMPACT_BOUNCE, SPRING_SMOOTH, TOKE_COLORS } from '../theme';

export const Scene3Demo: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas suaves de paneles
  const topPanelSpring = spring({ frame, fps, config: SPRING_SMOOTH });
  const bottomPanelSpring = spring({ frame: frame - 10, fps, config: SPRING_SMOOTH });

  // Notificación del dueño (deslizamiento directo, sin rebote innecesario)
  const notifY = interpolate(frame, [25, 45], [-30, 0], { extrapolateRight: 'clamp' });
  const notifOpacity = interpolate(frame, [25, 40], [0, 1], { extrapolateRight: 'clamp' });

  // Transmisión de datos hacia abajo
  const beamProgress = interpolate(frame, [50, 95], [0, 100], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const beamOpacity = interpolate(frame, [50, 60, 85, 95], [0, 1, 1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  // MOMENTO DE IMPACTO BOUNCY 1: La llegada del pago confirmado al trabajador
  const workerNotifSpring = spring({
    frame: frame - 90,
    fps,
    config: SPRING_IMPACT_BOUNCE, // Rebote elástico justificado en la confirmación de pago
  });

  // Ondas de voz animadas
  const wavePulse = (Math.sin((frame - 100) * 0.3) + 1) / 2;

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [420, 440], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div
      style={{
        width: 1080,
        height: 1920,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: TOKE_COLORS.fondo,
        opacity: exitOpacity,
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
        padding: '0 50px',
        boxSizing: 'border-box',
        position: 'relative',
      }}
    >
      {/* Audio Narrado de la Escena 3 */}
      <Audio src={staticFile('audio/scene3_demo.mp3')} />

      {/* Cabecera */}
      <div style={{ textAlign: 'center', marginBottom: 35, maxWidth: 960 }}>
        <div
          style={{
            color: TOKE_COLORS.verdeMarca,
            fontSize: 20,
            fontWeight: 800,
            letterSpacing: '2px',
            textTransform: 'uppercase',
            marginBottom: 10,
          }}
        >
          Flujo en Vivo
        </div>
        <h2
          style={{
            fontSize: 54,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: 0,
            lineHeight: 1.2,
            letterSpacing: '-1.5px',
          }}
        >
          El cliente paga por QR, el equipo lo sabe en 1 segundo
        </h2>
      </div>

      {/* Dispositivos Apilados Verticalmente (Formato 9:16) */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
          gap: 20,
          width: '100%',
          maxWidth: 960,
        }}
      >
        {/* PANEL SUPERIOR: Celular del Dueño */}
        <div
          style={{
            width: '100%',
            transform: `scale(${topPanelSpring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 28,
            padding: '24px 30px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            flexDirection: 'column',
            gap: 14,
          }}
        >
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderBottom: `1.5px solid ${TOKE_COLORS.borde}`,
              paddingBottom: 12,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <img src={staticFile('celular.png')} style={{ width: 30, height: 30 }} alt="Móvil" />
              <div>
                <div style={{ color: TOKE_COLORS.textoPrincipal, fontWeight: 800, fontSize: 22 }}>
                  Celular del Dueño
                </div>
                <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 15 }}>
                  Recibe pagos de Yape / Plin
                </div>
              </div>
            </div>
            <span
              style={{
                fontSize: 14,
                background: TOKE_COLORS.tarjetaSecundaria,
                color: TOKE_COLORS.textoPrincipal,
                fontWeight: 700,
                padding: '4px 12px',
                borderRadius: 8,
                border: `1px solid ${TOKE_COLORS.borde}`,
              }}
            >
              Yape Activo
            </span>
          </div>

          {/* Notificación Bancaria */}
          <div
            style={{
              transform: `translateY(${notifY}px)`,
              opacity: notifOpacity,
              background: '#FDF4FF',
              border: `2px solid ${TOKE_COLORS.yapeMorado}`,
              borderRadius: 20,
              padding: '16px 22px',
              display: 'flex',
              flexDirection: 'column',
              gap: 8,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <img src={staticFile('yape.png')} style={{ width: 30, height: 30 }} alt="Yape" />
              <span style={{ color: TOKE_COLORS.yapeMorado, fontWeight: 900, fontSize: 18 }}>
                Yape! Notificación Bancaria
              </span>
              <span style={{ marginLeft: 'auto', color: '#9333EA', fontSize: 14 }}>Ahora</span>
            </div>
            <div style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 24, fontWeight: 800 }}>
              ¡Recibiste S/ 45.00 de Mariana Gómez!
            </div>
            <div
              style={{
                fontSize: 14,
                color: TOKE_COLORS.verdeMarca,
                background: TOKE_COLORS.verdeFondoSolido,
                padding: '4px 12px',
                borderRadius: 8,
                alignSelf: 'flex-start',
                fontWeight: 700,
              }}
            >
              ⚡ TOKE detectó la notificación
            </div>
          </div>
        </div>

        {/* NODO CENTRAL: Flecha Vertical y Nube */}
        <div
          style={{
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 20,
            padding: '4px 0',
          }}
        >
          <div
            style={{
              background: TOKE_COLORS.tarjeta,
              border: `1.5px solid ${TOKE_COLORS.verdeMarca}`,
              borderRadius: 14,
              padding: '8px 20px',
              display: 'flex',
              alignItems: 'center',
              gap: 10,
              boxShadow: '0 4px 12px rgba(0, 166, 81, 0.15)',
            }}
          >
            <span style={{ fontSize: 20 }}>☁️</span>
            <span style={{ color: TOKE_COLORS.verdeMarca, fontWeight: 900, fontSize: 16 }}>
              Supabase Realtime &lt; 100 ms
            </span>
          </div>
          <span style={{ fontSize: 28, color: TOKE_COLORS.verdeMarca, fontWeight: 900 }}>↓</span>
        </div>

        {/* PANEL INFERIOR: Celular del Trabajador */}
        <div
          style={{
            width: '100%',
            transform: `scale(${bottomPanelSpring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 28,
            padding: '24px 30px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            flexDirection: 'column',
            gap: 14,
          }}
        >
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderBottom: `1.5px solid ${TOKE_COLORS.borde}`,
              paddingBottom: 12,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
              <span style={{ fontSize: 28 }}>🧑‍🍳</span>
              <div>
                <div style={{ color: TOKE_COLORS.textoPrincipal, fontWeight: 800, fontSize: 22 }}>
                  Celular del Trabajador
                </div>
                <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 15 }}>
                  Caja 1 / Mozo / Despacho
                </div>
              </div>
            </div>
            <span
              style={{
                fontSize: 14,
                background: TOKE_COLORS.verdeFondoSolido,
                color: TOKE_COLORS.verdeMarca,
                fontWeight: 800,
                padding: '4px 12px',
                borderRadius: 8,
              }}
            >
              ● Conectado
            </span>
          </div>

          {/* Tarjeta de Cobro Confirmado con REBOTE BOUNCY DE IMPACTO */}
          <div
            style={{
              transform: `scale(${workerNotifSpring})`,
              opacity: workerNotifSpring > 0.05 ? 1 : 0,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `2.5px solid ${TOKE_COLORS.verdeMarca}`,
              borderRadius: 22,
              padding: '18px 24px',
              boxShadow: '0 10px 25px rgba(0, 166, 81, 0.25)',
              display: 'flex',
              flexDirection: 'column',
              gap: 8,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
              <span
                style={{
                  color: TOKE_COLORS.verdeMarca,
                  fontWeight: 900,
                  fontSize: 16,
                  textTransform: 'uppercase',
                  letterSpacing: '1px',
                }}
              >
                ✓ Cobro Confirmado
              </span>
              <span style={{ color: TOKE_COLORS.textoSecundario, fontSize: 14, fontWeight: 700 }}>
                Al instante
              </span>
            </div>
            <div style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 38, fontWeight: 900 }}>
              + S/ 45.00
            </div>
            <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 18, fontWeight: 600 }}>
              Mariana Gómez · Yape · Asignado a Caja 1
            </div>

            {/* Aviso por voz integrado */}
            <div
              style={{
                marginTop: 6,
                display: 'flex',
                alignItems: 'center',
                gap: 12,
                background: TOKE_COLORS.tarjeta,
                padding: '8px 16px',
                borderRadius: 12,
                border: `1px solid ${TOKE_COLORS.verdeBordeSolido}`,
              }}
            >
              <span style={{ fontSize: 22 }}>🗣️</span>
              <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 16, fontWeight: 700 }}>
                Voz: "¡Pago de 45 soles recibido!"
              </span>
              <div
                style={{
                  marginLeft: 'auto',
                  width: 12,
                  height: 12,
                  borderRadius: '50%',
                  background: TOKE_COLORS.verdeMarca,
                  transform: `scale(${1 + wavePulse * 0.5})`,
                }}
              />
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
