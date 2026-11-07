-- Un método de cobro puede ser "de Plin": el método es la billetera o banco (columna tipo),
-- y es_plin indica que esa billetera se usa para recibir pagos Plin. Cada billetera sigue
-- siendo un solo método; por eso un negocio puede tener varios Plin solo en billeteras distintas.
ALTER TABLE public.metodos_de_cobro
  ADD COLUMN IF NOT EXISTS es_plin boolean NOT NULL DEFAULT false;

-- Yape y Plin no son "una billetera para Plin": es_plin solo aplica a bancos y otras billeteras.
ALTER TABLE public.metodos_de_cobro
  DROP CONSTRAINT IF EXISTS metodos_de_cobro_es_plin_tipo_check;
ALTER TABLE public.metodos_de_cobro
  ADD CONSTRAINT metodos_de_cobro_es_plin_tipo_check
  CHECK (NOT es_plin OR tipo NOT IN ('YAPE', 'PLIN', 'TRANSFERENCIA'));

COMMENT ON COLUMN public.metodos_de_cobro.es_plin IS
  'true si este método (billetera o banco indicado en tipo) se usa para recibir pagos Plin.';
