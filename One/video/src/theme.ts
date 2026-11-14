import { SpringConfig } from 'remotion';

// Colores sólidos - Tema Blanco Oficial TOKE (sin glassmorphism, sin transparencias difusas)
export const TOKE_COLORS = {
  // Fondos y tarjetas sólidas
  fondo: '#F2F4F7',          // Gris neutro limpio de la app
  tarjeta: '#FFFFFF',        // Blanco sólido puro
  tarjetaSecundaria: '#F8FAFC',
  borde: '#E2E8F0',          // Borde sólido nítido
  bordeOscuro: '#CBD5E1',

  // Tipografía
  textoPrincipal: '#0F172A', // Slate 900 casi negro
  textoSecundario: '#64748B',// Slate 500
  textoBlanco: '#FFFFFF',

  // Marca TOKE original
  verdeMarca: '#00A651',     // Verde oficial de TOKE
  verdeVivo: '#00C853',      // Verde brillante de confirmación
  verdeFondoSolido: '#E8F5E9',
  verdeBordeSolido: '#A8DAB5',

  // Billeteras del comercio peruano
  yapeMorado: '#742284',
  yapeCyan: '#00D2B5',
  plinAzul: '#0066FF',
  plinCyan: '#00D2F7',

  // Estados
  ambar: '#F59E0B',
  ambarFondoSolido: '#FEF3C7',
  ambarBordeSolido: '#FCD34D',
  rojoAlerta: '#DC2626',
  rojoFondoSolido: '#FEE2E2',

  // Color alternativo del FONDO en la escena final antes de mutar al original
  fondoAlternativoFinal: '#312E81', // Índigo profundo / vibrante
  fondoOriginalFinal: '#00A651',     // Verde oficial de marca TOKE
};

// Física suave y fluida para la mayoría de elementos (SIN REBOTE EXCESIVO)
export const SPRING_SMOOTH: Partial<SpringConfig> = {
  damping: 18,
  mass: 0.8,
  stiffness: 150,
  overshootClamping: true, // Asentamiento directo, sin oscilaciones
};

// Resorte con rebote (Bouncy) reservado ÚNICAMENTE para los 2 momentos de mayor impacto:
// 1. La confirmación del pago recibido
// 2. El remate del logo sobre el fondo dinámico
export const SPRING_IMPACT_BOUNCE: Partial<SpringConfig> = {
  damping: 9,
  mass: 0.7,
  stiffness: 140,
};
