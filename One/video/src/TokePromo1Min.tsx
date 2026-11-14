import React from 'react';
import { AbsoluteFill, Sequence, staticFile } from 'remotion';
import { TOKE_COLORS } from './theme';
import { Scene1Hook } from './scenes/Scene1Hook';
import { Scene2Hero } from './scenes/Scene2Hero';
import { Scene3Demo } from './scenes/Scene3Demo';
import { Scene4Benefits } from './scenes/Scene4Benefits';
import { Scene5CTA } from './scenes/Scene5CTA';

export const TokePromo1Min: React.FC = () => {
  return (
    <AbsoluteFill
      style={{
        backgroundColor: TOKE_COLORS.fondo,
        color: TOKE_COLORS.textoPrincipal,
        overflow: 'hidden',
        fontFamily: 'Inter, system-ui, -apple-system, sans-serif',
      }}
    >
      {/* Barra de Marca Superior Fija (Diseño Sólido, Sin Glassmorphism) */}
      <div
        style={{
          position: 'absolute',
          top: 30,
          left: 60,
          right: 60,
          display: 'flex',
          justifyContent: 'space-between',
          alignItems: 'center',
          zIndex: 50,
          background: TOKE_COLORS.tarjeta,
          border: `1.5px solid ${TOKE_COLORS.borde}`,
          borderRadius: 18,
          padding: '12px 24px',
          boxShadow: '0 4px 14px rgba(15, 23, 42, 0.05)',
        }}
      >
        <div style={{ display: 'flex', alignItems: 'center', gap: 14 }}>
          <img
            src={staticFile('logo.png')}
            style={{ width: 30, height: 30, objectFit: 'contain' }}
            alt="TOKE"
          />
          <span
            style={{
              fontWeight: 900,
              fontSize: 18,
              letterSpacing: '0.5px',
              color: TOKE_COLORS.textoPrincipal,
            }}
          >
            TOKE <span style={{ color: TOKE_COLORS.verdeMarca }}>· COBRATE</span>
          </span>
        </div>

        <div style={{ display: 'flex', alignItems: 'center', gap: 12 }}>
          <span
            style={{
              fontSize: 13,
              fontWeight: 700,
              color: TOKE_COLORS.textoSecundario,
            }}
          >
            App de Cobros para Negocios en Perú
          </span>
          <span
            style={{
              background: TOKE_COLORS.verdeFondoSolido,
              color: TOKE_COLORS.verdeMarca,
              border: `1px solid ${TOKE_COLORS.verdeBordeSolido}`,
              padding: '4px 12px',
              borderRadius: 8,
              fontSize: 12,
              fontWeight: 800,
            }}
          >
            ● En Vivo
          </span>
        </div>
      </div>

      {/* Escena 1: El Hook Fuerte (Frames 0 a 400 - ~13.3s) */}
      <Sequence from={0} durationInFrames={400}>
        <Scene1Hook />
      </Sequence>

      {/* Escena 2: Presentación TOKE (Frames 390 a 710 - ~10.7s) */}
      <Sequence from={390} durationInFrames={320}>
        <Scene2Hero />
      </Sequence>

      {/* Escena 3: Demostración en Vivo (Frames 700 a 1140 - ~14.7s) */}
      <Sequence from={700} durationInFrames={440}>
        <Scene3Demo />
      </Sequence>

      {/* Escena 4: Beneficios y Seguridad (Frames 1130 a 1410 - ~9.3s) */}
      <Sequence from={1130} durationInFrames={280}>
        <Scene4Benefits />
      </Sequence>

      {/* Escena 5: Llamado a la Acción + Contorno y Morph de Logo (Frames 1400 a 1800 - ~13.3s) */}
      <Sequence from={1400} durationInFrames={400}>
        <Scene5CTA />
      </Sequence>
    </AbsoluteFill>
  );
};
