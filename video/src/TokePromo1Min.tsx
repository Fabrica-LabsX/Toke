import React from 'react';
import { AbsoluteFill, interpolate, Sequence, staticFile, useCurrentFrame } from 'remotion';
import { TOKE_COLORS } from './theme';
import { Scene1Hook } from './scenes/Scene1Hook';
import { Scene2Hero } from './scenes/Scene2Hero';
import { Scene3Demo } from './scenes/Scene3Demo';
import { Scene4Benefits } from './scenes/Scene4Benefits';
import { Scene5CTA } from './scenes/Scene5CTA';

export const TokePromo1Min: React.FC = () => {
  const frame = useCurrentFrame();

  // Barra de progreso dinámico superior (0% a 100% en los 1800 frames)
  const progressPercent = interpolate(frame, [0, 1800], [0, 100], {
    extrapolateRight: 'clamp',
  });

  return (
    <AbsoluteFill
      style={{
        backgroundColor: TOKE_COLORS.fondo,
        color: TOKE_COLORS.textoPrincipal,
        overflow: 'hidden',
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
      }}
    >
      {/* Barra de Progreso Dinámico Vertical (Top Edge) */}
      <div
        style={{
          position: 'absolute',
          top: 0,
          left: 0,
          right: 0,
          height: 8,
          background: TOKE_COLORS.borde,
          zIndex: 100,
        }}
      >
        <div
          style={{
            height: '100%',
            width: `${progressPercent}%`,
            background: TOKE_COLORS.verdeMarca,
            transition: 'width 0.1s linear',
          }}
        />
      </div>

      {/* Cabecera Flotante Fija Superior (Formato 9:16 Vertical) */}
      <div
        style={{
          position: 'absolute',
          top: 40,
          left: 45,
          right: 45,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          zIndex: 80,
          background: TOKE_COLORS.tarjeta,
          border: `2px solid ${TOKE_COLORS.borde}`,
          borderRadius: 22,
          padding: '14px 28px',
          boxShadow: '0 6px 18px rgba(15, 23, 42, 0.06)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <img
            src={staticFile('logo.png')}
            style={{ width: 36, height: 36, objectFit: 'contain' }}
            alt="TOKE"
          />
          <span
            style={{
              fontWeight: 900,
              fontSize: 22,
              letterSpacing: '0.5px',
              color: TOKE_COLORS.textoPrincipal,
            }}
          >
            TOKE <span style={{ color: TOKE_COLORS.verdeMarca }}>· COBRATE</span>
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 10 }}>
          <span
            style={{
              background: TOKE_COLORS.verdeFondoSolido,
              color: TOKE_COLORS.verdeMarca,
              border: `1.5px solid ${TOKE_COLORS.verdeBordeSolido}`,
              padding: '6px 14px',
              borderRadius: 10,
              fontSize: 15,
              fontWeight: 800,
            }}
          >
            ● En Vivo
          </span>
        </div>
      </div>

      {/* Escena 1: El Hook Fuerte (Frames 0 a 370) */}
      <Sequence from={0} durationInFrames={370}>
        <Scene1Hook />
      </Sequence>

      {/* Escena 2: Presentación TOKE (Frames 360 a 680) */}
      <Sequence from={360} durationInFrames={320}>
        <Scene2Hero />
      </Sequence>

      {/* Escena 3: Demostración en Vivo con Notificación y Voz (Frames 670 a 1100) */}
      <Sequence from={670} durationInFrames={430}>
        <Scene3Demo />
      </Sequence>

      {/* Escena 4: Beneficios y Cero Acceso a Bancos (Frames 1090 a 1380) */}
      <Sequence from={1090} durationInFrames={290}>
        <Scene4Benefits />
      </Sequence>

      {/* Escena 5: Llamado a la Acción + Mutación Dinámica del Fondo a Verde Oficial (Frames 1370 a 1800) */}
      <Sequence from={1370} durationInFrames={430}>
        <Scene5CTA />
      </Sequence>
    </AbsoluteFill>
  );
};
