import React from 'react';
import { interpolate, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_BOUNCY, SPRING_EXTRA_BOUNCY, TOKE_COLORS } from '../theme';

export const Scene5CTA: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entrada inicial del logo con resorte
  const logoSpring = spring({ frame, fps, config: SPRING_EXTRA_BOUNCY });

  // Dinámica de Contorno y Transición de Color:
  // Del frame 0 al 70: Color alternativo (#6366F1 Indigo)
  // Del frame 70 al 130: El contorno se traza y el color muta dinámicamente al Verde Oficial TOKE (#00A651 / #00C853)
  const morphProgress = interpolate(frame, [70, 120], [0, 1], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  // Trazado del contorno dinámico (stroke dashoffset)
  const strokeProgress = interpolate(frame, [65, 115], [380, 0], {
    extrapolateLeft: 'clamp',
    extrapolateRight: 'clamp',
  });

  // Rebote elástico cuando el contorno se completa y bloquea el color oficial (frame 115)
  const lockSpring = spring({
    frame: frame - 115,
    fps,
    config: SPRING_EXTRA_BOUNCY,
  });

  // Interpolación de colores entre Alternativo e Identidad Original TOKE
  // 0 = Alternativo (#6366F1), 1 = Verde TOKE (#00A651)
  const borderColor = morphProgress < 0.5 ? '#6366F1' : TOKE_COLORS.verdeMarca;
  const shadowColor =
    morphProgress < 0.5 ? 'rgba(99, 102, 241, 0.3)' : 'rgba(0, 166, 81, 0.35)';

  // Entradas de textos y botón CTA
  const titleSpring = spring({ frame: frame - 30, fps, config: SPRING_BOUNCY });
  const buttonSpring = spring({ frame: frame - 125, fps, config: SPRING_EXTRA_BOUNCY });
  const badgeSpring = spring({ frame: frame - 145, fps, config: SPRING_BOUNCY });

  // Pulso sutil continuo después del asentamiento
  const idlePulse = Math.sin((frame - 130) * 0.12) * 0.03;

  return (
    <div
      style={{
        flex: 1,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        backgroundColor: TOKE_COLORS.fondo,
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
        padding: '0 80px',
        textAlign: 'center',
        position: 'relative',
      }}
    >
      {/* Audio Narrado de la Escena 5 */}
      <Audio src={staticFile('audio/scene5_cta.mp3')} />

      {/* CONTENEDOR DEL LOGO CON CONTORNO DINÁMICO Y TRANSFORMACIÓN DE COLOR */}
      <div style={{ position: 'relative', width: 140, height: 140, marginBottom: 28 }}>
        {/* SVG de Contorno Dinámico que se traza alrededor del logo */}
        <svg
          style={{
            position: 'absolute',
            top: -6,
            left: -6,
            width: 152,
            height: 152,
            pointerEvents: 'none',
            zIndex: 10,
          }}
        >
          <rect
            x="4"
            y="4"
            width="144"
            height="144"
            rx="40"
            fill="none"
            stroke={morphProgress > 0.5 ? TOKE_COLORS.verdeMarca : '#6366F1'}
            strokeWidth="5"
            strokeDasharray="400"
            strokeDashoffset={strokeProgress}
            strokeLinecap="round"
          />
        </svg>

        {/* Tarjeta del Logo con Morph de Color */}
        <div
          style={{
            transform: `scale(${logoSpring + (frame > 120 ? lockSpring * 0.08 + idlePulse : 0)})`,
            width: 140,
            height: 140,
            borderRadius: 38,
            background: TOKE_COLORS.tarjeta,
            border: `3.5px solid ${borderColor}`,
            boxShadow: `0 14px 35px ${shadowColor}`,
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            transition: 'border-color 0.2s ease, box-shadow 0.2s ease',
          }}
        >
          <img
            src={staticFile('logo.png')}
            style={{
              width: 90,
              height: 90,
              objectFit: 'contain',
              // Filtro dinámico: inicia con tono índigo/alternativo y transiciona al color natural
              filter:
                morphProgress < 1
                  ? `hue-rotate(${(1 - morphProgress) * 140}deg) saturate(${1 + (1 - morphProgress) * 0.5})`
                  : 'none',
            }}
            alt="TOKE Logo"
          />
        </div>

        {/* Etiqueta animada de Contorno Dinámico */}
        {frame > 65 && frame < 130 && (
          <div
            style={{
              position: 'absolute',
              bottom: -24,
              left: '50%',
              transform: 'translateX(-50%)',
              background: TOKE_COLORS.tarjeta,
              border: `1px solid ${borderColor}`,
              color: borderColor,
              fontSize: 11,
              fontWeight: 800,
              padding: '2px 10px',
              borderRadius: 999,
              whiteSpace: 'nowrap',
              boxShadow: '0 2px 8px rgba(0,0,0,0.06)',
            }}
          >
            {morphProgress < 0.9 ? 'Contorneando identidad...' : '✓ Verde Original TOKE'}
          </div>
        )}
      </div>

      {/* Titulares de Llamado a la Acción */}
      <div
        style={{
          transform: `scale(${titleSpring})`,
          marginBottom: 30,
        }}
      >
        <h2
          style={{
            fontSize: 54,
            fontWeight: 900,
            color: TOKE_COLORS.textoPrincipal,
            margin: '0 0 14px 0',
            letterSpacing: '-1.5px',
          }}
        >
          ¡Moderniza los cobros de tu negocio hoy!
        </h2>
        <p
          style={{
            fontSize: 24,
            color: TOKE_COLORS.textoSecundario,
            margin: 0,
            maxWidth: 780,
            fontWeight: 500,
          }}
        >
          Únete a los comercios en Perú que ya cobran sin interrupciones, sin llamadas y en tiempo real.
        </p>
      </div>

      {/* Botón CTA Sólido con Rebote */}
      <div
        style={{
          transform: `scale(${buttonSpring})`,
          marginBottom: 25,
        }}
      >
        <div
          style={{
            background: TOKE_COLORS.verdeMarca,
            color: TOKE_COLORS.textoBlanco,
            padding: '18px 46px',
            borderRadius: 22,
            fontSize: 24,
            fontWeight: 900,
            boxShadow: '0 10px 25px rgba(0, 166, 81, 0.35)',
            display: 'flex',
            alignItems: 'center',
            gap: 14,
            letterSpacing: '-0.5px',
            border: '2px solid #00B44A',
          }}
        >
          <span>🤖</span>
          <span>Descargar App Android Gratis</span>
        </div>
      </div>

      {/* Enlace Web & Badge Perú */}
      <div
        style={{
          transform: `scale(${badgeSpring})`,
          display: 'flex',
          alignItems: 'center',
          gap: 16,
        }}
      >
        <div
          style={{
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.verdeBordeSolido}`,
            padding: '10px 24px',
            borderRadius: 14,
            color: TOKE_COLORS.verdeMarca,
            fontSize: 19,
            fontWeight: 800,
            boxShadow: '0 4px 12px rgba(15, 23, 42, 0.05)',
          }}
        >
          🌐 tokelabs.pages.dev
        </div>

        <div
          style={{
            background: TOKE_COLORS.tarjeta,
            border: `1.5px solid ${TOKE_COLORS.borde}`,
            padding: '10px 20px',
            borderRadius: 14,
            color: TOKE_COLORS.textoPrincipal,
            fontSize: 16,
            fontWeight: 700,
            boxShadow: '0 4px 12px rgba(15, 23, 42, 0.05)',
          }}
        >
          🇵🇪 Hecho a la medida de Perú
        </div>
      </div>
    </div>
  );
};
