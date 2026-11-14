import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_BOUNCY, SPRING_EXTRA_BOUNCY, TOKE_COLORS } from '../theme';

export const Scene4Benefits: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entradas escalonadas con rebote
  const headerSpring = spring({ frame, fps, config: SPRING_BOUNCY });
  const card1Spring = spring({ frame: frame - 15, fps, config: SPRING_EXTRA_BOUNCY });
  const card2Spring = spring({ frame: frame - 35, fps, config: SPRING_EXTRA_BOUNCY });
  const card3Spring = spring({ frame: frame - 55, fps, config: SPRING_EXTRA_BOUNCY });

  // Desvanecimiento de salida
  const exitOpacity = interpolate(frame, [260, 280], [1, 0], {
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
        }}
      >
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
          Ventajas Exclusivas
        </div>
        <h2
          style={{
            fontSize: 50,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: 0,
            letterSpacing: '-1.5px',
          }}
        >
          Atención rápida, transparente y 100% segura
        </h2>
      </div>

      {/* Grid de 3 Tarjetas Bento (Colores sólidos, sin glassmorphism) */}
      <div
        style={{
          display: 'grid',
          gridTemplateColumns: 'repeat(3, 1fr)',
          gap: 30,
          width: '100%',
          maxWidth: 1200,
        }}
      >
        {/* Tarjeta 1: Rapidez */}
        <div
          style={{
            transform: `scale(${card1Spring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 24,
            padding: '36px 28px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.07)',
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
          }}
        >
          <div
            style={{
              width: 58,
              height: 58,
              borderRadius: 18,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `1.5px solid ${TOKE_COLORS.verdeBordeSolido}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <img src={staticFile('tienda.png')} style={{ width: 32, height: 32 }} alt="Tienda" />
          </div>
          <h3 style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 24, fontWeight: 800, margin: 0 }}>
            Atención 3x más rápida
          </h3>
          <p style={{ color: TOKE_COLORS.textoSecundario, fontSize: 16, lineHeight: 1.5, margin: 0 }}>
            Cero colas en mostrador. Tu equipo despacha pedidos al segundo sin esperar al dueño.
          </p>
        </div>

        {/* Tarjeta 2: Voz */}
        <div
          style={{
            transform: `scale(${card2Spring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.verdeBordeSolido}`,
            borderRadius: 24,
            padding: '36px 28px',
            boxShadow: '0 8px 24px rgba(0, 166, 81, 0.12)',
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
          }}
        >
          <div
            style={{
              width: 58,
              height: 58,
              borderRadius: 18,
              background: TOKE_COLORS.verdeFondoSolido,
              border: `1.5px solid ${TOKE_COLORS.verdeMarca}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
            }}
          >
            <img src={staticFile('campana.png')} style={{ width: 32, height: 32 }} alt="Campana" />
          </div>
          <h3 style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 24, fontWeight: 800, margin: 0 }}>
            Aviso por voz al local
          </h3>
          <p style={{ color: TOKE_COLORS.textoSecundario, fontSize: 16, lineHeight: 1.5, margin: 0 }}>
            La app canta el cobro por el parlante de tu tienda: manos libres para no parar de trabajar.
          </p>
        </div>

        {/* Tarjeta 3: Seguridad */}
        <div
          style={{
            transform: `scale(${card3Spring})`,
            background: TOKE_COLORS.tarjeta,
            border: `2px solid ${TOKE_COLORS.borde}`,
            borderRadius: 24,
            padding: '36px 28px',
            boxShadow: '0 8px 24px rgba(15, 23, 42, 0.07)',
            display: 'flex',
            flexDirection: 'column',
            gap: 16,
          }}
        >
          <div
            style={{
              width: 58,
              height: 58,
              borderRadius: 18,
              background: TOKE_COLORS.ambarFondoSolido,
              border: `1.5px solid ${TOKE_COLORS.ambarBordeSolido}`,
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 28,
            }}
          >
            🛡️
          </div>
          <h3 style={{ color: TOKE_COLORS.textoPrincipal, fontSize: 24, fontWeight: 800, margin: 0 }}>
            Cero acceso a bancos
          </h3>
          <p style={{ color: TOKE_COLORS.textoSecundario, fontSize: 16, lineHeight: 1.5, margin: 0 }}>
            Sin ingresar contraseñas bancarias ni vincular cuentas. Tu dinero permanece 100% seguro.
          </p>
        </div>
      </div>
    </div>
  );
};
