import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_BOUNCY, SPRING_EXTRA_BOUNCY, TOKE_COLORS } from '../theme';

export const Scene3Demo: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas con rebote
  const leftPanelSpring = spring({ frame, fps, config: SPRING_BOUNCY });
  const rightPanelSpring = spring({ frame: frame - 15, fps, config: SPRING_BOUNCY });

  // Notificación bancaria entrante con caída elástica
  const notifSpring = spring({ frame: frame - 40, fps, config: SPRING_EXTRA_BOUNCY });
  const notifY = interpolate(notifSpring, [0, 1], [-60, 0]);

  // Transmisión de datos
  const beamProgress = interpolate(frame, [80, 140], [0, 100], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });
  const beamOpacity = interpolate(frame, [80, 90, 130, 140], [0, 1, 1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  // Notificación del trabajador con rebote
  const workerNotifSpring = spring({ frame: frame - 130, fps, config: SPRING_EXTRA_BOUNCY });

  // Ondas de voz animadas
  const wavePulse = (Math.sin((frame - 140) * 0.3) + 1) / 2;

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [420, 440], [1, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: TOKE_COLORS.fondo,
        opacity: exitOpacity,
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
        padding: '0 60px',
        position: 'relative',
      }}
    >
      {/* Audio Narrado de la Escena 3 */}
      <Audio src={staticFile('audio/scene3_demo.mp3')} />

      {/* Cabecera */}
      <div style={{ textAlign: 'center', marginBottom: 35 }}>
        <div
          style={{
            color: TOKE_COLORS.verdeMarca,
            fontSize: 16,
            fontWeight: 800,
            letterSpacing: '2px',
            textTransform: 'uppercase',
            marginBottom: 8,
          }}
        >
          Flujo Automático en Vivo
        </div>
        <h2
          style={{
            fontSize: 46,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: 0,
            letterSpacing: '-1px',
          }}
        >
          El cliente paga por QR, el equipo lo sabe en 1 segundo
        </h2>
      </div>

      {/* Dispositivos Frente a Frente (Diseño Sólido, Sin Glassmorphism) */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          gap: 50,
          width: '100%',
          maxWidth: 1200,
        }}
      >
        {/* PANEL IZQUIERDO: Celular Dueño */}
        <div
          style={{
            flex: 1,
            transform: `scale(${leftPanelSpring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 28,
            padding: 30,
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            flexDirection: 'column',
            minHeight: 380,
          }}
        >
          {/* Header Dispositivo */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderBottom: `1.5px solid ${TOKE_COLORS.borde}`,
              paddingBottom: 16,
              marginBottom: 20,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <img src={staticFile('celular.png')} style={{ width: 28, height: 28 }} alt="Móvil" />
              <div>
                <div style={{ color: TOKE_COLORS.textoPrincipal, fontWeight: 800, fontSize: 18 }}>
                  Celular del Dueño
                </div>
                <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 13 }}>
                  Recibe pagos de Yape / Plin
                </div>
              </div>
            </div>
            <span
              style={{
                fontSize: 12,
                background: TOKE_COLORS.tarjetaSecundaria,
                color: TOKE_COLORS.textoPrincipal,
                fontWeight: 700,
                padding: '4px 10px',
                borderRadius: 8,
                border: `1px solid ${TOKE_COLORS.borde}`,
              }}
            >
              Yape Activo
            </span>
          </div>

          {/* Notificación Bancaria que Cae con Rebote */}
          <div
            style={{
              transform: `translateY(${notifY}px) scale(${notifSpring})`,
              opacity: notifSpring > 0.05 ? 1 : 0,
              background: '#FDF4FF',
              border: `2px solid ${TOKE_COLORS.yapeMorado}`,
              borderRadius: 20,
              padding: 20,
              boxShadow: '0 8px 20px rgba(116, 34, 132, 0.15)',
              display: 'flex',
              flexDirection: 'column',
              gap: 10,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <img src={staticFile('yape.png')} style={{ width: 28, height: 28 }} alt="Yape" />
              <span style={{ color: TOKE_COLORS.yapeMorado, fontWeight: 900, fontSize: 15 }}>
                Yape! Notificación Bancaria
              </span>
              <span style={{ marginLeft: 'auto', color: '#9333EA', fontSize: 12 }}>Ahora</span>
            </div>
            <div style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 20, fontWeight: 800 }}>
              ¡Recibiste S/ 45.00 de Mariana Gómez!
            </div>
            <div
              style={{
                fontSize: 13,
                color: TOKE_COLORS.verdeMarca,
                background: TOKE_COLORS.verdeFondoSolido,
                padding: '4px 12px',
                borderRadius: 8,
                alignSelf: 'flex-start',
                fontWeight: 700,
                border: `1px solid ${TOKE_COLORS.verdeBordeSolido}`,
              }}
            >
              ⚡ TOKE detectó la notificación
            </div>
          </div>

          <div
            style={{
              marginTop: 'auto',
              color: TOKE_COLORS.textoSecundario,
              fontSize: 14,
              textAlign: 'center',
              fontWeight: 500,
            }}
          >
            🔒 Seguro: El dueño no comparte contraseñas.
          </div>
        </div>

        {/* NODO CENTRAL: Nube Supabase & Flecha */}
        <div
          style={{
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 12,
            width: 140,
          }}
        >
          <div
            style={{
              background: TOKE_COLORS.tarjeta,
              border: `1.5px solid ${TOKE_COLORS.verdeMarca}`,
              borderRadius: 16,
              padding: '12px 18px',
              textAlign: 'center',
              boxShadow: '0 4px 16px rgba(0, 166, 81, 0.15)',
            }}
          >
            <div style={{ fontSize: 20 }}>☁️</div>
            <div style={{ color: TOKE_COLORS.verdeMarca, fontWeight: 900, fontSize: 13 }}>
              Supabase
            </div>
            <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 11, fontWeight: 600 }}>
              &lt; 100 ms
            </div>
          </div>

          <div
            style={{
              width: '100%',
              height: 6,
              background: TOKE_COLORS.borde,
              borderRadius: 999,
              position: 'relative',
              overflow: 'hidden',
            }}
          >
            <div
              style={{
                position: 'absolute',
                top: 0,
                left: `${beamProgress - 40}%`,
                width: '40%',
                height: '100%',
                background: TOKE_COLORS.verdeMarca,
                opacity: beamOpacity,
              }}
            />
          </div>
          <span style={{ fontSize: 24, color: TOKE_COLORS.verdeMarca, fontWeight: 900 }}>➔</span>
        </div>

        {/* PANEL DERECHO: Celular Trabajador */}
        <div
          style={{
            flex: 1,
            transform: `scale(${rightPanelSpring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 28,
            padding: 30,
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            flexDirection: 'column',
            minHeight: 380,
          }}
        >
          {/* Header Dispositivo */}
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              borderBottom: `1.5px solid ${TOKE_COLORS.borde}`,
              paddingBottom: 16,
              marginBottom: 20,
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
              <span style={{ fontSize: 24 }}>🧑‍🍳</span>
              <div>
                <div style={{ color: TOKE_COLORS.textoPrincipal, fontWeight: 800, fontSize: 18 }}>
                  Celular del Trabajador
                </div>
                <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 13 }}>
                  Caja 1 / Mozo / Despacho
                </div>
              </div>
            </div>
            <span
              style={{
                fontSize: 12,
                background: TOKE_COLORS.verdeFondoSolido,
                color: TOKE_COLORS.verdeMarca,
                fontWeight: 800,
                padding: '4px 10px',
                borderRadius: 8,
                border: `1px solid ${TOKE_COLORS.verdeBordeSolido}`,
              }}
            >
              ● Conectado
            </span>
          </div>

          {/* Tarjeta de Cobro Confirmado con Bouncy Pop */}
          <div
            style={{
              transform: `scale(${workerNotifSpring})`,
              opacity: workerNotifSpring > 0.05 ? 1 : 0,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `2px solid ${TOKE_COLORS.verdeMarca}`,
              borderRadius: 20,
              padding: 20,
              boxShadow: '0 8px 20px rgba(0, 166, 81, 0.2)',
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
                  fontSize: 13,
                  textTransform: 'uppercase',
                  letterSpacing: '1px',
                }}
              >
                ✓ Cobro Confirmado
              </span>
              <span style={{ color: TOKE_COLORS.textoSecundario, fontSize: 12, fontWeight: 700 }}>
                Al instante
              </span>
            </div>
            <div style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 32, fontWeight: 900 }}>
              + S/ 45.00
            </div>
            <div style={{ color: TOKE_COLORS.textoSecundario, fontSize: 15, fontWeight: 600 }}>
              Mariana Gómez · Yape · Asignado a Caja 1
            </div>

            {/* Aviso por voz integrado */}
            <div
              style={{
                marginTop: 6,
                display: 'flex',
                alignItems: 'center',
                gap: 8,
                background: TOKE_COLORS.tarjeta,
                padding: '6px 14px',
                borderRadius: 12,
                border: `1px solid ${TOKE_COLORS.verdeBordeSolido}`,
              }}
            >
              <span style={{ fontSize: 18 }}>🗣️</span>
              <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 13, fontWeight: 700 }}>
                Voz: "¡Pago de 45 soles recibido!"
              </span>
              <div
                style={{
                  marginLeft: 'auto',
                  width: 10,
                  height: 10,
                  borderRadius: '50%',
                  background: TOKE_COLORS.verdeMarca,
                  transform: `scale(${1 + wavePulse * 0.5})`,
                }}
              />
            </div>
          </div>

          <div
            style={{
              marginTop: 'auto',
              color: TOKE_COLORS.verdeMarca,
              fontSize: 14,
              fontWeight: 800,
              textAlign: 'center',
            }}
          >
            ✓ Sin interrumpir al dueño ni pedir capturas.
          </div>
        </div>
      </div>
    </div>
  );
};
