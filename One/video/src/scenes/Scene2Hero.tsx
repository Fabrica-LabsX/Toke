import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_BOUNCY, SPRING_EXTRA_BOUNCY, TOKE_COLORS } from '../theme';

export const Scene2Hero: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas con rebote elástico
  const logoSpring = spring({ frame, fps, config: SPRING_EXTRA_BOUNCY });
  const titleSpring = spring({ frame: frame - 15, fps, config: SPRING_BOUNCY });
  const badge1Spring = spring({ frame: frame - 40, fps, config: SPRING_EXTRA_BOUNCY });
  const badge2Spring = spring({ frame: frame - 60, fps, config: SPRING_EXTRA_BOUNCY });
  const badge3Spring = spring({ frame: frame - 80, fps, config: SPRING_EXTRA_BOUNCY });

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [300, 320], [1, 0], {
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
      {/* Audio Narrado de la Escena 2 */}
      <Audio src={staticFile('audio/scene2_hero.mp3')} />

      {/* Tarjeta Sólida del Logo TOKE */}
      <div
        style={{
          transform: `scale(${logoSpring})`,
          width: 130,
          height: 130,
          borderRadius: 36,
          background: TOKE_COLORS.tarjeta,
          border: `3px solid ${TOKE_COLORS.verdeMarca}`,
          boxShadow: '0 12px 30px rgba(0, 166, 81, 0.25)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 28,
        }}
      >
        <img
          src={staticFile('logo.png')}
          style={{ width: 85, height: 85, objectFit: 'contain' }}
          alt="TOKE"
        />
      </div>

      {/* Titulares de Marca */}
      <div
        style={{
          transform: `scale(${titleSpring})`,
          textAlign: 'center',
          marginBottom: 35,
        }}
      >
        <div
          style={{
            display: 'inline-block',
            padding: '6px 20px',
            borderRadius: 999,
            background: TOKE_COLORS.verdeFondoSolido,
            border: `1.5px solid ${TOKE_COLORS.verdeBordeSolido}`,
            color: TOKE_COLORS.verdeMarca,
            fontSize: 16,
            fontWeight: 800,
            letterSpacing: '2px',
            textTransform: 'uppercase',
            marginBottom: 14,
          }}
        >
          Presentando TOKE
        </div>
        <h1
          style={{
            fontSize: 66,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: '0 0 16px 0',
            letterSpacing: '-2px',
          }}
        >
          TOKE <span style={{ color: TOKE_COLORS.verdeMarca }}>· Cobrate</span>
        </h1>
        <p
          style={{
            fontSize: 26,
            color: TOKE_COLORS.textoSecundario,
            margin: 0,
            maxWidth: 820,
            lineHeight: 1.4,
            fontWeight: 500,
          }}
        >
          ¡Se acabaron las llamadas y las capturas falsas por WhatsApp!
          <br />
          <strong style={{ color: TOKE_COLORS.textoPrincipal }}>
            El dueño cobra. El equipo de caja lo sabe al instante.
          </strong>
        </p>
      </div>

      {/* Insignias de Valores Clave (Colores sólidos) */}
      <div style={{ display: 'flex', gap: 20 }}>
        <div
          style={{
            transform: `scale(${badge1Spring})`,
            padding: '14px 24px',
            borderRadius: 18,
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.verdeBordeSolido}`,
            boxShadow: '0 4px 14px rgba(15, 23, 42, 0.06)',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <img src={staticFile('sincronizar.png')} style={{ width: 26, height: 26 }} alt="Sync" />
          <span style={{ color: TOKE_COLORS.verdeMarca, fontSize: 18, fontWeight: 800 }}>
            Sincronización &lt; 100 ms
          </span>
        </div>

        <div
          style={{
            transform: `scale(${badge2Spring})`,
            padding: '14px 24px',
            borderRadius: 18,
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.borde}`,
            boxShadow: '0 4px 14px rgba(15, 23, 42, 0.06)',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <span style={{ fontSize: 22 }}>🔒</span>
          <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 18, fontWeight: 700 }}>
            100% Seguro (Sin claves bancarias)
          </span>
        </div>

        <div
          style={{
            transform: `scale(${badge3Spring})`,
            padding: '14px 24px',
            borderRadius: 18,
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.borde}`,
            boxShadow: '0 4px 14px rgba(15, 23, 42, 0.06)',
            display: 'flex',
            alignItems: 'center',
            gap: 12,
          }}
        >
          <img src={staticFile('equipo.png')} style={{ width: 26, height: 26 }} alt="Equipo" />
          <span style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 18, fontWeight: 700 }}>
            Avisos a todo el equipo
          </span>
        </div>
      </div>
    </div>
  );
};
