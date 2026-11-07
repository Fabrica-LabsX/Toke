-- La marca "Interbank | Plin" es solo visual y se deduce de la billetera; no se guarda nada.
ALTER TABLE public.metodos_de_cobro
  DROP CONSTRAINT IF EXISTS metodos_de_cobro_es_plin_tipo_check;
ALTER TABLE public.metodos_de_cobro
  DROP COLUMN IF EXISTS es_plin;
