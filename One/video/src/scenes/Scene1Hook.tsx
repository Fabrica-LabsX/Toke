import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_BOUNCY, SPRING_EXTRA_BOUNCY, TOKE_COLORS } from '../theme';

export const Scene1Hook: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas con física de rebote
  const badgeSpring = spring({ frame: frame - 5, fps, config: SPRING_EXTRA_BOUNCY });
  const titleSpring = spring({ frame: frame - 15, fps, config: SPRING_BOUNCY });
  const yapeSpring = spring({ frame: frame - 30, fps, config: SPRING_EXTRA_BOUNCY });
  const plinSpring = spring({ frame: frame - 40, fps, config: SPRING_EXTRA_BOUNCY });

  // Tarjetas de fricción escalonadas
  const card1Spring = spring({ frame: frame - 70, fps, config: SPRING_EXTRA_BOUNCY });
  const card2Spring = spring({ frame: frame - 130, fps, config: SPRING_EXTRA_BOUNCY });
  const card3Spring = spring({ frame: frame - 190, fps, config: SPRING_EXTRA_BOUNCY });
  const footerSpring = spring({ frame: frame - 250, fps, config: SPRING_BOUNCY });

  // Oscilación sutil de las billeteras
  const bounceY = Math.sin((frame - 50) * 0.12) * 6;

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [380, 400], [1, 0], {
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
        padding: '0 80px',
        position: 'relative',
      }}
    >
      {/* Audio Narrado de la Escena 1 */}
      <Audio src={staticFile('audio/scene1_hook.mp3')} />

      {/* Badge Alerta Superior */}
      <div
        style={{
          transform: `scale(${badgeSpring})`,
          background: TOKE_COLORS.rojoFondoSolido,
          border: `1.5px solid ${TOKE_COLORS.rojoAlerta}`,
          padding: '8px 24px',
          borderRadius: 999,
          display: 'flex',
          alignItems: 'center',
          gap: 10,
          marginBottom: 20,
        }}
      >
        <span style={{ fontSize: 20 }}>⚠️</span>
        <span
          style={{
            color: TOKE_COLORS.rojoAlerta,
            fontWeight: 800,
            fontSize: 16,
            letterSpacing: '1px',
            textTransform: 'uppercase',
          }}
        >
          El Problema Diario en Mostrador
        </span>
      </div>

      {/* Título Principal de Impacto */}
      <div
        style={{
          transform: `scale(${titleSpring})`,
          textAlign: 'center',
          marginBottom: 35,
        }}
      >
        <h1
          style={{
            fontSize: 54,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: '0 0 14px 0',
            letterSpacing: '-1.5px',
          }}
        >
          ¿Tu negocio se detiene cada vez que pagan con{' '}
          <span style={{ color: TOKE_COLORS.yapeMorado }}>Yape</span> o{' '}
          <span style={{ color: TOKE_COLORS.plinAzul }}>Plin</span>?
        </h1>
        <p
          style={{
            fontSize: 24,
            color: TOKE_COLORS.textoSecundario,
            margin: 0,
            fontWeight: 500,
          }}
        >
          En hora punta, cada cobro por QR paraliza la atención de tus clientes.
        </p>
      </div>

      {/* Logos de Yape y Plin rebotando con colores sólidos */}
      <div style={{ display: 'flex', gap: 30, marginBottom: 35 }}>
        <div
          style={{
            transform: `scale(${yapeSpring}) translateY(${frame > 50 ? bounceY : 0}px) rotate(-4deg)`,
            background: TOKE_COLORS.yapeMorado,
            padding: '12px 28px',
            borderRadius: 20,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            boxShadow: '0 8px 20px rgba(116, 34, 132, 0.25)',
          }}
        >
          <img src={staticFile('yape.png')} style={{ width: 36, height: 36 }} alt="Yape" />
          <span style={{ color: '#fff', fontSize: 24, fontWeight: 900 }}>Yape</span>
        </div>

        <div
          style={{
            transform: `scale(${plinSpring}) translateY(${frame > 50 ? -bounceY : 0}px) rotate(4deg)`,
            background: TOKE_COLORS.plinAzul,
            padding: '12px 28px',
            borderRadius: 20,
            display: 'flex',
            alignItems: 'center',
            gap: 12,
            boxShadow: '0 8px 20px rgba(0, 102, 255, 0.25)',
          }}
        >
          <img src={staticFile('plin.png')} style={{ width: 36, height: 36 }} alt="Plin" />
          <span style={{ color: '#fff', fontSize: 24, fontWeight: 900 }}>Plin</span>
        </div>
      </div>

      {/* Tarjetas de Fricción en Caja (Blanco sólido con sombra limpia) */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: 16,
          width: '100%',
          maxWidth: 820,
        }}
      >
        <div
          style={{
            transform: `scale(${card1Spring}) translateX(-20px)`,
            alignSelf: 'flex-start',
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.borde}`,
            borderRadius: '20px 20px 20px 4px',
            padding: '16px 26px',
            boxShadow: '0 4px 14px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            alignItems: 'center',
            gap: 14,
          }}
        >
          <span style={{ fontSize: 24 }}>🗣️</span>
          <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 22, fontWeight: 700 }}>
            "— ¡Jefe, le llegó el Yape de la mesa 4?"
          </span>
        </div>

        <div
          style={{
            transform: `scale(${card2Spring}) translateX(20px)`,
            alignSelf: 'flex-end',
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.borde}`,
            borderRadius: '20px 20px 4px 20px',
            padding: '16px 26px',
            boxShadow: '0 4px 14px rgba(15, 23, 42, 0.08)',
            display: 'flex',
            alignItems: 'center',
            gap: 14,
          }}
        >
          <span style={{ fontSize: 24 }}>📱</span>
          <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 22, fontWeight: 700 }}>
            "— Espera que mire mi teléfono o mándame captura..."
          </span>
        </div>

        <div
          style={{
            transform: `scale(${card3Spring})`,
            alignSelf: 'center',
            background: TOKE_COLORS.ambarFondoSolido,
            border: `1.5px solid ${TOKE_COLORS.ambarBordeSolido}`,
            borderRadius: 20,
            padding: '14px 28px',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <span style={{ fontSize: 22 }}>⏳</span>
          <span style={{ color: '#78350F', fontSize: 20, fontWeight: 700 }}>
            Clientes esperando en la fila y descontrol en caja.
          </span>
        </div>
      </div>

      {/* Footer de Impacto */}
      <div
        style={{
          transform: `scale(${footerSpring})`,
          marginTop: 25,
          color: TOKE_COLORS.textoSecundario,
          fontSize: 16,
          fontWeight: 600,
        }}
      >
        ❌ Hasta 3 minutos perdidos por cada cobro en horas pico
      </div>
    </div>
  );
};
