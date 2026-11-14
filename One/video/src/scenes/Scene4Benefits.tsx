import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_SMOOTH, TOKE_COLORS } from '../theme';

export const Scene4Benefits: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas suaves
  const headerSpring = spring({ frame, fps, config: SPRING_SMOOTH });

  const card1Y = interpolate(frame, [15, 35], [30, 0], { extrapolateRight: 'clamp' });
  const card1Opacity = interpolate(frame, [15, 30], [0, 1], { extrapolateRight: 'clamp' });

  const card2Y = interpolate(frame, [35, 55], [30, 0], { extrapolateRight: 'clamp' });
  const card2Opacity = interpolate(frame, [35, 50], [0, 1], { extrapolateRight: 'clamp' });

  const card3Y = interpolate(frame, [55, 75], [30, 0], { extrapolateRight: 'clamp' });
  const card3Opacity = interpolate(frame, [55, 70], [0, 1], { extrapolateRight: 'clamp' });

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [270, 290], [1, 0], {
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
        padding: '0 60px',
        boxSizing: 'border-box',
      }}
    >
      {/* Audio Narrado de la Escena 4 */}
      <Audio src={staticFile('audio/scene4_benefits.mp3')} />

      {/* Cabecera */}
      <div
        style={{
          transform: `scale(${headerSpring})`,
          textAlign: 'center',
          marginBottom: 45,
          maxWidth: 960,
        }}
      >
        <div
          style={{
            color: TOKE_COLORS.verdeMarca,
            fontSize: 20,
            fontWeight: 800,
            letterSpacing: '2px',
            textTransform: 'uppercase',
            marginBottom: 12,
          }}
        >
          Ventajas Exclusivas
        </div>
        <h2
          style={{
            fontSize: 58,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: 0,
            lineHeight: 1.2,
            letterSpacing: '-2px',
          }}
        >
          Atención rápida, transparente y 100% segura
        </h2>
      </div>

      {/* 3 Tarjetas Bento Apiladas Verticalmente (9:16) */}
      <div
        style={{
          display: 'flex',
          flexDirection: 'column',
          gap: 24,
          width: '100%',
          maxWidth: 920,
        }}
      >
        {/* Tarjeta 1: Rapidez */}
        <div
          style={{
            transform: `translateY(${card1Y}px)`,
            opacity: card1Opacity,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 26,
            padding: '28px 32px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.06)',
            display: 'flex',
            alignItems: 'center',
            gap: 24,
          }}
        >
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: 22,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `2px solid ${TOKE_COLORS.verdeBordeSolido}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <img src={staticFile('tienda.png')} style={{ width: 40, height: 40 }} alt="Tienda" />
          </div>
          <div>
            <h3
              style={{
                color: TOKE_COLORS.textoPrincipal,
                fontSize: 28,
                fontWeight: 800,
                margin: '0 0 6px 0',
              }}
            >
              Atención 3x más rápida
            </h3>
            <p
              style={{
                color: TOKE_COLORS.textoSecundario,
                fontSize: 20,
                lineHeight: 1.4,
                margin: 0,
              }}
            >
              Cero colas en caja. Tu equipo despacha pedidos al segundo sin esperar al dueño.
            </p>
          </div>
        </div>

        {/* Tarjeta 2: Voz */}
        <div
          style={{
            transform: `translateY(${card2Y}px)`,
            opacity: card2Opacity,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.verdeBordeSolido}`,
            borderRadius: 26,
            padding: '28px 32px',
            boxShadow: '0 8px 24px rgba(0, 166, 81, 0.12)',
            display: 'flex',
            alignItems: 'center',
            gap: 24,
          }}
        >
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: 22,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `2px solid ${TOKE_COLORS.verdeMarca}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              flexShrink: 0,
            }}
          >
            <img src={staticFile('campana.png')} style={{ width: 40, height: 40 }} alt="Campana" />
          </div>
          <div>
            <h3
              style={{
                color: TOKE_COLORS.textoPrincipal,
                fontSize: 28,
                fontWeight: 800,
                margin: '0 0 6px 0',
              }}
            >
              Aviso por voz al local
            </h3>
            <p
              style={{
                color: TOKE_COLORS.textoSecundario,
                fontSize: 20,
                lineHeight: 1.4,
                margin: 0,
              }}
            >
              La app canta el cobro en el parlante: manos libres para seguir atendiendo.
            </p>
          </div>
        </div>

        {/* Tarjeta 3: Seguridad */}
        <div
          style={{
            transform: `translateY(${card3Y}px)`,
            opacity: card3Opacity,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 26,
            padding: '28px 32px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.06)',
            display: 'flex',
            alignItems: 'center',
            gap: 24,
          }}
        >
          <div
            style={{
              width: 72,
              height: 72,
              borderRadius: 22,
              background: TOKE_COLORS.ambarFondoSolido,
              border: `2px solid ${TOKE_COLORS.ambarBordeSolido}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 36,
              flexShrink: 0,
            }}
          >
            🛡️
          </div>
          <div>
            <h3
              style={{
                color: TOKE_COLORS.textoPrincipal,
                fontSize: 28,
                fontWeight: 800,
                margin: '0 0 6px 0',
              }}
            >
              Cero acceso a bancos
            </h3>
            <p
              style={{
                color: TOKE_COLORS.textoSecundario,
                fontSize: 20,
                lineHeight: 1.4,
                margin: 0,
              }}
            >
              Sin ingresar claves bancarias ni cuentas. Tu dinero permanece 100% seguro.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
