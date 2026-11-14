import React from 'react';
import { interpolate, interpolateColors, spring, staticFile, useCurrentFrame, useVideoConfig } from 'remotion';
import { Audio } from 'remotion';
import { SPRING_IMPACT_BOUNCE, SPRING_SMOOTH, TOKE_COLORS } from '../theme';

export const Scene5CTA: React.FC = () => {
  const frame = useCurrentFrame();
  const { fps } = useVideoConfig();

  // Entrada inicial del logo
  const logoEntrance = spring({ frame, fps, config: SPRING_SMOOTH });

  // REQUERIMIENTO ESPECÍFICO: EL CAMBIO DE COLOR ES DEL FONDO (NO DE LOS BORDES)
  // Del frame 0 al 60: Fondo alternativo (#312E81 Índigo)
  // Del frame 60 al 120: El FONDO cambia dinámicamente al color Verde Oficial TOKE (#00A651)
  const currentBgColor = interpolateColors(
    frame,
    [60, 115],
    [TOKE_COLORS.fondoAlternativoFinal, TOKE_COLORS.fondoOriginalFinal]
  );

  // MOMENTO DE IMPACTO BOUNCY 2: Rebote elástico del logo cuando el fondo completa su color oficial
  const logoBounceWhenBgSettles = spring({
    frame: frame - 115,
    fps,
    config: SPRING_IMPACT_BOUNCE,
  });

  // Entradas de textos y botón
  const titleSpring = spring({ frame: frame - 25, fps, config: SPRING_SMOOTH });
  const buttonSpring = spring({ frame: frame - 120, fps, config: SPRING_SMOOTH });
  const badgeSpring = spring({ frame: frame - 135, fps, config: SPRING_SMOOTH });

  return (
    <div
      style={{
        width: 1080,
        height: 1920,
        display: 'flex',
        flexDirection: 'column',
        alignItems: 'center',
        justifyContent: 'center',
        // EL FONDO MUTANDO DINÁMICAMENTE (Sin afectar los bordes de la tarjeta)
        backgroundColor: currentBgColor,
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
        padding: '0 60px',
        boxSizing: 'border-box',
        textAlign: 'center',
        position: 'relative',
        transition: 'background-color 0.1s linear',
      }}
    >
      {/* Audio Narrado de la Escena 5 */}
      <Audio src={staticFile('audio/scene5_cta.mp3')} />

      {/* Tarjeta Sólida del Logo TOKE con Rebote de Remate cuando el fondo muta */}
      <div
        style={{
          transform: `scale(${logoEntrance + (frame > 115 ? logoBounceWhenBgSettles * 0.1 : 0)})`,
          width: 180,
          height: 180,
          borderRadius: 48,
          background: TOKE_COLORS.tarjeta,
          border: '4px solid #FFFFFF',
          boxShadow: '0 20px 50px rgba(0, 0, 0, 0.25)',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          marginBottom: 40,
        }}
      >
        <img
          src={staticFile('logo.png')}
          style={{ width: 115, height: 115, objectFit: 'contain' }}
          alt="TOKE Logo"
        />
      </div>

      {/* Titulares de Llamado a la Acción */}
      <div
        style={{
          transform: `scale(${titleSpring})`,
          marginBottom: 45,
          maxWidth: 960,
        }}
      >
        <h2
          style={{
            fontSize: 72,
            fontWeight: 900,
            color: '#FFFFFF',
            margin: '0 0 18px 0',
            letterSpacing: '-2.5px',
            lineHeight: 1.15,
          }}
        >
          ¡Moderniza los cobros de tu negocio hoy!
        </h2>
        <p
          style={{
            fontSize: 32,
            color: '#E0E7FF',
            margin: 0,
            fontWeight: 500,
            lineHeight: 1.35,
          }}
        >
          Únete a los comercios en Perú que ya cobran en tiempo real, sin interrupciones ni capturas.
        </p>
      </div>

      {/* Botón CTA Blanco Sólido de Gran Impacto */}
      <div
        style={{
          transform: `scale(${buttonSpring})`,
          marginBottom: 35,
          width: '100%',
          maxWidth: 820,
        }}
      >
        <div
          style={{
            background: '#FFFFFF',
            color: TOKE_COLORS.verdeMarca,
            padding: '24px 44px',
            borderRadius: 26,
            fontSize: 30,
            fontWeight: 900,
            boxShadow: '0 16px 40px rgba(0, 0, 0, 0.2)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            gap: 16,
            letterSpacing: '-0.5px',
          }}
        >
          <span style={{ fontSize: 34 }}>🤖</span>
          <span>Descargar App Android Gratis</span>
        </div>
      </div>

      {/* Badges de Enlace Web y País */}
      <div
        style={{
          transform: `scale(${badgeSpring})`,
          display: 'flex',
          gap: 20,
        }}
      >
        <div
          style={{
            background: 'rgba(255, 255, 255, 0.15)',
            border: '2px solid rgba(255, 255, 255, 0.4)',
            padding: '14px 28px',
            borderRadius: 18,
            color: '#FFFFFF',
            fontSize: 24,
            fontWeight: 800,
          }}
        >
          🌐 tokelabs.pages.dev
        </div>

        <div
          style={{
            background: 'rgba(255, 255, 255, 0.15)',
            border: '2px solid rgba(255, 255, 255, 0.4)',
            padding: '14px 28px',
            borderRadius: 18,
            color: '#FFFFFF',
            fontSize: 22,
            fontWeight: 700,
          }}
        >
          🇵🇪 Hecho para Perú
        </div>
      </div>
    </div>
  );
};
